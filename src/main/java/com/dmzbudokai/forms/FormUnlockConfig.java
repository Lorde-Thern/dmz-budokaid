package com.dmzbudokai.forms;

import com.dmzbudokai.DMZBudokai;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Battle Power needed for each transformation, loaded from {@code config/dmzbudokai/form_unlocks.json}.
 * <p>
 * Lookup order for a form: {@code races.<race>.<group>.<form>}, then {@code races."*".<group>.<form>},
 * then the fallback formula {@code defaultFirstFormBp * defaultBpMultiplierPerLevel^(level - 1)}.
 * The fallback is what makes custom races (hand-made, DMZ Ragnarok, Noea...) work with no setup:
 * every form they define gets a BP requirement from its DMZ unlock level.
 */
public final class FormUnlockConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve(DMZBudokai.MOD_ID).resolve("form_unlocks.json");

	private static Data data = Data.defaults();

	private FormUnlockConfig() {
	}

	public static Data get() {
		return data;
	}

	public static Path file() {
		return FILE;
	}

	/** Loads the file, writing the realistic defaults first if it doesn't exist. Returns an error message or null. */
	public static String load() {
		try {
			if (!Files.exists(FILE)) {
				Files.createDirectories(FILE.getParent());
				try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
					GSON.toJson(Data.defaults(), writer);
				}
			}
			try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				Data loaded = GSON.fromJson(json, Data.class);
				loaded.normalize();
				data = loaded;
			}
			return null;
		} catch (IOException | RuntimeException e) {
			// Keep whatever was loaded before so a typo doesn't wipe everyone's requirements.
			DMZBudokai.LOGGER.error("Could not read {}: {}", FILE, e.getMessage());
			return e.getMessage();
		}
	}

	public static final class Data {
		public String _readme = "Base Battle Power (no power release) needed to unlock each DMZ form. "
				+ "races.<race>.<group>.<form> = BP. Race \"*\" applies to every race. "
				+ "Forms not listed use defaultFirstFormBp * defaultBpMultiplierPerLevel^(unlock level - 1). "
				+ "masterRequired: form skills that also need you to right-click a master NPC first (skill -> entity id). "
				+ "Use /budokai forms in game to see the race, group and form names DMZ is using. "
				+ "Reload with /budokai reload.";
		public double defaultFirstFormBp = 1_000_000;
		public double defaultBpMultiplierPerLevel = 3.0;
		/** DMZ form skills that BP never unlocks (they keep their normal DMZ unlock: quests, wishes, mutations...). */
		public List<String> excludedSkills = new ArrayList<>(List.of("legendaryforms", "androidforms"));
		/**
		 * Form skills that also need you to have talked to (right-clicked) a master NPC first,
		 * as skill -> entity id. Like in the show: Kaioken needs King Kai, Ultra Instinct needs Whis,
		 * Ultra Ego needs Beerus.
		 */
		public Map<String, String> masterRequired = defaultMasters();
		/** Form skills that also need an Angel Path rank. */
		public Map<String, Integer> angelRankRequired = new LinkedHashMap<>();
		public Map<String, Map<String, Map<String, Double>>> races = new LinkedHashMap<>();
		public Map<String, Map<String, Double>> stackForms = new LinkedHashMap<>();

		private static Map<String, String> defaultMasters() {
			Map<String, String> masters = new LinkedHashMap<>();
			masters.put("kaioken", "dragonminez:master_kaiosama");
			masters.put("ultrainstinct", "dragonminez:master_whis");
			masters.put("ultraego", "dragonminez:master_beerus");
			return masters;
		}

		/** BP for a race form, or null if not listed. */
		public Double raceFormBp(String race, String group, String form) {
			Double bp = lookup(races.get(race), group, form);
			return bp != null ? bp : lookup(races.get("*"), group, form);
		}

		public Double stackFormBp(String group, String form) {
			Map<String, Double> forms = stackForms.get(lower(group));
			return forms == null ? null : forms.get(lower(form));
		}

		public double fallbackBp(int level) {
			return defaultFirstFormBp * Math.pow(Math.max(1.0, defaultBpMultiplierPerLevel), Math.max(0, level - 1));
		}

		public boolean isExcluded(String skill) {
			return excludedSkills.contains(lower(skill));
		}

		/** Entity id of the master this skill needs, or "" if none. */
		public String masterFor(String skill) {
			return masterRequired.getOrDefault(lower(skill), "");
		}

		public int angelRankFor(String skill) {
			return angelRankRequired.getOrDefault(lower(skill), 0);
		}

		private static Double lookup(Map<String, Map<String, Double>> groups, String group, String form) {
			if (groups == null) return null;
			Map<String, Double> forms = groups.get(lower(group));
			return forms == null ? null : forms.get(lower(form));
		}

		/** Lower-cases every key so the file is case-insensitive, and fills in anything missing. */
		void normalize() {
			if (excludedSkills == null) excludedSkills = new ArrayList<>();
			excludedSkills.replaceAll(FormUnlockConfig::lower);
			if (angelRankRequired == null) angelRankRequired = new LinkedHashMap<>();
			Map<String, Integer> angel = new LinkedHashMap<>();
			angelRankRequired.forEach((k, v) -> angel.put(lower(k), v == null ? 0 : v));
			angelRankRequired = angel;
			Map<String, String> masters = new LinkedHashMap<>();
			if (masterRequired != null) masterRequired.forEach((k, v) -> { if (v != null && !v.isBlank()) masters.put(lower(k), lower(v.trim())); });
			masterRequired = masters;

			Map<String, Map<String, Map<String, Double>>> r = new LinkedHashMap<>();
			if (races != null) races.forEach((race, groups) -> r.put(lower(race), lowerGroups(groups)));
			races = r;
			stackForms = lowerGroups(stackForms);
			if (defaultBpMultiplierPerLevel < 1.0) defaultBpMultiplierPerLevel = 1.0;
			if (defaultFirstFormBp < 0) defaultFirstFormBp = 0;
		}

		private static Map<String, Map<String, Double>> lowerGroups(Map<String, Map<String, Double>> groups) {
			Map<String, Map<String, Double>> out = new LinkedHashMap<>();
			if (groups == null) return out;
			groups.forEach((group, forms) -> {
				Map<String, Double> f = new LinkedHashMap<>();
				if (forms != null) forms.forEach((form, bp) -> { if (bp != null) f.put(lower(form), bp); });
				out.put(lower(group), f);
			});
			return out;
		}

		/**
		 * "Realistic" defaults: Super Saiyan at 1M base BP, and every other race's forms placed on the
		 * same scale (forms of similar power need similar BP). Names match Dragon Mine Z 2.1.x defaults.
		 */
		public static Data defaults() {
			Data d = new Data();
			d.angelRankRequired.put("ultrainstinct", 2);

			Map<String, Map<String, Double>> saiyan = new LinkedHashMap<>();
			saiyan.put("ssgrades", forms(
					"supersaiyan", 1_000_000,
					"supersaiyangrade2", 1_500_000,
					"supersaiyangrade3", 2_500_000));
			saiyan.put("supersaiyan", forms(
					"supersaiyanmastered", 5_000_000,
					"supersaiyan2", 15_000_000,
					"supersaiyan3", 50_000_000,
					"supersaiyan4", 250_000_000));
			saiyan.put("oozaru", forms(
					"goldenoozaru", 100_000_000,
					"supersaiyan4", 250_000_000));
			d.races.put("saiyan", saiyan);

			Map<String, Map<String, Double>> human = new LinkedHashMap<>();
			human.put("superforms", forms(
					"buffed", 100_000,
					"fullpower", 500_000,
					"overdrive", 3_000_000,
					"solaris", 20_000_000));
			d.races.put("human", human);

			Map<String, Map<String, Double>> namekian = new LinkedHashMap<>();
			namekian.put("superforms", forms(
					"giant", 150_000,
					"fullpower", 750_000,
					"supernamekian", 5_000_000));
			d.races.put("namekian", namekian);

			Map<String, Map<String, Double>> frost = new LinkedHashMap<>();
			frost.put("evolutionforms", forms(
					"second", 100_000,
					"third", 300_000,
					"final", 800_000,
					"fullpower", 10_000_000,
					"fifth", 100_000_000));
			d.races.put("frostdemon", frost);

			Map<String, Map<String, Double>> majin = new LinkedHashMap<>();
			majin.put("pureforms", forms(
					"kid", 250_000,
					"evil", 1_000_000,
					"super", 8_000_000,
					"ultra", 50_000_000));
			d.races.put("majin", majin);

			Map<String, Map<String, Double>> bio = new LinkedHashMap<>();
			bio.put("bioevolution", forms(
					"semiperfect", 300_000,
					"perfect", 1_200_000,
					"superperfect", 10_000_000,
					"ultraperfect", 60_000_000));
			d.races.put("bioandroid", bio);

			d.races.put("*", new LinkedHashMap<>());

			d.stackForms.put("kaioken", forms(
					"x2", 50_000,
					"x3", 150_000,
					"x4", 400_000,
					"x10", 2_000_000,
					"x20", 6_000_000,
					"x100", 75_000_000));
			d.stackForms.put("ultimate", forms("ultimate", 20_000_000));
			d.stackForms.put("ultrainstinct", forms(
					"sign", 500_000_000,
					"mastered", 2_000_000_000));
			d.stackForms.put("ultraego", forms(
					"sign", 500_000_000,
					"mastered", 2_000_000_000));
			return d;
		}

		private static Map<String, Double> forms(Object... pairs) {
			Map<String, Double> map = new LinkedHashMap<>();
			for (int i = 0; i + 1 < pairs.length; i += 2) {
				map.put((String) pairs[i], ((Number) pairs[i + 1]).doubleValue());
			}
			return map;
		}
	}

	static String lower(String s) {
		return s == null ? "" : s.toLowerCase(Locale.ROOT);
	}
}

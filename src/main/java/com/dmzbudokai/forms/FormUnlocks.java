package com.dmzbudokai.forms;

import com.dmzbudokai.angel.AngelPath;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dragonminez.common.config.FormConfig;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Unlocks Dragon Mine Z transformations by Battle Power.
 * <p>
 * DMZ unlocks forms through "form skills": every form has an unlock level, and having the form
 * skill (e.g. {@code superforms}) at that level unlocks it. This class reads the race's form groups
 * straight from DMZ's loaded config, gives each form a BP requirement from
 * {@link FormUnlockConfig}, and raises the form skill to the highest level the player's base BP
 * has earned. It never lowers a skill, so forms earned other ways (quests, wishes) are kept.
 * <p>
 * Because it reads whatever races DMZ has loaded, custom races work automatically.
 * DMZ's own rules still apply on top: mastery requirements between forms, tails for Oozaru, etc.
 */
public final class FormUnlocks {
	private FormUnlocks() {
	}

	/** One form and what it needs. */
	public record FormEntry(String group, String form, String skill, int level, double bp, boolean stack, boolean excluded,
							int angelRank) {
	}

	/** Every form this player's race (plus stack forms) has, in unlock order. */
	public static List<FormEntry> entriesFor(StatsData stats) {
		FormUnlockConfig.Data cfg = FormUnlockConfig.get();
		String race = DmzBridge.race(stats);
		List<FormEntry> entries = new ArrayList<>();

		for (Map.Entry<String, FormConfig> e : DmzBridge.raceFormGroups(race).entrySet()) {
			FormConfig group = e.getValue();
			if (group == null) continue;
			String groupName = groupName(e.getKey(), group);
			String skill = DmzBridge.raceFormSkill(group);
			for (FormConfig.FormData form : group.getForms().values()) {
				if (form == null || form.getName() == null) continue;
				int level = form.getUnlockOnSkillLevel() != null ? form.getUnlockOnSkillLevel() : 0;
				if (level <= 0) continue; // free forms (e.g. Oozaru) need no unlock
				Double bp = cfg.raceFormBp(race, groupName, form.getName());
				entries.add(new FormEntry(groupName, form.getName(), skill, level, bp != null ? bp : cfg.fallbackBp(level),
						false, cfg.isExcluded(skill), cfg.angelRankFor(skill)));
			}
		}

		for (Map.Entry<String, FormConfig> e : DmzBridge.stackFormGroups().entrySet()) {
			FormConfig group = e.getValue();
			if (group == null) continue;
			String groupName = groupName(e.getKey(), group);
			String skill = DmzBridge.stackFormSkill(group);
			for (FormConfig.FormData form : group.getForms().values()) {
				if (form == null || form.getName() == null) continue;
				int level = form.getUnlockOnSkillLevel() != null ? form.getUnlockOnSkillLevel() : 0;
				if (level <= 0) continue;
				Double bp = cfg.stackFormBp(groupName, form.getName());
				entries.add(new FormEntry(groupName, form.getName(), skill, level, bp != null ? bp : cfg.fallbackBp(level),
						true, cfg.isExcluded(skill), cfg.angelRankFor(skill)));
			}
		}

		entries.sort(Comparator.comparing(FormEntry::stack).thenComparing(FormEntry::skill).thenComparingInt(FormEntry::level)
				.thenComparingDouble(FormEntry::bp));
		return entries;
	}

	/**
	 * BP needed for each level of each form skill. A level needs the highest BP of any form at or
	 * below it, so unlocks always happen in order.
	 */
	public static Map<String, TreeMap<Integer, Double>> requirementsBySkill(List<FormEntry> entries) {
		Map<String, TreeMap<Integer, Double>> bySkill = new LinkedHashMap<>();
		for (FormEntry entry : entries) {
			if (entry.excluded()) continue;
			bySkill.computeIfAbsent(entry.skill(), k -> new TreeMap<>()).merge(entry.level(), entry.bp(), Math::max);
		}
		for (TreeMap<Integer, Double> levels : bySkill.values()) {
			double running = 0.0;
			for (Map.Entry<Integer, Double> level : levels.entrySet()) {
				running = Math.max(running, level.getValue());
				level.setValue(running);
			}
		}
		return bySkill;
	}

	/** Checks the player's BP and unlocks anything they've earned. Runs once a second per player. */
	public static void update(ServerPlayer player, StatsData stats, TrainingData data) {
		if (!BudokaiConfig.BP_FORM_UNLOCKS.get() || !DmzBridge.hasCharacter(stats)) return;

		double bp = DmzBridge.baseBattlePower(stats);
		List<FormEntry> entries = entriesFor(stats);
		Set<String> unlocked = new LinkedHashSet<>();

		for (Map.Entry<String, TreeMap<Integer, Double>> skill : requirementsBySkill(entries).entrySet()) {
			String skillName = skill.getKey();
			if (data.getAngelRank() < FormUnlockConfig.get().angelRankFor(skillName)) continue;

			int earned = 0;
			for (Map.Entry<Integer, Double> level : skill.getValue().entrySet()) {
				if (bp < level.getValue()) break;
				earned = level.getKey();
			}

			int current = DmzBridge.skillLevel(stats, skillName);
			if (earned <= current) continue;

			DmzBridge.raiseSkillLevel(stats, skillName, earned);
			int announced = Math.max(current, data.getAnnouncedFormLevel(skillName));
			for (FormEntry entry : entries) {
				if (entry.skill().equals(skillName) && entry.level() > announced && entry.level() <= earned) {
					unlocked.add(prettyName(entry.form()));
				}
			}
			data.setAnnouncedFormLevel(skillName, Math.max(announced, earned));
		}

		if (!unlocked.isEmpty()) {
			player.sendSystemMessage(Component.translatable("dmzbudokai.forms.unlocked", String.join(", ", unlocked))
					.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
			player.level().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6F, 1.4F);
		}
	}

	/** True if the BP requirement (and Angel rank, if any) for this form is met. */
	public static boolean meets(FormEntry entry, double bp, TrainingData data) {
		return bp >= entry.bp() && data.getAngelRank() >= entry.angelRank();
	}

	private static String groupName(String key, FormConfig group) {
		String name = group.getGroupName();
		return FormUnlockConfig.lower(name == null || name.isEmpty() ? key : name);
	}

	/** "supersaiyangrade2" → "Supersaiyangrade2", "shin_shiyoken" → "Shin Shiyoken". */
	public static String prettyName(String form) {
		StringBuilder out = new StringBuilder();
		for (String part : form.replace('-', '_').split("_")) {
			if (part.isEmpty()) continue;
			if (!out.isEmpty()) out.append(' ');
			out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return out.toString();
	}

	public static String formatBp(double bp) {
		return AngelPath.formatBp(bp);
	}
}

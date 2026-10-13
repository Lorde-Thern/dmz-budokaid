package com.dmzbudokai.compat;

import com.dmzbudokai.config.BudokaiConfig;
import com.dragonminez.common.stats.StatsData;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Battle Power math.
 * <p>
 * <b>Trained BP</b> is DMZ's own BP curve applied to your stats (race scaling and permanent bonuses
 * included) with no transformation, buff or power-release multipliers. It only grows by training,
 * so it's what form unlocks, leeching and the Angel Path compare.
 * <p>
 * <b>Realistic BP</b> (when enabled) is what players see: trained BP times Dragon Ball-style form
 * multipliers (SSJ x50, Kaioken xN...) times power release. A mixin makes DMZ report it too.
 */
public final class BattlePower {
	// Same constants as DMZ's StatsData.getBattlePowerExact, so trained BP matches DMZ's base numbers.
	private static final double K = 100.0;
	private static final double BP_REF_VALUE = 1_200.0;
	private static final double BP_CURVE_EXPONENT = 1.2;
	private static final double SUPPORT_STAT_BP_WEIGHT = 0.5;
	private static final String[] FORM_STATS = {"STR", "SKP", "PWR"};

	private static List<? extends String> parsedFrom;
	private static Map<String, Double> parsed = Map.of();

	private BattlePower() {
	}

	public static boolean realisticEnabled() {
		return BudokaiConfig.SPEC.isLoaded() && BudokaiConfig.REALISTIC_BATTLE_POWER.get();
	}

	/** BP from training alone. Never less than 1. */
	public static double trained(StatsData data) {
		var stats = data.getStats();
		double raw = part(data, "STR", "STR", stats.getStrength())
				+ part(data, "SKP", "SKP", stats.getStrikePower())
				+ part(data, "DEF", "DEF", stats.getResistance())
				+ part(data, "PWR", "PWR", stats.getKiPower())
				+ SUPPORT_STAT_BP_WEIGHT * (part(data, "VIT", "VIT", stats.getVitality()) + part(data, "ENE", "ENE", stats.getEnergy()));
		if (Double.isNaN(raw) || raw <= 0) return 1.0;
		double bp = BP_REF_VALUE * Math.pow(raw / K, BP_CURVE_EXPONENT);
		return Double.isNaN(bp) || bp < 1.0 ? 1.0 : bp;
	}

	private static double part(StatsData data, String bonusKey, String scalingKey, int value) {
		double mult = data.getBonusStats().calculateBonus(bonusKey, value, true);
		double flat = data.getBonusStats().calculateBonus(bonusKey, value, false);
		double scaling = data.getStatScaling(scalingKey);
		return (value + mult) * scaling + flat * scaling;
	}

	/** What players see in realistic mode: trained BP x form x stack form x power release. */
	public static double realistic(StatsData data) {
		var character = data.getCharacter();
		String race = DmzBridge.race(data);

		double form = formMultiplier(race, character.getActiveFormGroup(), character.getActiveForm(), data, false);
		double stack = formMultiplier("*", character.getActiveStackFormGroup(), character.getActiveStackForm(), data, true);
		double release = Math.max(0, data.getResources().getPowerRelease()) / 100.0;

		double bp = trained(data) * form * stack * release;
		return Double.isNaN(bp) || bp < 0 ? 0.0 : bp;
	}

	private static double formMultiplier(String race, String group, String form, StatsData data, boolean stack) {
		if (form == null || form.isEmpty() || form.equalsIgnoreCase("base") || group == null || group.isEmpty()) return 1.0;

		Map<String, Double> table = table();
		String suffix = "/" + lower(group) + "/" + lower(form);
		Double listed = table.get(lower(race) + suffix);
		if (listed == null) listed = table.get("*" + suffix);
		if (listed != null) return Math.max(0.0, listed);

		// Unlisted (custom races, addon forms): amplify DMZ's own stat multiplier for that form.
		double sum = 0.0;
		for (String stat : FORM_STATS) {
			sum += stack ? data.getStackFormMultiplier(stat) : data.getFormMultiplier(stat);
		}
		double avg = Math.max(1.0, sum / FORM_STATS.length);
		return Math.pow(avg, BudokaiConfig.FORM_BP_FALLBACK_EXPONENT.get());
	}

	private static Map<String, Double> table() {
		List<? extends String> source = BudokaiConfig.FORM_BP_MULTIPLIERS.get();
		if (source != parsedFrom) {
			Map<String, Double> map = new HashMap<>();
			for (String line : source) {
				int eq = line.indexOf('=');
				if (eq <= 0) continue;
				try {
					map.put(lower(line.substring(0, eq).trim()), Double.parseDouble(line.substring(eq + 1).trim()));
				} catch (NumberFormatException ignored) {
					// skip malformed entries rather than breaking every BP readout
				}
			}
			parsed = map;
			parsedFrom = source;
		}
		return parsed;
	}

	private static String lower(String s) {
		return s == null ? "" : s.toLowerCase(Locale.ROOT);
	}
}

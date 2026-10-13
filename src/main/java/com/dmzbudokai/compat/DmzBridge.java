package com.dmzbudokai.compat;

import com.dmzbudokai.config.BudokaiConfig;
import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.FormConfig;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import com.dragonminez.common.stats.skills.Skill;
import com.dragonminez.common.util.TransformationsHelper;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Every call into Dragon Mine Z goes through here, so a DMZ API change only needs fixing in one place.
 */
public final class DmzBridge {
	private DmzBridge() {
	}

	public static Optional<StatsData> stats(Player player) {
		return StatsProvider.get(StatsCapability.INSTANCE, player).resolve();
	}

	/** True once the player has finished DMZ character creation. */
	public static boolean hasCharacter(StatsData data) {
		return data.getStatus().isHasCreatedCharacter();
	}

	/**
	 * Trained Battle Power: no transformations, buffs or power release, so players compare fairly
	 * whether or not they're transformed or powered up. Never returns less than 1.
	 */
	public static double baseBattlePower(StatsData data) {
		return BattlePower.trained(data);
	}

	/** Current Battle Power exactly as DMZ shows it (includes power release and forms; realistic when enabled). */
	public static double currentBattlePower(StatsData data) {
		return data.getBattlePowerExact();
	}

	public static int statValue(StatsData data, String stat) {
		return data.getCurrentStatValue(stat);
	}

	/**
	 * Adds up to {@code amount} points to a stat, respecting DMZ's per-stat and total stat caps.
	 * Returns the number of points actually added.
	 */
	public static int addStat(StatsData data, String stat, int amount) {
		// Limitless skips DMZ's total stat cap here; StatsMixin lifts DMZ's per-stat cap.
		boolean limitless = BudokaiConfig.LIMITLESS_MODE.get();
		int allowed = limitless ? amount : data.getMaxAllowedIncreaseForStat(stat, amount);
		if (allowed <= 0) return 0;
		int before = data.getCurrentStatValue(stat);
		data.getStats().addStat(stat, allowed);
		return Math.max(0, data.getCurrentStatValue(stat) - before);
	}

	public static float stamina(StatsData data) {
		return data.getResources().getCurrentStamina();
	}

	public static void useStamina(StatsData data, double amount) {
		if (amount > 0) data.getResources().removeStamina((float) amount);
	}

	public static int alignment(StatsData data) {
		return data.getResources().getAlignment();
	}

	public static String race(StatsData data) {
		String race = data.getCharacter().getRaceName();
		return race == null ? "" : race.toLowerCase();
	}

	public static void setForceHalo(StatsData data, boolean enabled) {
		data.getStatus().setForceHalo(enabled);
	}

	public static boolean isForceHalo(StatsData data) {
		return data.getStatus().isForceHalo();
	}

	// ---- Transformations ------------------------------------------------------------------------

	/**
	 * Every form group DMZ has loaded for a race, keyed by group name. This includes custom races
	 * from {@code config/dragonminez/races/<race>/forms/} (hand-made or written by addons).
	 */
	public static Map<String, FormConfig> raceFormGroups(String race) {
		if (race == null || race.isEmpty()) return Collections.emptyMap();
		Map<String, FormConfig> groups = ConfigManager.getAllFormsForRace(race);
		return groups != null ? groups : Collections.emptyMap();
	}

	/** Stack forms (Kaioken, Ultra Instinct...), usable by every race. */
	public static Map<String, FormConfig> stackFormGroups() {
		Map<String, FormConfig> groups = ConfigManager.getAllStackForms();
		return groups != null ? groups : Collections.emptyMap();
	}

	/** The DMZ skill whose level unlocks the forms in a race form group. */
	public static String raceFormSkill(FormConfig group) {
		return TransformationsHelper.getSkillNameForType(group.getFormType()).toLowerCase(Locale.ROOT);
	}

	/** The DMZ skill whose level unlocks the forms in a stack form group. */
	public static String stackFormSkill(FormConfig group) {
		return group.getFormType().toLowerCase(Locale.ROOT);
	}

	public static int skillLevel(StatsData data, String skill) {
		return data.getSkills().getSkillLevel(skill);
	}

	/**
	 * Raises a form skill to {@code level}. DMZ caps form skill levels by the race's TP cost table,
	 * so the cap is raised too when a custom race defines forms past it.
	 */
	public static void raiseSkillLevel(StatsData data, String skill, int level) {
		Skill existing = data.getSkills().getSkill(skill);
		if (existing != null && existing.getMaxLevel() < level) existing.setMaxLevel(level);
		data.getSkills().setSkillLevel(skill, level);
		Skill updated = data.getSkills().getSkill(skill);
		if (updated != null && updated.getLevel() < level) {
			updated.setMaxLevel(level);
			updated.setLevel(level);
		}
	}

	/** DMZ's level/stat cap from general-server.json ({@code gameplay.maxValue}). */
	public static int dmzMaxValue() {
		try {
			return ConfigManager.getServerConfig().getGameplay().getMaxValue();
		} catch (RuntimeException e) {
			return Integer.MAX_VALUE;
		}
	}
}

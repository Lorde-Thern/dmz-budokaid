package com.dmzbudokai.compat;

import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.world.entity.player.Player;

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
	 * Battle Power with DMZ's power-release multiplier taken out, so players compare fairly
	 * whether or not they are powered up. Never returns less than 1.
	 */
	public static double baseBattlePower(StatsData data) {
		double bp = data.getBattlePowerExact();
		int release = Math.max(1, data.getResources().getPowerRelease());
		double base = bp * 100.0 / release;
		if (Double.isNaN(base) || base < 1.0) return 1.0;
		return base;
	}

	/** Current Battle Power exactly as DMZ shows it (includes power release and forms). */
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
		int allowed = data.getMaxAllowedIncreaseForStat(stat, amount);
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
}

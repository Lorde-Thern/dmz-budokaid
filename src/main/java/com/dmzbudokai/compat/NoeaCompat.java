package com.dmzbudokai.compat;

import com.dmzbudokai.config.BudokaiConfig;
import com.dragonminez.common.stats.StatsData;
import net.minecraftforge.fml.ModList;

/**
 * Dragon Block Noea support. Noea builds on Dragon Mine Z's race system, so the Angel Path stays
 * compatible by reading the player's DMZ race name rather than depending on Noea's classes.
 * Which races count as "can't be an angel" (Noea's demon race by default) is set in the config.
 */
public final class NoeaCompat {
	private NoeaCompat() {
	}

	public static boolean isNoeaLoaded() {
		ModList mods = ModList.get();
		for (String id : BudokaiConfig.NOEA_MOD_IDS.get()) {
			if (mods.isLoaded(id)) return true;
		}
		return false;
	}

	public static boolean isAngelForbiddenRace(StatsData stats) {
		String race = DmzBridge.race(stats);
		for (String forbidden : BudokaiConfig.ANGEL_FORBIDDEN_RACES.get()) {
			if (race.equalsIgnoreCase(forbidden)) return true;
		}
		return false;
	}
}

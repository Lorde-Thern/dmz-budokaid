package com.dmzbudokai.compat;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.config.BudokaiConfig;
import com.dragonminez.common.init.entities.IBattlePower;
import com.dragonminez.common.init.entities.sagas.DBSagasEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Puts mob Battle Power on the same realistic scale as players.
 * <p>
 * DMZ rates a mob roughly as {@code health + attack * 5} (plus armor, speed and ranged damage).
 * A player with every stat at {@code s} has about {@code 20 + s} health and {@code 1 + s} melee damage,
 * which DMZ would rate {@code 25 + 6s}. So a mob's DMZ value is turned into "equivalent stats"
 * {@code s = (value - 25) / 6} and run through the same BP curve as a player with those stats.
 * <p>
 * With DMZ's default mob formula: a zombie reads ~170 next to a fresh character's ~230, an iron golem
 * ~1,600, the Ender Dragon ~2,700 and the Warden ~8,800.
 * Story NPCs (Goku, Vegeta, Frieza, Cell...) keep DMZ's hand-set canon numbers.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class MobBattlePower {
	private static final double PLAYER_BASE_VALUE = 25.0;
	private static final double PLAYER_VALUE_PER_STAT = 6.0;
	private static final int MIN_BP = 5;

	private MobBattlePower() {
	}

	public static boolean enabled() {
		return BattlePower.realisticEnabled() && BudokaiConfig.REALISTIC_MOB_BATTLE_POWER.get();
	}

	/** Converts DMZ's mob BP into realistic BP. */
	public static int fromDmz(int dmzValue) {
		double equivalentStat = Math.max(0.0, (dmzValue - PLAYER_BASE_VALUE) / PLAYER_VALUE_PER_STAT);
		double bp = BattlePower.curveForEvenStats(equivalentStat) * BudokaiConfig.MOB_BP_MULTIPLIER.get();
		if (!Double.isFinite(bp) || bp >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return (int) Math.max(MIN_BP, Math.round(bp));
	}

	// LOWEST so anything else that sets BP on spawn has already run; DMZ raids set it before the mob joins.
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onJoin(EntityJoinLevelEvent event) {
		if (event.isCanceled()) return;
		if (!(event.getEntity() instanceof Mob mob) || mob instanceof DBSagasEntity) return;
		if (!(mob instanceof IBattlePower bp) || !enabled()) return;

		// Runs on both sides: the scouter and ki sense read the client copy, so it must match.
		// DMZ doesn't save this value, so each newly joined instance is converted exactly once.
		bp.setBattlePower(fromDmz(bp.getBattlePower()));
	}
}

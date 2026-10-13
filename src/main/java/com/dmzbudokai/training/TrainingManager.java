package com.dmzbudokai.training;

import com.dmzbudokai.angel.AngelPath;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dmzbudokai.network.BudokaiNetwork;
import com.dmzbudokai.network.TrainingHudPacket;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * The heart of the addon: turns one training rep into stat XP, and stat XP into DMZ stat points.
 */
public final class TrainingManager {
	private TrainingManager() {
	}

	/**
	 * Performs one training rep. Returns true if the rep counted (false if the player has no DMZ
	 * character yet or is too exhausted).
	 */
	public static boolean train(ServerPlayer player, TrainingActivity activity) {
		Optional<StatsData> statsOpt = DmzBridge.stats(player);
		Optional<TrainingData> dataOpt = TrainingDataProvider.get(player);
		if (statsOpt.isEmpty() || dataOpt.isEmpty()) return false;

		StatsData stats = statsOpt.get();
		TrainingData data = dataOpt.get();
		if (!DmzBridge.hasCharacter(stats)) return false;

		long now = player.level().getGameTime();

		double staminaCost = activity.staminaCost();
		if (staminaCost > 0 && !player.isCreative()) {
			if (DmzBridge.stamina(stats) < staminaCost) {
				player.displayClientMessage(Component.translatable("dmzbudokai.message.exhausted").withStyle(ChatFormatting.RED), true);
				return false;
			}
			DmzBridge.useStamina(stats, staminaCost);
		}

		TrainingBonus bonus = PartnerTraining.compute(player, stats, data, activity);
		double xp = activity.baseXp() * BudokaiConfig.GLOBAL_GAIN_MULTIPLIER.get() * bonus.total()
				* DmzBridge.gravityTrainingMultiplier(stats);

		int gained = 0;
		for (String stat : activity.stats()) {
			gained += addStatXp(stats, data, stat, xp);
		}

		data.setLastRepTick(now);

		if (bonus.isLeeching()) {
			ServerPlayer source = bonus.leechSource();
			TrainingDataProvider.get(source).ifPresent(sourceData ->
					sourceData.setMentoringUntilTick(now + BudokaiConfig.PARTNER_ACTIVE_TICKS.get()));
		}

		AngelPath.onTrainingRep(player, stats, data, activity);
		for (ServerPlayer angel : bonus.guidingAngels()) {
			TrainingDataProvider.get(angel).ifPresent(angelData -> AngelPath.onMentored(angel, angelData));
		}

		if (gained > 0 || now - data.getLastHudSyncTick() >= BudokaiConfig.HUD_SYNC_INTERVAL.get()) {
			data.setLastHudSyncTick(now);
			BudokaiNetwork.sendTo(player, TrainingHudPacket.of(activity, bonus, gained, data));
		}
		return true;
	}

	/** Training XP needed to raise a stat from its current value by one point. */
	public static double costForNextPoint(int currentValue) {
		return BudokaiConfig.STAT_COST_BASE.get() + currentValue * BudokaiConfig.STAT_COST_PER_POINT.get();
	}

	/**
	 * Adds XP to a stat's bank and spends it on as many stat points as it can afford.
	 * Returns the number of points gained.
	 */
	public static int addStatXp(StatsData stats, TrainingData data, String stat, double xp) {
		int value = DmzBridge.statValue(stats, stat);
		double bank = data.getStatXp(stat) + xp * (1.0 + value * BudokaiConfig.STAT_XP_GROWTH_PER_POINT.get());
		int toAdd = 0;
		int cap = BudokaiConfig.MAX_POINTS_PER_REP.get();

		while (toAdd < cap) {
			double cost = costForNextPoint(value + toAdd);
			if (bank < cost) break;
			bank -= cost;
			toAdd++;
		}

		int added = toAdd > 0 ? DmzBridge.addStat(stats, stat, toAdd) : 0;
		if (added < toAdd) {
			// Hit a DMZ stat cap: don't keep banking XP the player can never spend.
			bank = 0.0;
		}
		data.setStatXp(stat, bank);
		return added;
	}
}

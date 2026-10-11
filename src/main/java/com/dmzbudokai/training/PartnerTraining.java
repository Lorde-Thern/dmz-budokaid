package com.dmzbudokai.training;

import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Works out the partner, leech, mentor and angel-mentor bonuses for a player who is training right now.
 */
public final class PartnerTraining {
	private PartnerTraining() {
	}

	/** True if the player did a training rep recently enough to count as someone's partner. */
	public static boolean isActivelyTraining(TrainingData data, long gameTime) {
		return gameTime - data.getLastRepTick() <= BudokaiConfig.PARTNER_ACTIVE_TICKS.get();
	}

	public static TrainingBonus compute(ServerPlayer player, StatsData stats, TrainingData data, TrainingActivity activity) {
		long now = player.level().getGameTime();
		double radius = BudokaiConfig.PARTNER_RADIUS.get();
		double radiusSq = radius * radius;
		double myBp = DmzBridge.baseBattlePower(stats);

		int partners = 0;
		double strongestBp = 0.0;
		ServerPlayer strongest = null;
		int bestAngelRank = 0;

		for (ServerPlayer other : player.serverLevel().players()) {
			if (other == player || other.isSpectator() || !other.isAlive()) continue;
			if (other.distanceToSqr(player) > radiusSq) continue;

			Optional<TrainingData> otherTraining = TrainingDataProvider.get(other);
			if (otherTraining.isEmpty() || !isActivelyTraining(otherTraining.get(), now)) continue;

			Optional<StatsData> otherStats = DmzBridge.stats(other);
			if (otherStats.isEmpty() || !DmzBridge.hasCharacter(otherStats.get())) continue;

			partners++;
			bestAngelRank = Math.max(bestAngelRank, otherTraining.get().getAngelRank());

			double otherBp = DmzBridge.baseBattlePower(otherStats.get());
			if (otherBp > strongestBp) {
				strongestBp = otherBp;
				strongest = other;
			}
		}

		int counted = Math.min(partners, BudokaiConfig.PARTNER_MAX_COUNTED.get());
		double partnerMult = 1.0 + counted * BudokaiConfig.PARTNER_BONUS_PER_PLAYER.get();

		double leechMult = 1.0;
		ServerPlayer leechSource = null;
		if (BudokaiConfig.LEECH_ENABLED.get() && strongest != null) {
			double ratio = strongestBp / myBp;
			if (ratio >= BudokaiConfig.LEECH_MIN_RATIO.get()) {
				double log2 = Math.log(ratio) / Math.log(2.0);
				leechMult = Math.min(BudokaiConfig.LEECH_MAX_MULTIPLIER.get(), 1.0 + BudokaiConfig.LEECH_STRENGTH.get() * log2);
				leechSource = strongest;
			}
		}

		double mentorMult = data.getMentoringUntilTick() >= now ? BudokaiConfig.MENTOR_MULTIPLIER.get() : 1.0;

		double angelMult = 1.0;
		if (bestAngelRank > 0) {
			angelMult *= 1.0 + bestAngelRank * BudokaiConfig.ANGEL_MENTOR_BONUS_PER_RANK.get();
		}
		if (activity == TrainingActivity.MEDITATION && data.isAngel()) {
			angelMult *= 1.0 + data.getAngelRank() * BudokaiConfig.ANGEL_MEDITATION_BONUS_PER_RANK.get();
		}

		return new TrainingBonus(counted, partnerMult, leechMult, leechSource, mentorMult, angelMult);
	}
}

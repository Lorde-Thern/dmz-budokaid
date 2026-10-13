package com.dmzbudokai.training;

import com.dmzbudokai.angel.AngelAbilities;
import com.dmzbudokai.angel.AngelPath;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
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
		double bestGuidance = 0.0;
		List<ServerPlayer> guidingAngels = new ArrayList<>();
		int guidanceRank = AngelAbilities.Ability.ANGELIC_GUIDANCE.rank();

		for (ServerPlayer other : player.serverLevel().players()) {
			if (other == player || other.isSpectator() || !other.isAlive()) continue;
			if (other.distanceToSqr(player) > radiusSq) continue;

			Optional<TrainingData> otherTraining = TrainingDataProvider.get(other);
			if (otherTraining.isEmpty()) continue;

			// Angelic Guidance: an angel only has to be watching, not training themselves.
			TrainingData otherData = otherTraining.get();
			if (AngelPath.hasRank(otherData, guidanceRank)) {
				guidingAngels.add(other);
				bestGuidance = Math.max(bestGuidance,
						otherData.getAngelRank() * BudokaiConfig.ANGEL_MENTOR_BONUS_PER_RANK.get() * AngelPath.perkStrength(otherData));
			}

			if (!isActivelyTraining(otherData, now)) continue;

			Optional<StatsData> otherStats = DmzBridge.stats(other);
			if (otherStats.isEmpty() || !DmzBridge.hasCharacter(otherStats.get())) continue;

			partners++;

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

		double angelMult = 1.0 + bestGuidance;
		boolean blessed = data.angel().isBlessed();
		angelMult *= data.angel().getBlessingMultiplier();
		if (activity == TrainingActivity.MEDITATION && AngelPath.hasRank(data, 1)) {
			angelMult *= 1.0 + data.getAngelRank() * BudokaiConfig.ANGEL_MEDITATION_BONUS_PER_RANK.get() * AngelPath.perkStrength(data);
		}

		return new TrainingBonus(counted, partnerMult, leechMult, leechSource, mentorMult, angelMult, guidingAngels, blessed);
	}
}

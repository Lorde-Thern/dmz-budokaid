package com.dmzbudokai.training;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Multipliers for one training rep.
 *
 * @param partners     number of nearby partners that counted toward the partner bonus
 * @param partnerMult  bonus from training with others
 * @param leechMult    bonus from training next to someone much stronger
 * @param leechSource  the strongest nearby partner when leeching, otherwise null
 * @param mentorMult   bonus for being leeched from (helping weaker players)
 * @param angelMult    bonus from angels: Angelic Guidance nearby, an Angel's Blessing, or an angel's own meditation
 * @param guidingAngels angels close enough to be teaching this rep (they earn mentor credit)
 * @param blessed      whether an Angel's Blessing is active
 */
public record TrainingBonus(int partners, double partnerMult, double leechMult, @Nullable ServerPlayer leechSource,
							double mentorMult, double angelMult, List<ServerPlayer> guidingAngels, boolean blessed) {

	public static final TrainingBonus NONE = new TrainingBonus(0, 1.0, 1.0, null, 1.0, 1.0, List.of(), false);

	public double total() {
		return partnerMult * leechMult * mentorMult * angelMult;
	}

	public boolean isLeeching() {
		return leechSource != null && leechMult > 1.0;
	}
}

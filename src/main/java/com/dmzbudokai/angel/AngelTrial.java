package com.dmzbudokai.angel;

import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;

/**
 * The trial each rank-up needs on top of Divine XP and Battle Power.
 * Progress is kept until you rank up (or fall), so trials can be worked on at your own pace.
 */
public enum AngelTrial {
	/** To Attendant Angel: meditate without breaking for several minutes. Angels are calm above all. */
	STILLNESS(2, "stillness"),
	/** To Guardian Angel: guide others. Counts training reps done by other players near you. */
	MENTOR(3, "mentor"),
	/** To Grand Angel: move without thinking. Counts attacks dodged with Angelic Instinct. */
	INSTINCT(4, "instinct");

	private final int forRank;
	private final String id;

	AngelTrial(int forRank, String id) {
		this.forRank = forRank;
		this.id = id;
	}

	public int forRank() { return forRank; }

	public String nameKey() { return "dmzbudokai.angel.trial." + id; }

	public String descKey() { return "dmzbudokai.angel.trial." + id + ".desc"; }

	/** The trial needed to reach {@code rank}, or null if that rank has none. */
	public static AngelTrial forRank(int rank) {
		for (AngelTrial trial : values()) {
			if (trial.forRank == rank) return trial;
		}
		return null;
	}

	public int progress(TrainingData data) {
		AngelState angel = data.angel();
		return switch (this) {
			case STILLNESS -> Math.max(angel.getBestMeditationTicks(), angel.getMeditationStreakTicks()) / 20; // seconds
			case MENTOR -> angel.getMentorReps();
			case INSTINCT -> angel.getInstinctDodges();
		};
	}

	public int goal() {
		return switch (this) {
			case STILLNESS -> BudokaiConfig.ANGEL_TRIAL_STILLNESS_MINUTES.get() * 60;
			case MENTOR -> BudokaiConfig.ANGEL_TRIAL_MENTOR_REPS.get();
			case INSTINCT -> BudokaiConfig.ANGEL_TRIAL_INSTINCT_DODGES.get();
		};
	}

	public boolean isComplete(TrainingData data) {
		return progress(data) >= goal();
	}
}

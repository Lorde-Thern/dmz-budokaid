package com.dmzbudokai.training;

import com.dmzbudokai.config.BudokaiConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * A way to train. Each activity trains a pair of DMZ stats (keys match DMZ's {@code Stats.addStat}).
 */
public enum TrainingActivity {
	PUNCHING_BAG("punching_bag", BudokaiConfig.PUNCHING_BAG_XP, BudokaiConfig.PUNCHING_BAG_STAMINA, "str", "skp"),
	WEIGHTS("weights", BudokaiConfig.WEIGHTS_XP, BudokaiConfig.WEIGHTS_STAMINA, "res", "vit"),
	MEDITATION("meditation", BudokaiConfig.MEDITATION_XP, null, "pwr", "ene"),
	COMBAT_HIT("combat_hit", BudokaiConfig.COMBAT_HIT_XP, BudokaiConfig.COMBAT_STAMINA, "str", "skp"),
	COMBAT_HURT("combat_hurt", BudokaiConfig.COMBAT_HURT_XP, null, "res", "vit"),
	SPRINT("sprint", BudokaiConfig.SPRINT_XP, null, "vit", "ene");

	private final String id;
	private final ForgeConfigSpec.DoubleValue xp;
	private final ForgeConfigSpec.DoubleValue stamina;
	private final String[] stats;

	TrainingActivity(String id, ForgeConfigSpec.DoubleValue xp, ForgeConfigSpec.DoubleValue stamina, String... stats) {
		this.id = id;
		this.xp = xp;
		this.stamina = stamina;
		this.stats = stats;
	}

	public String id() { return id; }

	public double baseXp() { return xp.get(); }

	public double staminaCost() { return stamina == null ? 0.0 : stamina.get(); }

	public String[] stats() { return stats; }

	public String translationKey() { return "dmzbudokai.activity." + id; }
}

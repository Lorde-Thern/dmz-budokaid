package com.dmzbudokai.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Server config, stored per world in {@code <world>/serverconfig/dmzbudokai-server.toml}.
 * Values are read live, so edits apply after a reload without restarting.
 */
public final class BudokaiConfig {
	public static final ForgeConfigSpec SPEC;

	// General
	public static final ForgeConfigSpec.BooleanValue DISABLE_TRAINING_POINTS;
	public static final ForgeConfigSpec.DoubleValue GLOBAL_GAIN_MULTIPLIER;
	public static final ForgeConfigSpec.IntValue HUD_SYNC_INTERVAL;

	// Stat growth
	public static final ForgeConfigSpec.DoubleValue STAT_COST_BASE;
	public static final ForgeConfigSpec.DoubleValue STAT_COST_PER_POINT;
	public static final ForgeConfigSpec.IntValue MAX_POINTS_PER_REP;
	public static final ForgeConfigSpec.DoubleValue STAT_XP_GROWTH_PER_POINT;

	// Transformations
	public static final ForgeConfigSpec.BooleanValue BP_FORM_UNLOCKS;
	public static final ForgeConfigSpec.IntValue RECOMMENDED_DMZ_MAX_VALUE;

	// Activities
	public static final ForgeConfigSpec.DoubleValue PUNCHING_BAG_XP;
	public static final ForgeConfigSpec.IntValue PUNCHING_BAG_COOLDOWN;
	public static final ForgeConfigSpec.DoubleValue PUNCHING_BAG_STAMINA;
	public static final ForgeConfigSpec.DoubleValue WEIGHTS_XP;
	public static final ForgeConfigSpec.IntValue WEIGHTS_REP_TICKS;
	public static final ForgeConfigSpec.DoubleValue WEIGHTS_STAMINA;
	public static final ForgeConfigSpec.DoubleValue MEDITATION_XP;
	public static final ForgeConfigSpec.IntValue MEDITATION_REP_TICKS;
	public static final ForgeConfigSpec.DoubleValue COMBAT_HIT_XP;
	public static final ForgeConfigSpec.DoubleValue COMBAT_HURT_XP;
	public static final ForgeConfigSpec.IntValue COMBAT_COOLDOWN;
	public static final ForgeConfigSpec.DoubleValue COMBAT_STAMINA;
	public static final ForgeConfigSpec.DoubleValue SPRINT_XP;
	public static final ForgeConfigSpec.DoubleValue SPRINT_BLOCKS_PER_REP;

	// Partner training
	public static final ForgeConfigSpec.DoubleValue PARTNER_RADIUS;
	public static final ForgeConfigSpec.IntValue PARTNER_ACTIVE_TICKS;
	public static final ForgeConfigSpec.DoubleValue PARTNER_BONUS_PER_PLAYER;
	public static final ForgeConfigSpec.IntValue PARTNER_MAX_COUNTED;

	// Leeching
	public static final ForgeConfigSpec.BooleanValue LEECH_ENABLED;
	public static final ForgeConfigSpec.DoubleValue LEECH_MIN_RATIO;
	public static final ForgeConfigSpec.DoubleValue LEECH_STRENGTH;
	public static final ForgeConfigSpec.DoubleValue LEECH_MAX_MULTIPLIER;
	public static final ForgeConfigSpec.DoubleValue MENTOR_MULTIPLIER;

	// Angel Path
	public static final ForgeConfigSpec.BooleanValue ANGEL_ENABLED;
	public static final ForgeConfigSpec.DoubleValue ANGEL_JOIN_BP;
	public static final ForgeConfigSpec.IntValue ANGEL_JOIN_ALIGNMENT;
	public static final ForgeConfigSpec.IntValue ANGEL_FALL_ALIGNMENT;
	public static final ForgeConfigSpec.IntValue ANGEL_REJOIN_COOLDOWN_MINUTES;
	public static final ForgeConfigSpec.ConfigValue<List<? extends Number>> ANGEL_RANK_DIVINE_XP;
	public static final ForgeConfigSpec.ConfigValue<List<? extends Number>> ANGEL_RANK_BP;
	public static final ForgeConfigSpec.DoubleValue ANGEL_MEDITATION_BONUS_PER_RANK;
	public static final ForgeConfigSpec.DoubleValue ANGEL_MENTOR_BONUS_PER_RANK;
	public static final ForgeConfigSpec.DoubleValue ANGEL_DODGE_CHANCE_PER_RANK;
	public static final ForgeConfigSpec.IntValue ANGEL_DODGE_COOLDOWN;
	public static final ForgeConfigSpec.DoubleValue ANGEL_SPEED_PER_RANK;
	public static final ForgeConfigSpec.BooleanValue ANGEL_FORCE_HALO;

	// Dragon Block Noea compatibility
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NOEA_MOD_IDS;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ANGEL_FORBIDDEN_RACES;

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

		b.push("general");
		DISABLE_TRAINING_POINTS = b
				.comment("Cancel every Dragon Mine Z Training Point gain so stats only grow through training.",
						"Set to false to keep TP as well (training then stacks on top of the normal DMZ system).")
				.define("disableTrainingPoints", true);
		GLOBAL_GAIN_MULTIPLIER = b
				.comment("Multiplies every training gain. Raise it for a faster server, lower it for a grindier one.")
				.defineInRange("globalGainMultiplier", 1.0, 0.0, 1000.0);
		HUD_SYNC_INTERVAL = b
				.comment("How often (in ticks) the training HUD is refreshed while a player is training.")
				.defineInRange("hudSyncInterval", 10, 1, 200);
		b.pop();

		b.push("statGrowth");
		STAT_COST_BASE = b
				.comment("Training XP needed for the first point in a stat.")
				.defineInRange("statCostBase", 12.5, 0.01, 1.0E9);
		STAT_COST_PER_POINT = b
				.comment("Extra training XP needed per point you already have in that stat.",
						"cost = statCostBase + currentStat * statCostPerPoint")
				.defineInRange("statCostPerPoint", 0.0625, 0.0, 1.0E6);
		MAX_POINTS_PER_REP = b
				.comment("Safety cap on how many points one rep can give a single stat.")
				.defineInRange("maxPointsPerRep", 50, 1, 100000);
		STAT_XP_GROWTH_PER_POINT = b
				.comment("XP per rep grows with the stat being trained: xp *= 1 + currentStat * statXpGrowthPerPoint.",
						"Keeps late-game training worthwhile so millions of BP are reachable (Xeno-game style).",
						"With the defaults, high stats settle at about 1 point per rep. 0 = flat gains (much grindier).")
				.defineInRange("statXpGrowthPerPoint", 0.0125, 0.0, 1000.0);
		b.pop();

		b.push("transformations");
		BP_FORM_UNLOCKS = b
				.comment("Unlock DMZ transformations by reaching Battle Power thresholds.",
						"Thresholds live in config/dmzbudokai/form_unlocks.json (realistic defaults: Super Saiyan at 1M base BP).",
						"Works with every race DMZ loads, including custom races: unlisted forms get a BP from their unlock level.")
				.define("bpFormUnlocks", true);
		RECOMMENDED_DMZ_MAX_VALUE = b
				.comment("DMZ's maxValue (general-server.json) caps total stats at maxValue * 6, which caps Battle Power.",
						"Roughly: 10000 (DMZ default) = 1.6M base BP, 250000 = 78M, 1000000 = 414M, 4000000 = 2.2B.",
						"If DMZ's value is below this number, the server log and /budokai forms warn that higher",
						"forms can't be reached. 0 = never warn.")
				.defineInRange("recommendedDmzMaxValue", 1000000, 0, Integer.MAX_VALUE);
		b.pop();

		b.push("activities");
		PUNCHING_BAG_XP = b.comment("XP per punch on a Punching Bag (Strength + Strike Power).")
				.defineInRange("punchingBagXp", 4.0, 0.0, 1.0E6);
		PUNCHING_BAG_COOLDOWN = b.comment("Ticks between punches that count.")
				.defineInRange("punchingBagCooldown", 8, 1, 200);
		PUNCHING_BAG_STAMINA = b.comment("DMZ stamina used per punch. Too tired = no gains.")
				.defineInRange("punchingBagStamina", 2.0, 0.0, 1.0E6);
		WEIGHTS_XP = b.comment("XP per lift with Training Weights (Resistance + Vitality).")
				.defineInRange("weightsXp", 5.0, 0.0, 1.0E6);
		WEIGHTS_REP_TICKS = b.comment("Ticks of holding right-click per lift.")
				.defineInRange("weightsRepTicks", 20, 1, 1200);
		WEIGHTS_STAMINA = b.comment("DMZ stamina used per lift.")
				.defineInRange("weightsStamina", 3.0, 0.0, 1.0E6);
		MEDITATION_XP = b.comment("XP per meditation cycle (Ki Power + Energy). Meditate by sneaking without moving.")
				.defineInRange("meditationXp", 3.0, 0.0, 1.0E6);
		MEDITATION_REP_TICKS = b.comment("Ticks of still sneaking per meditation cycle.")
				.defineInRange("meditationRepTicks", 40, 1, 1200);
		COMBAT_HIT_XP = b.comment("XP for landing a hit on a mob or player (Strength + Strike Power).")
				.defineInRange("combatHitXp", 3.0, 0.0, 1.0E6);
		COMBAT_HURT_XP = b.comment("XP for taking a hit from a mob or player (Resistance + Vitality).")
				.defineInRange("combatHurtXp", 2.0, 0.0, 1.0E6);
		COMBAT_COOLDOWN = b.comment("Minimum ticks between combat reps (stops fast-hit spam).")
				.defineInRange("combatCooldown", 10, 0, 200);
		COMBAT_STAMINA = b.comment("DMZ stamina used per combat rep.")
				.defineInRange("combatStamina", 1.0, 0.0, 1.0E6);
		SPRINT_XP = b.comment("XP per sprint rep (Vitality + Energy).")
				.defineInRange("sprintXp", 2.0, 0.0, 1.0E6);
		SPRINT_BLOCKS_PER_REP = b.comment("Blocks sprinted per sprint rep.")
				.defineInRange("sprintBlocksPerRep", 40.0, 1.0, 10000.0);
		b.pop();

		b.push("partnerTraining");
		PARTNER_RADIUS = b.comment("How close (blocks) another trainee must be to count as your partner.")
				.defineInRange("partnerRadius", 12.0, 1.0, 128.0);
		PARTNER_ACTIVE_TICKS = b.comment("A player counts as 'training' for this many ticks after their last rep.")
				.defineInRange("partnerActiveTicks", 100, 1, 6000);
		PARTNER_BONUS_PER_PLAYER = b.comment("Extra gain per training partner. 0.25 = +25% each.")
				.defineInRange("partnerBonusPerPlayer", 0.25, 0.0, 100.0);
		PARTNER_MAX_COUNTED = b.comment("Most partners that can count toward the bonus.")
				.defineInRange("partnerMaxCounted", 4, 0, 100);
		b.pop();

		b.push("leeching");
		LEECH_ENABLED = b.comment("Training next to someone much stronger gives you a leech bonus.")
				.define("leechEnabled", true);
		LEECH_MIN_RATIO = b.comment("Partner BP must be at least this many times yours to leech.")
				.defineInRange("leechMinRatio", 2.0, 1.0, 1.0E9);
		LEECH_STRENGTH = b.comment("Leech multiplier = 1 + leechStrength * log2(partnerBP / yourBP).")
				.defineInRange("leechStrength", 0.5, 0.0, 100.0);
		LEECH_MAX_MULTIPLIER = b.comment("Cap on the leech multiplier.")
				.defineInRange("leechMaxMultiplier", 3.0, 1.0, 1000.0);
		MENTOR_MULTIPLIER = b.comment("Gain multiplier for the stronger player while someone is leeching from them.",
						"Gives strong players a reason to help weaker ones. 1.0 = no bonus.")
				.defineInRange("mentorMultiplier", 1.1, 1.0, 100.0);
		b.pop();

		b.push("angelPath");
		ANGEL_ENABLED = b.comment("Allow players to walk the Angel Path.")
				.define("angelEnabled", true);
		ANGEL_JOIN_BP = b.comment("Base Battle Power needed to start the Angel Path.")
				.defineInRange("angelJoinBp", 5000.0, 0.0, Double.MAX_VALUE);
		ANGEL_JOIN_ALIGNMENT = b.comment("DMZ alignment (0-100) needed to start the Angel Path.")
				.defineInRange("angelJoinAlignment", 75, 0, 100);
		ANGEL_FALL_ALIGNMENT = b.comment("If your alignment drops below this you fall from the path.")
				.defineInRange("angelFallAlignment", 40, 0, 100);
		ANGEL_REJOIN_COOLDOWN_MINUTES = b.comment("Real-time minutes before a fallen or departed angel can rejoin.")
				.defineInRange("angelRejoinCooldownMinutes", 60, 0, 100000);
		ANGEL_RANK_DIVINE_XP = b.comment("Divine XP needed for ranks 2, 3, 4 ... (rank 1 is given on joining).")
				.defineList("angelRankDivineXp", List.<Number>of(500.0, 2500.0, 10000.0), o -> o instanceof Number n && n.doubleValue() >= 0);
		ANGEL_RANK_BP = b.comment("Base Battle Power needed for ranks 2, 3, 4 ... (same order as angelRankDivineXp).")
				.defineList("angelRankBp", List.<Number>of(50000.0, 500000.0, 5000000.0), o -> o instanceof Number n && n.doubleValue() >= 0);
		ANGEL_MEDITATION_BONUS_PER_RANK = b.comment("Extra meditation gains per Angel rank.")
				.defineInRange("angelMeditationBonusPerRank", 0.25, 0.0, 100.0);
		ANGEL_MENTOR_BONUS_PER_RANK = b.comment("Extra gains per Angel rank given to anyone training next to an angel.")
				.defineInRange("angelMentorBonusPerRank", 0.10, 0.0, 100.0);
		ANGEL_DODGE_CHANCE_PER_RANK = b.comment("'Angelic Instinct': chance per rank to fully dodge an attack.")
				.defineInRange("angelDodgeChancePerRank", 0.05, 0.0, 1.0);
		ANGEL_DODGE_COOLDOWN = b.comment("Ticks between Angelic Instinct dodges.")
				.defineInRange("angelDodgeCooldown", 60, 0, 12000);
		ANGEL_SPEED_PER_RANK = b.comment("Movement speed bonus per rank (0.05 = +5%).")
				.defineInRange("angelSpeedPerRank", 0.05, 0.0, 10.0);
		ANGEL_FORCE_HALO = b.comment("Show the DMZ halo on players walking the Angel Path.")
				.define("angelForceHalo", true);
		b.pop();

		b.push("noeaCompat");
		NOEA_MOD_IDS = b.comment("Mod ids that identify Dragon Block Noea. Only used to report whether Noea is installed.")
				.defineListAllowEmpty(List.of("noeaModIds"), () -> List.of("noea", "dragonblocknoea", "dbnoea"), o -> o instanceof String);
		ANGEL_FORBIDDEN_RACES = b.comment("DMZ race names (lowercase) that can never walk the Angel Path.",
						"Defaults cover Noea's demon race. Add or remove names to match your modpack.")
				.defineListAllowEmpty(List.of("angelForbiddenRaces"), () -> List.of("demon", "glind"), o -> o instanceof String);
		b.pop();

		SPEC = b.build();
	}

	private BudokaiConfig() {
	}
}

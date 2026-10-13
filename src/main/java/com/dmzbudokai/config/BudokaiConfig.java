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

	// Battle Power
	public static final ForgeConfigSpec.BooleanValue REALISTIC_BATTLE_POWER;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FORM_BP_MULTIPLIERS;
	public static final ForgeConfigSpec.DoubleValue FORM_BP_FALLBACK_EXPONENT;
	public static final ForgeConfigSpec.BooleanValue REALISTIC_MOB_BATTLE_POWER;
	public static final ForgeConfigSpec.DoubleValue MOB_BP_MULTIPLIER;

	// Planets
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLANET_GRAVITY;
	public static final ForgeConfigSpec.BooleanValue GRAVITY_TRAINING_BONUS;

	// Limitless
	public static final ForgeConfigSpec.BooleanValue LIMITLESS_MODE;

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
	public static final ForgeConfigSpec.BooleanValue ANGEL_REQUIRE_MENTOR;
	public static final ForgeConfigSpec.ConfigValue<String> ANGEL_MENTOR_ENTITY;
	public static final ForgeConfigSpec.IntValue ANGEL_TRIAL_STILLNESS_MINUTES;
	public static final ForgeConfigSpec.IntValue ANGEL_TRIAL_MENTOR_REPS;
	public static final ForgeConfigSpec.IntValue ANGEL_TRIAL_INSTINCT_DODGES;
	public static final ForgeConfigSpec.DoubleValue ANGEL_DISFAVOR_THRESHOLD;
	public static final ForgeConfigSpec.DoubleValue ANGEL_FAVOR_PENALTY_INNOCENT;
	public static final ForgeConfigSpec.DoubleValue ANGEL_FAVOR_PENALTY_BULLY;
	public static final ForgeConfigSpec.DoubleValue ANGEL_BULLY_BP_RATIO;
	public static final ForgeConfigSpec.DoubleValue ANGEL_FAVOR_PER_MEDITATION;
	public static final ForgeConfigSpec.DoubleValue ANGEL_FAVOR_PER_MENTOR_REP;
	public static final ForgeConfigSpec.IntValue ANGEL_BLESS_RANK;
	public static final ForgeConfigSpec.IntValue ANGEL_BLESS_DURATION_SECONDS;
	public static final ForgeConfigSpec.IntValue ANGEL_BLESS_COOLDOWN_SECONDS;
	public static final ForgeConfigSpec.DoubleValue ANGEL_BLESS_BONUS_PER_RANK;
	public static final ForgeConfigSpec.IntValue ANGEL_REWIND_RANK;
	public static final ForgeConfigSpec.IntValue ANGEL_REWIND_SECONDS;
	public static final ForgeConfigSpec.IntValue ANGEL_REWIND_COOLDOWN_MINUTES;
	public static final ForgeConfigSpec.IntValue ANGEL_PRESENCE_RANK;
	public static final ForgeConfigSpec.DoubleValue ANGEL_PRESENCE_RADIUS;
	public static final ForgeConfigSpec.IntValue ANGEL_AUTONOMOUS_RANK;
	public static final ForgeConfigSpec.DoubleValue ANGEL_AUTONOMOUS_HEALTH;

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

		b.push("battlePower");
		REALISTIC_BATTLE_POWER = b
				.comment("Dragon Ball-style Battle Power: transformations multiply your BP like in the show",
						"(Super Saiyan x50, SSJ2 x100, SSJ3 x400, Kaioken xN...) instead of DMZ's small stat boost.",
						"Shown everywhere DMZ shows BP (scouter, stats screen, ki sense). BP is display/comparison only:",
						"damage still comes from DMZ's stats. Form unlocks, leeching and the Angel Path always use your",
						"trained base BP, so transforming never counts toward them.")
				.define("realisticBattlePower", true);
		FORM_BP_MULTIPLIERS = b
				.comment("BP multiplier per form: \"<race>/<group>/<form>=<multiplier>\". Race \"*\" matches every race",
						"(use it for stack forms like Kaioken). Forms not listed use their DMZ stat multiplier raised to",
						"formBpFallbackExponent, so custom races' forms still scale. Use /budokai forms for exact names.")
				.defineListAllowEmpty(List.of("formBpMultipliers"), BudokaiConfig::defaultFormBpMultipliers, o -> o instanceof String);
		FORM_BP_FALLBACK_EXPONENT = b
				.comment("For forms not in formBpMultipliers: BP multiplier = (average DMZ form stat multiplier) ^ this.",
						"5.5 turns DMZ's x1.5 into about x9, x2.25 into about x90 and x3 into about x400.")
				.defineInRange("formBpFallbackExponent", 5.5, 1.0, 20.0);
		REALISTIC_MOB_BATTLE_POWER = b
				.comment("Put mob Battle Power on the same realistic scale (needs realisticBattlePower).",
						"A mob's health and damage are matched to a player of equal strength and run through the",
						"player BP curve: a zombie reads ~170 next to a fresh character's ~230, the Ender Dragon ~2,700.",
						"Story NPCs (Goku, Vegeta, Frieza, Cell...) keep DMZ's canon numbers. Display only, like player BP.")
				.define("realisticMobBattlePower", true);
		MOB_BP_MULTIPLIER = b
				.comment("Multiplies every converted mob BP. Raise it if mobs read too weak for your modpack.")
				.defineInRange("mobBpMultiplier", 1.0, 0.0, 1.0E9);
		b.pop();

		b.push("planets");
		PLANET_GRAVITY = b
				.comment("Gravity for custom planets, as \"<dimension>=<gravity>\" (1.0 = normal). Applied through Dragon Mine Z's",
						"gravity system, so it slows movement and boosts training. A gravityPerWorld entry for the same",
						"dimension in DMZ's general-server.json takes priority.")
				.defineListAllowEmpty(List.of("planetGravity"), () -> List.of("dmzbudokai:planet_vegeta=10"), o -> o instanceof String);
		GRAVITY_TRAINING_BONUS = b
				.comment("Training under high gravity (planets like Vegeta, DMZ gravity rooms, King Kai) gives bonus gains,",
						"using DMZ's own gravity training multiplier (which normally only boosts Training Points).")
				.define("gravityTrainingBonus", true);
		b.pop();

		b.push("limitless");
		LIMITLESS_MODE = b
				.comment("Remove every ceiling on growth: DMZ's total stat cap (maxValue * 6) and per-stat cap are ignored",
						"for training gains, so you can keep getting stronger forever. Gains are NOT boosted; training",
						"works exactly the same, it just never stops. (Stats are whole numbers, so one stat tops out at",
						"2,147,483,647, which is far beyond anything reachable.)")
				.define("limitlessMode", false);
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
		ANGEL_REJOIN_COOLDOWN_MINUTES = b.comment("Real-time minutes before a fallen or departed angel can rejoin.",
						"Falling multiplies it by how many times you've fallen.")
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

		b.comment("Initiation: an angel must accept you as their pupil before you can begin.").push("initiation");
		ANGEL_REQUIRE_MENTOR = b.comment("Require talking to (right-clicking) the mentor NPC before joining.")
				.define("requireMentor", true);
		ANGEL_MENTOR_ENTITY = b.comment("Entity id of the angel who accepts pupils. Defaults to DMZ's Whis.")
				.define("mentorEntity", "dragonminez:master_whis");
		b.pop();

		b.comment("Trials: each rank-up past Angel Trainee also needs its trial completed.").push("trials");
		ANGEL_TRIAL_STILLNESS_MINUTES = b.comment("Trial of Stillness (to Attendant Angel): minutes of unbroken meditation.")
				.defineInRange("stillnessMinutes", 5, 0, 1440);
		ANGEL_TRIAL_MENTOR_REPS = b.comment("Trial of the Mentor (to Guardian Angel): training reps done by others near you.")
				.defineInRange("mentorReps", 500, 0, 10_000_000);
		ANGEL_TRIAL_INSTINCT_DODGES = b.comment("Trial of Instinct (to Grand Angel): attacks dodged with Angelic Instinct.")
				.defineInRange("instinctDodges", 40, 0, 1_000_000);
		b.pop();

		b.comment("Divine Favor (0-100): angels must stay neutral and protect the innocent.").push("divineFavor");
		ANGEL_DISFAVOR_THRESHOLD = b.comment("Below this favor you are Disfavored: every Angel perk works at half strength.",
						"At 0 favor you fall from the path.")
				.defineInRange("disfavorThreshold", 50.0, 0.0, 100.0);
		ANGEL_FAVOR_PENALTY_INNOCENT = b.comment("Favor lost for killing a villager, wandering trader or iron golem.")
				.defineInRange("penaltyInnocent", 25.0, 0.0, 100.0);
		ANGEL_FAVOR_PENALTY_BULLY = b.comment("Favor lost for killing a player far weaker than you.")
				.defineInRange("penaltyBully", 20.0, 0.0, 100.0);
		ANGEL_BULLY_BP_RATIO = b.comment("A player counts as 'far weaker' when your trained BP is this many times theirs.")
				.defineInRange("bullyBpRatio", 10.0, 1.0, 1.0E9);
		ANGEL_FAVOR_PER_MEDITATION = b.comment("Favor restored per meditation rep.")
				.defineInRange("favorPerMeditation", 0.25, 0.0, 100.0);
		ANGEL_FAVOR_PER_MENTOR_REP = b.comment("Favor restored per training rep someone does near you.")
				.defineInRange("favorPerMentorRep", 0.1, 0.0, 100.0);
		b.pop();

		b.comment("Abilities unlocked by rank.").push("abilities");
		ANGEL_BLESS_RANK = b.comment("Angel's Blessing: rank needed to bless other players with the Angel Staff.")
				.defineInRange("blessRank", 2, 1, 99);
		ANGEL_BLESS_DURATION_SECONDS = b.comment("How long a blessing lasts.")
				.defineInRange("blessDurationSeconds", 300, 1, 86400);
		ANGEL_BLESS_COOLDOWN_SECONDS = b.comment("Cooldown between blessings.")
				.defineInRange("blessCooldownSeconds", 600, 0, 86400);
		ANGEL_BLESS_BONUS_PER_RANK = b.comment("Training gain bonus a blessing gives, per rank of the angel who gave it.")
				.defineInRange("blessBonusPerRank", 0.15, 0.0, 100.0);
		ANGEL_REWIND_RANK = b.comment("Temporal Do-Over: rank needed. When you would die, time rewinds a few seconds instead.")
				.defineInRange("rewindRank", 3, 1, 99);
		ANGEL_REWIND_SECONDS = b.comment("How far back Temporal Do-Over rewinds.")
				.defineInRange("rewindSeconds", 3, 1, 10);
		ANGEL_REWIND_COOLDOWN_MINUTES = b.comment("Cooldown for Temporal Do-Over (real-time minutes).")
				.defineInRange("rewindCooldownMinutes", 10, 0, 10080);
		ANGEL_PRESENCE_RANK = b.comment("Divine Presence: rank needed. Hostile mobs near you are slowed and weakened.")
				.defineInRange("presenceRank", 4, 1, 99);
		ANGEL_PRESENCE_RADIUS = b.comment("Divine Presence radius in blocks.")
				.defineInRange("presenceRadius", 8.0, 1.0, 64.0);
		ANGEL_AUTONOMOUS_RANK = b.comment("Autonomous Instinct: rank needed. Angelic Instinct dodge chance doubles at low health.")
				.defineInRange("autonomousRank", 4, 1, 99);
		ANGEL_AUTONOMOUS_HEALTH = b.comment("Health fraction that counts as 'low' for Autonomous Instinct.")
				.defineInRange("autonomousHealth", 0.3, 0.0, 1.0);
		b.pop();
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

	private static List<String> defaultFormBpMultipliers() {
		return List.of(
				// Saiyan
				"saiyan/oozaru/oozaru=10", "saiyan/oozaru/goldenoozaru=500", "saiyan/oozaru/supersaiyan4=4000",
				"saiyan/ssgrades/supersaiyan=50", "saiyan/ssgrades/supersaiyangrade2=60", "saiyan/ssgrades/supersaiyangrade3=75",
				"saiyan/supersaiyan/supersaiyanmastered=50", "saiyan/supersaiyan/supersaiyan2=100",
				"saiyan/supersaiyan/supersaiyan3=400", "saiyan/supersaiyan/supersaiyan4=4000",
				"saiyan/legendaryforms/ikari=20", "saiyan/legendaryforms/ssjhybrid=60", "saiyan/legendaryforms/ssjfullpower=150",
				// Human
				"human/superforms/buffed=2", "human/superforms/fullpower=5", "human/superforms/overdrive=20",
				"human/superforms/solaris=60",
				"human/legendaryforms/shiyoken=10", "human/legendaryforms/shin_shiyoken=40", "human/legendaryforms/chou_shiyoken=150",
				"human/androidforms/superandroid=10", "human/androidforms/fusedandroid=40",
				// Namekian
				"namekian/superforms/giant=2", "namekian/superforms/fullpower=5", "namekian/superforms/supernamekian=50",
				"namekian/legendaryforms/evilnamek=10", "namekian/legendaryforms/evilgiantnamek=20",
				"namekian/legendaryforms/buffednamek=60",
				// Frost Demon
				"frostdemon/evolutionforms/second=2", "frostdemon/evolutionforms/third=3", "frostdemon/evolutionforms/final=4",
				"frostdemon/evolutionforms/fullpower=10", "frostdemon/evolutionforms/fifth=50",
				"frostdemon/legendaryforms/mecha=5", "frostdemon/legendaryforms/metal=20", "frostdemon/legendaryforms/metalcore=80",
				// Majin
				"majin/pureforms/kid=5", "majin/pureforms/evil=3", "majin/pureforms/super=15", "majin/pureforms/ultra=50",
				"majin/legendaryforms/innocencedemon=10", "majin/legendaryforms/giantinnocencedemon=20",
				"majin/legendaryforms/superdemon=60",
				// Bio-Android
				"bioandroid/bioevolution/semiperfect=3", "bioandroid/bioevolution/perfect=6",
				"bioandroid/bioevolution/superperfect=15", "bioandroid/bioevolution/ultraperfect=50",
				"bioandroid/legendaryforms/xeno=10", "bioandroid/legendaryforms/xenofp=25", "bioandroid/legendaryforms/xenomax=60",
				// Stack forms (any race)
				"*/kaioken/x2=2", "*/kaioken/x3=3", "*/kaioken/x4=4", "*/kaioken/x10=10", "*/kaioken/x20=20", "*/kaioken/x100=100",
				"*/ultimate/ultimate=100",
				"*/ultrainstinct/sign=1000", "*/ultrainstinct/mastered=5000",
				"*/ultraego/sign=1000", "*/ultraego/mastered=5000");
	}

	private BudokaiConfig() {
	}
}

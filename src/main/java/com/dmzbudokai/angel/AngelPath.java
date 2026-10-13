package com.dmzbudokai.angel;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.compat.NoeaCompat;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dmzbudokai.registry.BudokaiItems;
import com.dmzbudokai.training.TrainingActivity;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The Angel Path: an opt-in progression path inspired by the angels of Dragon Ball Super (Whis, Vados).
 * <p>
 * <b>Initiation.</b> Whis must accept you as a pupil (right-click him), and you need a pure heart
 * (DMZ alignment) and enough Battle Power. Then {@code /budokai angel join}.
 * <p>
 * <b>Ranks.</b> 1 Angel Trainee, 2 Attendant Angel, 3 Guardian Angel, 4 Grand Angel. Each rank-up needs
 * Divine XP (from training), trained Battle Power and that rank's {@link AngelTrial}.
 * <p>
 * <b>Abilities</b> unlock by rank (see {@link AngelAbilities}): halo, divine meditation, Angelic Instinct,
 * Angelic Guidance, Angel's Blessing (with the Angel Staff), Temporal Do-Over, Divine Presence and
 * Autonomous Instinct.
 * <p>
 * <b>The Angel Code.</b> Angels stay neutral and protect the innocent. Divine Favor (0-100) drops when
 * you kill innocents or far weaker fighters and recovers through meditation and teaching. Below the
 * disfavor threshold every perk works at half strength; at 0 favor, or if your alignment sinks too low,
 * you fall. Each fall makes the wait to rejoin longer.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class AngelPath {
	private static final UUID SPEED_MODIFIER_ID = UUID.fromString("5f0d8b8e-3f7a-4d0e-9b1f-6a1d2c7e0a11");
	private static final String SPEED_MODIFIER_NAME = "dmzbudokai.angel_speed";

	private AngelPath() {
	}

	// ---- Ranks ------------------------------------------------------------------------------

	public static int maxRank() {
		return 1 + Math.min(BudokaiConfig.ANGEL_RANK_DIVINE_XP.get().size(), BudokaiConfig.ANGEL_RANK_BP.get().size());
	}

	/** Translation key for a rank's title. Ranks past the built-in four reuse the top title. */
	public static String rankKey(int rank) {
		return "dmzbudokai.angel.rank." + Math.max(0, Math.min(rank, 4));
	}

	public static double divineXpForRank(int rank) {
		List<? extends Number> list = BudokaiConfig.ANGEL_RANK_DIVINE_XP.get();
		int index = rank - 2;
		return index >= 0 && index < list.size() ? list.get(index).doubleValue() : Double.MAX_VALUE;
	}

	public static double bpForRank(int rank) {
		List<? extends Number> list = BudokaiConfig.ANGEL_RANK_BP.get();
		int index = rank - 2;
		return index >= 0 && index < list.size() ? list.get(index).doubleValue() : Double.MAX_VALUE;
	}

	/** 1.0 normally, 0.5 while Disfavored. Every perk scales by this. */
	public static double perkStrength(TrainingData data) {
		return isDisfavored(data) ? 0.5 : 1.0;
	}

	public static boolean isDisfavored(TrainingData data) {
		return data.isAngel() && data.angel().getFavor() < BudokaiConfig.ANGEL_DISFAVOR_THRESHOLD.get();
	}

	/** True if the player is an angel of at least {@code rank} and the path is enabled. */
	public static boolean hasRank(TrainingData data, int rank) {
		return BudokaiConfig.ANGEL_ENABLED.get() && data.getAngelRank() >= rank;
	}

	// ---- Initiation ---------------------------------------------------------------------------

	public static String mentorEntity() {
		return BudokaiConfig.ANGEL_MENTOR_ENTITY.get().trim().toLowerCase();
	}

	/** Right-clicking the mentor (Whis) is how you're accepted as a pupil. */
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !BudokaiConfig.ANGEL_ENABLED.get()) return;
		ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getTarget().getType());
		if (id == null || !id.toString().equals(mentorEntity())) return;

		TrainingDataProvider.get(player).ifPresent(data -> {
			AngelState angel = data.angel();
			if (data.isAngel() || angel.isMentorAccepted()) return;
			Optional<StatsData> stats = DmzBridge.stats(player);
			if (stats.isPresent() && NoeaCompat.isAngelForbiddenRace(stats.get())) {
				player.sendSystemMessage(Component.translatable("dmzbudokai.angel.mentor.refuse_race",
						event.getTarget().getType().getDescription()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				return;
			}
			angel.setMentorAccepted(true);
			player.sendSystemMessage(Component.translatable("dmzbudokai.angel.mentor.accepted",
					event.getTarget().getType().getDescription()).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
			player.sendSystemMessage(Component.translatable("dmzbudokai.angel.mentor.next").withStyle(ChatFormatting.GRAY));
		});
	}

	/** Why the player can't join right now, or null if they can. */
	public static Component joinBlocker(ServerPlayer player, StatsData stats, TrainingData data) {
		if (!BudokaiConfig.ANGEL_ENABLED.get()) return Component.translatable("dmzbudokai.angel.disabled");
		if (!DmzBridge.hasCharacter(stats)) return Component.translatable("dmzbudokai.message.no_character");
		if (data.isAngel()) return Component.translatable("dmzbudokai.angel.already");
		if (NoeaCompat.isAngelForbiddenRace(stats)) {
			return Component.translatable("dmzbudokai.angel.forbidden_race", DmzBridge.race(stats));
		}

		long waitMs = data.getAngelRejoinAtMs() - System.currentTimeMillis();
		if (waitMs > 0) {
			return Component.translatable("dmzbudokai.angel.cooldown", (waitMs / 60000L) + 1);
		}

		if (BudokaiConfig.ANGEL_REQUIRE_MENTOR.get() && !data.angel().isMentorAccepted()) {
			return Component.translatable("dmzbudokai.angel.need_mentor", mentorName());
		}

		int alignment = DmzBridge.alignment(stats);
		int needAlignment = BudokaiConfig.ANGEL_JOIN_ALIGNMENT.get();
		if (alignment < needAlignment) {
			return Component.translatable("dmzbudokai.angel.need_alignment", needAlignment, alignment);
		}

		double bp = DmzBridge.baseBattlePower(stats);
		double needBp = BudokaiConfig.ANGEL_JOIN_BP.get();
		if (bp < needBp) {
			return Component.translatable("dmzbudokai.angel.need_bp", formatBp(needBp), formatBp(bp));
		}
		return null;
	}

	public static Component mentorName() {
		ResourceLocation id = ResourceLocation.tryParse(mentorEntity());
		if (id != null && ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
			return ForgeRegistries.ENTITY_TYPES.getValue(id).getDescription();
		}
		return Component.literal(mentorEntity());
	}

	public static boolean join(ServerPlayer player) {
		Optional<StatsData> statsOpt = DmzBridge.stats(player);
		Optional<TrainingData> dataOpt = TrainingDataProvider.get(player);
		if (statsOpt.isEmpty() || dataOpt.isEmpty()) return false;

		StatsData stats = statsOpt.get();
		TrainingData data = dataOpt.get();
		Component blocker = joinBlocker(player, stats, data);
		if (blocker != null) {
			player.sendSystemMessage(blocker.copy().withStyle(ChatFormatting.RED));
			return false;
		}

		data.setAngelRank(1);
		data.setDivineXp(0.0);
		data.angel().resetProgress();
		applyPerks(player, stats, data);
		player.sendSystemMessage(Component.translatable("dmzbudokai.angel.joined",
				Component.translatable(rankKey(1))).withStyle(ChatFormatting.AQUA));
		player.sendSystemMessage(Component.translatable("dmzbudokai.angel.code").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		celebrate(player);
		return true;
	}

	/** Leave voluntarily, or be cast out ({@code fallen} = true). Falling makes the next wait longer. */
	public static void leave(ServerPlayer player, boolean fallen) {
		TrainingDataProvider.get(player).ifPresent(data -> {
			if (!data.isAngel()) return;
			AngelState angel = data.angel();
			if (fallen) angel.setFallCount(angel.getFallCount() + 1);
			long cooldownMs = BudokaiConfig.ANGEL_REJOIN_COOLDOWN_MINUTES.get() * 60_000L * Math.max(1, fallen ? angel.getFallCount() : 1);

			data.setAngelRank(0);
			data.setDivineXp(0.0);
			angel.resetProgress();
			data.setAngelRejoinAtMs(System.currentTimeMillis() + cooldownMs);
			DmzBridge.stats(player).ifPresent(stats -> removePerks(player, stats, data));

			if (fallen) {
				player.sendSystemMessage(Component.translatable("dmzbudokai.angel.fallen", cooldownMs / 60_000L)
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
				player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(),
						40, 0.5, 1.0, 0.5, 0.02);
				player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.4F, 1.6F);
			} else {
				player.sendSystemMessage(Component.translatable("dmzbudokai.angel.left").withStyle(ChatFormatting.GRAY));
			}
		});
	}

	// ---- Progression --------------------------------------------------------------------------

	/** Called by the training system after every counted rep. */
	public static void onTrainingRep(ServerPlayer player, StatsData stats, TrainingData data, TrainingActivity activity) {
		if (!data.isAngel()) return;
		boolean meditation = activity == TrainingActivity.MEDITATION;
		data.setDivineXp(data.getDivineXp() + (meditation ? 2.0 : 1.0));
		if (meditation) changeFavor(player, data, BudokaiConfig.ANGEL_FAVOR_PER_MEDITATION.get());
		tryRankUp(player, stats, data);
	}

	/** Called for each angel near someone who just did a training rep: they're teaching. */
	public static void onMentored(ServerPlayer angel, TrainingData data) {
		if (!data.isAngel()) return;
		data.angel().addMentorRep();
		changeFavor(angel, data, BudokaiConfig.ANGEL_FAVOR_PER_MENTOR_REP.get());
	}

	/** What still stands between the player and their next rank, or null if they can rank up. */
	public static Component rankUpBlocker(StatsData stats, TrainingData data) {
		int next = data.getAngelRank() + 1;
		if (next > maxRank()) return Component.translatable("dmzbudokai.angel.info.max");
		if (data.getDivineXp() < divineXpForRank(next)) return Component.translatable("dmzbudokai.angel.block.xp");
		if (DmzBridge.baseBattlePower(stats) < bpForRank(next)) return Component.translatable("dmzbudokai.angel.block.bp");
		AngelTrial trial = AngelTrial.forRank(next);
		if (trial != null && !trial.isComplete(data)) {
			return Component.translatable("dmzbudokai.angel.block.trial", Component.translatable(trial.nameKey()));
		}
		if (isDisfavored(data)) return Component.translatable("dmzbudokai.angel.block.favor");
		return null;
	}

	public static void tryRankUp(ServerPlayer player, StatsData stats, TrainingData data) {
		if (rankUpBlocker(stats, data) != null) return;
		int next = data.getAngelRank() + 1;
		AngelTrial trial = AngelTrial.forRank(next);

		data.setAngelRank(next);
		applyPerks(player, stats, data);
		if (trial != null) {
			player.sendSystemMessage(Component.translatable("dmzbudokai.angel.trial_passed", Component.translatable(trial.nameKey()))
					.withStyle(ChatFormatting.AQUA));
		}
		player.sendSystemMessage(Component.translatable("dmzbudokai.angel.rank_up",
				Component.translatable(rankKey(next))).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
		for (AngelAbilities.Ability ability : AngelAbilities.Ability.values()) {
			if (ability.rank() == next) {
				player.sendSystemMessage(Component.translatable("dmzbudokai.angel.ability_unlocked",
						Component.translatable(ability.nameKey()), Component.translatable(ability.descKey())).withStyle(ChatFormatting.AQUA));
			}
		}
		celebrate(player);
	}

	// ---- Divine Favor -------------------------------------------------------------------------

	public static void changeFavor(ServerPlayer player, TrainingData data, double amount) {
		if (!data.isAngel() || amount == 0) return;
		AngelState angel = data.angel();
		boolean wasDisfavored = isDisfavored(data);
		angel.addFavor(amount);

		if (angel.getFavor() <= 0.0) {
			leave(player, true);
			return;
		}
		boolean nowDisfavored = isDisfavored(data);
		if (nowDisfavored && !wasDisfavored) {
			player.sendSystemMessage(Component.translatable("dmzbudokai.angel.disfavored").withStyle(ChatFormatting.GOLD));
		} else if (!nowDisfavored && wasDisfavored) {
			player.sendSystemMessage(Component.translatable("dmzbudokai.angel.favor_restored").withStyle(ChatFormatting.AQUA));
		}
	}

	// ---- Upkeep -------------------------------------------------------------------------------

	/** Runs once a second per player: keeps perks applied, tracks trials and checks the Angel Code. */
	public static void tick(ServerPlayer player, TrainingData data) {
		Optional<StatsData> statsOpt = DmzBridge.stats(player);
		if (statsOpt.isEmpty()) return;
		StatsData stats = statsOpt.get();

		if (!data.isAngel()) {
			if (data.isHaloForcedByAngel()) removePerks(player, stats, data);
			return;
		}
		if (!BudokaiConfig.ANGEL_ENABLED.get()) {
			removePerks(player, stats, data);
			return;
		}
		if (DmzBridge.alignment(stats) < BudokaiConfig.ANGEL_FALL_ALIGNMENT.get()) {
			leave(player, true);
			return;
		}

		AngelState angel = data.angel();
		if (angel.getMeditationStreakTicks() > angel.getBestMeditationTicks()) {
			angel.setBestMeditationTicks(angel.getMeditationStreakTicks());
		}

		applyPerks(player, stats, data);
		giveStaffIfEarned(player, data);
		AngelAbilities.tickAbilities(player, data);
		tryRankUp(player, stats, data);
	}

	private static void giveStaffIfEarned(ServerPlayer player, TrainingData data) {
		if (data.angel().isStaffGiven() || !hasRank(data, BudokaiConfig.ANGEL_BLESS_RANK.get())) return;
		data.angel().setStaffGiven(true);
		ItemStack staff = new ItemStack(BudokaiItems.ANGEL_STAFF.get());
		if (!player.getInventory().add(staff)) player.drop(staff, false);
		player.sendSystemMessage(Component.translatable("dmzbudokai.angel.staff_given").withStyle(ChatFormatting.AQUA));
	}

	private static void applyPerks(ServerPlayer player, StatsData stats, TrainingData data) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null) {
			double amount = data.getAngelRank() * BudokaiConfig.ANGEL_SPEED_PER_RANK.get() * perkStrength(data);
			AttributeModifier existing = speed.getModifier(SPEED_MODIFIER_ID);
			if (existing == null || existing.getAmount() != amount) {
				speed.removeModifier(SPEED_MODIFIER_ID);
				if (amount > 0) {
					speed.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_ID, SPEED_MODIFIER_NAME, amount,
							AttributeModifier.Operation.MULTIPLY_BASE));
				}
			}
		}

		if (BudokaiConfig.ANGEL_FORCE_HALO.get()) {
			if (!DmzBridge.isForceHalo(stats)) {
				DmzBridge.setForceHalo(stats, true);
				data.setHaloForcedByAngel(true);
			}
		} else if (data.isHaloForcedByAngel()) {
			DmzBridge.setForceHalo(stats, false);
			data.setHaloForcedByAngel(false);
		}
	}

	private static void removePerks(ServerPlayer player, StatsData stats, TrainingData data) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
		// Only take the halo away if we were the ones who put it there (not a DMZ /halo command or death).
		if (data.isHaloForcedByAngel()) {
			DmzBridge.setForceHalo(stats, false);
			data.setHaloForcedByAngel(false);
		}
	}

	static void celebrate(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
					40, 0.6, 1.0, 0.6, 0.05);
		}
		player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
	}

	public static String formatBp(double bp) {
		if (bp >= 1.0E12) return String.format("%.2fT", bp / 1.0E12);
		if (bp >= 1.0E9) return String.format("%.2fB", bp / 1.0E9);
		if (bp >= 1.0E6) return String.format("%.2fM", bp / 1.0E6);
		if (bp >= 1.0E3) return String.format("%.1fK", bp / 1.0E3);
		return String.format("%.0f", bp);
	}
}

package com.dmzbudokai.angel;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.compat.NoeaCompat;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dmzbudokai.training.TrainingActivity;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The Angel Path: an opt-in progression path for pure-hearted fighters, inspired by the angels of
 * Dragon Ball Super (Whis, Vados). Angels are master teachers and masters of instinctive movement.
 * <p>
 * Ranks: 1 Angel Trainee, 2 Attendant Angel, 3 Guardian Angel, 4 Grand Angel (more if the config
 * lists more thresholds). Rank 1 comes from joining; later ranks need Divine XP (earned by training,
 * double from meditation) and base Battle Power.
 * <p>
 * Perks, all scaling with rank: bigger meditation gains, a mentor bonus for anyone training next to
 * you, "Angelic Instinct" dodges, extra movement speed, and the DMZ halo.
 * Restriction: angels must stay pure. Let your alignment drop too low and you fall from the path.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class AngelPath {
	private static final UUID SPEED_MODIFIER_ID = UUID.fromString("5f0d8b8e-3f7a-4d0e-9b1f-6a1d2c7e0a11");
	private static final String SPEED_MODIFIER_NAME = "dmzbudokai.angel_speed";

	private AngelPath() {
	}

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
		applyPerks(player, stats, data);
		player.sendSystemMessage(Component.translatable("dmzbudokai.angel.joined",
				Component.translatable(rankKey(1))).withStyle(ChatFormatting.AQUA));
		celebrate(player);
		return true;
	}

	/** Leave voluntarily, or be cast out ({@code fallen} = true) for losing your purity. */
	public static void leave(ServerPlayer player, boolean fallen) {
		TrainingDataProvider.get(player).ifPresent(data -> {
			if (!data.isAngel()) return;
			data.setAngelRank(0);
			data.setDivineXp(0.0);
			data.setAngelRejoinAtMs(System.currentTimeMillis() + BudokaiConfig.ANGEL_REJOIN_COOLDOWN_MINUTES.get() * 60_000L);
			DmzBridge.stats(player).ifPresent(stats -> removePerks(player, stats, data));
			player.sendSystemMessage(Component.translatable(fallen ? "dmzbudokai.angel.fallen" : "dmzbudokai.angel.left")
					.withStyle(fallen ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
		});
	}

	/** Called by the training system after every counted rep. */
	public static void onTrainingRep(ServerPlayer player, StatsData stats, TrainingData data, TrainingActivity activity) {
		if (!data.isAngel()) return;
		data.setDivineXp(data.getDivineXp() + (activity == TrainingActivity.MEDITATION ? 2.0 : 1.0));
		tryRankUp(player, stats, data);
	}

	public static void tryRankUp(ServerPlayer player, StatsData stats, TrainingData data) {
		int next = data.getAngelRank() + 1;
		if (next > maxRank()) return;
		if (data.getDivineXp() < divineXpForRank(next)) return;
		if (DmzBridge.baseBattlePower(stats) < bpForRank(next)) return;

		data.setAngelRank(next);
		applyPerks(player, stats, data);
		player.sendSystemMessage(Component.translatable("dmzbudokai.angel.rank_up",
				Component.translatable(rankKey(next))).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
		celebrate(player);
	}

	/** Runs once a second per player: keeps perks applied and checks the purity requirement. */
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
		applyPerks(player, stats, data);
	}

	private static void applyPerks(ServerPlayer player, StatsData stats, TrainingData data) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null) {
			double amount = data.getAngelRank() * BudokaiConfig.ANGEL_SPEED_PER_RANK.get();
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

	/** Angelic Instinct: a rank-scaled chance to dodge an attack outright. */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onAttacked(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getSource().getEntity() == null || event.getSource().getEntity() == player) return;

		TrainingDataProvider.get(player).ifPresent(data -> {
			if (!data.isAngel() || !BudokaiConfig.ANGEL_ENABLED.get()) return;
			long now = player.level().getGameTime();
			if (now < data.getDodgeReadyTick()) return;

			double chance = Math.min(0.95, data.getAngelRank() * BudokaiConfig.ANGEL_DODGE_CHANCE_PER_RANK.get());
			if (player.getRandom().nextDouble() >= chance) return;

			event.setCanceled(true);
			data.setDodgeReadyTick(now + BudokaiConfig.ANGEL_DODGE_COOLDOWN.get());
			player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(),
					10, 0.3, 0.5, 0.3, 0.02);
			player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5F, 1.8F);
			player.displayClientMessage(Component.translatable("dmzbudokai.angel.dodge").withStyle(ChatFormatting.AQUA), true);
		});
	}

	private static void celebrate(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
					40, 0.6, 1.0, 0.6, 0.05);
		}
		player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
	}

	public static String formatBp(double bp) {
		if (bp >= 1.0E9) return String.format("%.2fB", bp / 1.0E9);
		if (bp >= 1.0E6) return String.format("%.2fM", bp / 1.0E6);
		if (bp >= 1.0E3) return String.format("%.1fK", bp / 1.0E3);
		return String.format("%.0f", bp);
	}
}

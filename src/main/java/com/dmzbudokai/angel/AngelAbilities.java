package com.dmzbudokai.angel;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Deque;

/**
 * Angel abilities, unlocked by rank. Every ability's strength is halved while Disfavored.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class AngelAbilities {
	private static final int SNAPSHOT_INTERVAL_TICKS = 10;

	private AngelAbilities() {
	}

	/** Every ability, for unlock messages and {@code /budokai angel info}. */
	public enum Ability {
		HALO("halo", 1),
		DIVINE_MEDITATION("divine_meditation", 1),
		ANGELIC_INSTINCT("angelic_instinct", 1),
		ANGELIC_GUIDANCE("angelic_guidance", 2),
		BLESSING("blessing", -1),
		TEMPORAL_DO_OVER("temporal_do_over", -2),
		DIVINE_PRESENCE("divine_presence", -3),
		AUTONOMOUS_INSTINCT("autonomous_instinct", -4);

		private final String id;
		private final int fixedRank;

		/** Negative ranks are read from the config: -1 bless, -2 rewind, -3 presence, -4 autonomous. */
		Ability(String id, int rank) {
			this.id = id;
			this.fixedRank = rank;
		}

		public int rank() {
			return switch (fixedRank) {
				case -1 -> BudokaiConfig.ANGEL_BLESS_RANK.get();
				case -2 -> BudokaiConfig.ANGEL_REWIND_RANK.get();
				case -3 -> BudokaiConfig.ANGEL_PRESENCE_RANK.get();
				case -4 -> BudokaiConfig.ANGEL_AUTONOMOUS_RANK.get();
				default -> fixedRank;
			};
		}

		public String nameKey() { return "dmzbudokai.angel.ability." + id; }

		public String descKey() { return "dmzbudokai.angel.ability." + id + ".desc"; }
	}

	/** Once a second for every angel: Divine Presence. */
	static void tickAbilities(ServerPlayer player, TrainingData data) {
		if (AngelPath.hasRank(data, BudokaiConfig.ANGEL_PRESENCE_RANK.get())) {
			double radius = BudokaiConfig.ANGEL_PRESENCE_RADIUS.get() * AngelPath.perkStrength(data);
			for (Monster monster : player.serverLevel().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
				monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, false));
				monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false));
			}
		}
	}

	/** Every tick for every angel that can rewind: remember where they've been. */
	public static void recordSnapshot(ServerPlayer player, TrainingData data) {
		if (!AngelPath.hasRank(data, BudokaiConfig.ANGEL_REWIND_RANK.get())) {
			data.angel().snapshots().clear();
			return;
		}
		long now = player.level().getGameTime();
		Deque<AngelState.Snapshot> snapshots = data.angel().snapshots();
		AngelState.Snapshot last = snapshots.peekLast();
		if (last != null && now - last.gameTime() < SNAPSHOT_INTERVAL_TICKS) return;

		snapshots.addLast(new AngelState.Snapshot(now, player.level().dimension(), player.position(), player.getHealth()));
		long keepTicks = BudokaiConfig.ANGEL_REWIND_SECONDS.get() * 20L + SNAPSHOT_INTERVAL_TICKS;
		while (!snapshots.isEmpty() && now - snapshots.peekFirst().gameTime() > keepTicks) {
			snapshots.removeFirst();
		}
	}

	// ---- Angelic Instinct / Autonomous Instinct ---------------------------------------------

	/** Angelic Instinct: a rank-scaled chance to dodge an attack outright. Doubles at low health from Grand Angel. */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onAttacked(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getSource().getEntity() == null || event.getSource().getEntity() == player) return;

		TrainingDataProvider.get(player).ifPresent(data -> {
			if (!AngelPath.hasRank(data, 1)) return;
			long now = player.level().getGameTime();
			if (now < data.getDodgeReadyTick()) return;

			double chance = data.getAngelRank() * BudokaiConfig.ANGEL_DODGE_CHANCE_PER_RANK.get() * AngelPath.perkStrength(data);
			boolean autonomous = AngelPath.hasRank(data, BudokaiConfig.ANGEL_AUTONOMOUS_RANK.get())
					&& player.getHealth() <= player.getMaxHealth() * BudokaiConfig.ANGEL_AUTONOMOUS_HEALTH.get();
			if (autonomous) chance *= 2.0;
			if (player.getRandom().nextDouble() >= Math.min(0.95, chance)) return;

			event.setCanceled(true);
			data.setDodgeReadyTick(now + BudokaiConfig.ANGEL_DODGE_COOLDOWN.get());
			data.angel().addInstinctDodge();
			player.serverLevel().sendParticles(autonomous ? ParticleTypes.END_ROD : ParticleTypes.CLOUD,
					player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
			player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5F, 1.8F);
			player.displayClientMessage(Component.translatable(autonomous ? "dmzbudokai.angel.dodge_autonomous" : "dmzbudokai.angel.dodge")
					.withStyle(ChatFormatting.AQUA), true);
		});
	}

	// ---- Temporal Do-Over ---------------------------------------------------------------------

	/** Whis's do-over: instead of dying, time rewinds a few seconds. Runs before DMZ's own death handling. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		TrainingDataProvider.get(player).ifPresent(data -> {
			if (!AngelPath.hasRank(data, BudokaiConfig.ANGEL_REWIND_RANK.get())) return;
			AngelState angel = data.angel();
			long nowMs = System.currentTimeMillis();
			if (nowMs < angel.getRewindReadyAtMs()) return;

			AngelState.Snapshot target = angel.snapshots().peekFirst();
			if (target == null) return;

			event.setCanceled(true);
			ServerLevel level = player.serverLevel();
			level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.5, 1.0, 0.5, 0.1);

			float restored = Math.max(target.health(), player.getMaxHealth() * 0.25F);
			player.setHealth(Math.min(player.getMaxHealth(), restored));
			player.clearFire();
			player.fallDistance = 0.0F;
			if (target.dimension().equals(level.dimension())) {
				player.teleportTo(target.pos().x, target.pos().y, target.pos().z);
			}

			long cooldownMs = (long) (BudokaiConfig.ANGEL_REWIND_COOLDOWN_MINUTES.get() * 60_000L / AngelPath.perkStrength(data));
			angel.setRewindReadyAtMs(nowMs + cooldownMs);
			angel.snapshots().clear();

			level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.5, 1.0, 0.5, 0.1);
			level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.6F);
			player.sendSystemMessage(Component.translatable("dmzbudokai.angel.rewind", cooldownMs / 60_000L)
					.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
		});
	}

	// ---- Angel's Blessing ---------------------------------------------------------------------

	/** An angel blesses another player's training (used by the Angel Staff). Returns true if it worked. */
	public static boolean bless(ServerPlayer angelPlayer, ServerPlayer target) {
		TrainingData data = TrainingDataProvider.get(angelPlayer).orElse(null);
		TrainingData targetData = TrainingDataProvider.get(target).orElse(null);
		if (data == null || targetData == null) return false;

		if (!AngelPath.hasRank(data, BudokaiConfig.ANGEL_BLESS_RANK.get())) {
			angelPlayer.displayClientMessage(Component.translatable("dmzbudokai.angel.bless.need_rank",
					Component.translatable(AngelPath.rankKey(BudokaiConfig.ANGEL_BLESS_RANK.get()))).withStyle(ChatFormatting.RED), true);
			return false;
		}
		if (target == angelPlayer) {
			angelPlayer.displayClientMessage(Component.translatable("dmzbudokai.angel.bless.self").withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		long nowMs = System.currentTimeMillis();
		AngelState angel = data.angel();
		if (nowMs < angel.getBlessCooldownUntilMs()) {
			long seconds = (angel.getBlessCooldownUntilMs() - nowMs) / 1000L + 1;
			angelPlayer.displayClientMessage(Component.translatable("dmzbudokai.angel.bless.cooldown", seconds).withStyle(ChatFormatting.GRAY), true);
			return false;
		}

		double multiplier = 1.0 + data.getAngelRank() * BudokaiConfig.ANGEL_BLESS_BONUS_PER_RANK.get() * AngelPath.perkStrength(data);
		long durationMs = BudokaiConfig.ANGEL_BLESS_DURATION_SECONDS.get() * 1000L;
		targetData.angel().bless(nowMs + durationMs, multiplier, angelPlayer.getGameProfile().getName());
		angel.setBlessCooldownUntilMs(nowMs + BudokaiConfig.ANGEL_BLESS_COOLDOWN_SECONDS.get() * 1000L);

		// Teaching is what angels do: it earns Divine XP and Favor.
		data.setDivineXp(data.getDivineXp() + 10.0);
		AngelPath.changeFavor(angelPlayer, data, 2.0);

		String percent = String.format("%.0f", (multiplier - 1.0) * 100.0);
		target.sendSystemMessage(Component.translatable("dmzbudokai.angel.bless.received", angelPlayer.getDisplayName(), percent,
				durationMs / 60_000L).withStyle(ChatFormatting.AQUA));
		angelPlayer.sendSystemMessage(Component.translatable("dmzbudokai.angel.bless.given", target.getDisplayName(), percent)
				.withStyle(ChatFormatting.AQUA));
		target.serverLevel().sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 30, 0.4, 0.8, 0.4, 0.03);
		target.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
		return true;
	}
}

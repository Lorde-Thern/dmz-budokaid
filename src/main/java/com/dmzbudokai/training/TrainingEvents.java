package com.dmzbudokai.training;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.angel.AngelPath;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingData;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dragonminez.common.events.DMZEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Detects training that happens through normal play: fighting, meditating and sprinting.
 * Punching bags and training weights call {@link TrainingManager} directly.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class TrainingEvents {
	private static final double MEDITATION_MAX_DRIFT_SQ = 0.15 * 0.15;

	private TrainingEvents() {
	}

	/** Replaces Training Points: no TP is earned while the addon is in charge of progression. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onTrainingPointGain(DMZEvent.TPGainEvent event) {
		if (BudokaiConfig.SPEC.isLoaded() && BudokaiConfig.DISABLE_TRAINING_POINTS.get()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onHurt(LivingHurtEvent event) {
		if (event.isCanceled() || event.getAmount() <= 0) return;
		LivingEntity victim = event.getEntity();
		if (victim.level().isClientSide || victim instanceof ArmorStand) return;

		if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != victim) {
			TrainingDataProvider.get(attacker).ifPresent(data -> {
				long now = attacker.level().getGameTime();
				if (now - data.getLastCombatTick() >= BudokaiConfig.COMBAT_COOLDOWN.get()) {
					data.setLastCombatTick(now);
					TrainingManager.train(attacker, TrainingActivity.COMBAT_HIT);
				}
			});
		}

		if (victim instanceof ServerPlayer hurtPlayer && event.getSource().getEntity() instanceof LivingEntity) {
			TrainingDataProvider.get(hurtPlayer).ifPresent(data -> {
				long now = hurtPlayer.level().getGameTime();
				if (now - data.getLastHurtTick() >= BudokaiConfig.COMBAT_COOLDOWN.get()) {
					data.setLastHurtTick(now);
					TrainingManager.train(hurtPlayer, TrainingActivity.COMBAT_HURT);
				}
			});
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
		if (!player.isAlive() || player.isSpectator()) return;

		TrainingDataProvider.get(player).ifPresent(data -> {
			tickMeditation(player, data);
			tickSprint(player, data);
			if (player.tickCount % 20 == 0) {
				AngelPath.tick(player, data);
			}
		});
	}

	private static void tickMeditation(ServerPlayer player, TrainingData data) {
		Vec3 pos = player.position();
		boolean meditating = player.isShiftKeyDown() && player.onGround() && !player.isUsingItem() && !player.isSprinting();
		Vec3 anchor = data.getMeditationAnchor();

		if (!meditating || anchor == null || anchor.distanceToSqr(pos) > MEDITATION_MAX_DRIFT_SQ) {
			data.setMeditationTicks(0);
			data.setMeditationAnchor(meditating ? pos : null);
			return;
		}

		int ticks = data.getMeditationTicks() + 1;
		if (ticks >= BudokaiConfig.MEDITATION_REP_TICKS.get()) {
			ticks = 0;
			if (TrainingManager.train(player, TrainingActivity.MEDITATION)) {
				player.serverLevel().sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z,
						8, 0.4, 0.6, 0.4, 0.02);
			}
		}
		data.setMeditationTicks(ticks);
	}

	private static void tickSprint(ServerPlayer player, TrainingData data) {
		Vec3 pos = player.position();
		Vec3 last = data.getLastSprintPos();
		data.setLastSprintPos(pos);
		if (last == null || !player.isSprinting() || !player.onGround() || player.isPassenger()) return;

		double dx = pos.x - last.x;
		double dz = pos.z - last.z;
		double step = Math.sqrt(dx * dx + dz * dz);
		if (step > 1.5) return; // teleport, knockback or dash: not running

		double distance = data.getSprintDistance() + step;
		double perRep = BudokaiConfig.SPRINT_BLOCKS_PER_REP.get();
		if (distance >= perRep) {
			distance -= perRep;
			TrainingManager.train(player, TrainingActivity.SPRINT);
		}
		data.setSprintDistance(distance);
	}
}

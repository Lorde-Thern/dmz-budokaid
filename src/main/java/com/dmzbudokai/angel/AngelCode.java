package com.dmzbudokai.angel;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingDataProvider;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * The Angel Code: angels stay neutral and never prey on the defenseless.
 * Killing an innocent (villagers, wandering traders, iron golems) or a player far weaker than you
 * costs Divine Favor. Meditation and teaching earn it back.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class AngelCode {
	private AngelCode() {
	}

	// LOWEST and only if not canceled, so a Temporal Do-Over or totem save isn't counted as a kill.
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onKill(LivingDeathEvent event) {
		if (event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
		LivingEntity victim = event.getEntity();
		if (victim == killer) return;

		TrainingDataProvider.get(killer).ifPresent(data -> {
			if (!AngelPath.hasRank(data, 1)) return;

			String reason = null;
			double penalty = 0.0;
			if (victim instanceof AbstractVillager || victim instanceof IronGolem) {
				reason = "dmzbudokai.angel.transgression.innocent";
				penalty = BudokaiConfig.ANGEL_FAVOR_PENALTY_INNOCENT.get();
			} else if (victim instanceof ServerPlayer victimPlayer && isFarWeaker(killer, victimPlayer)) {
				reason = "dmzbudokai.angel.transgression.bully";
				penalty = BudokaiConfig.ANGEL_FAVOR_PENALTY_BULLY.get();
			}
			if (reason == null || penalty <= 0) return;

			killer.sendSystemMessage(Component.translatable(reason, victim.getDisplayName(), String.format("%.0f", penalty))
					.withStyle(ChatFormatting.RED));
			AngelPath.changeFavor(killer, data, -penalty);
		});
	}

	private static boolean isFarWeaker(ServerPlayer killer, ServerPlayer victim) {
		Optional<StatsData> killerStats = DmzBridge.stats(killer);
		Optional<StatsData> victimStats = DmzBridge.stats(victim);
		if (killerStats.isEmpty() || victimStats.isEmpty()) return false;
		double ratio = DmzBridge.baseBattlePower(killerStats.get()) / DmzBridge.baseBattlePower(victimStats.get());
		return ratio >= BudokaiConfig.ANGEL_BULLY_BP_RATIO.get();
	}
}

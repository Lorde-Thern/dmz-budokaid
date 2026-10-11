package com.dmzbudokai.forms;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingDataProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class FormEvents {
	private FormEvents() {
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		FormUnlockConfig.load();
		if (isStatCapTooLow()) {
			DMZBudokai.LOGGER.warn("Dragon Mine Z maxValue is {} (total stats capped at {}). That caps Battle Power too low to reach "
							+ "the higher BP form unlocks. Raise maxValue in config/dragonminez/general-server.json "
							+ "to at least {} (or lower recommendedDmzMaxValue in dmzbudokai-server.toml to silence this).",
					DmzBridge.dmzMaxValue(), (long) DmzBridge.dmzMaxValue() * 6L, BudokaiConfig.RECOMMENDED_DMZ_MAX_VALUE.get());
		}
	}

	/** True when DMZ's stat cap is too low for the configured form thresholds to be reachable. */
	public static boolean isStatCapTooLow() {
		int recommended = BudokaiConfig.RECOMMENDED_DMZ_MAX_VALUE.get();
		return BudokaiConfig.BP_FORM_UNLOCKS.get() && recommended > 0 && DmzBridge.dmzMaxValue() < recommended;
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
		if (player.tickCount % 20 != 10) return;

		DmzBridge.stats(player).ifPresent(stats ->
				TrainingDataProvider.get(player).ifPresent(data -> FormUnlocks.update(player, stats, data)));
	}
}

package com.dmzbudokai.forms;

import com.dmzbudokai.DMZBudokai;
import com.dmzbudokai.compat.DmzBridge;
import com.dmzbudokai.config.BudokaiConfig;
import com.dmzbudokai.data.TrainingDataProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		return BudokaiConfig.BP_FORM_UNLOCKS.get() && !BudokaiConfig.LIMITLESS_MODE.get()
				&& recommended > 0 && DmzBridge.dmzMaxValue() < recommended;
	}

	/** Right-clicking a master NPC (e.g. King Kai) meets the "talk to a master" requirement for their forms. */
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getTarget().getType());
		if (id == null) return;
		String entityId = id.toString();

		List<String> teaches = new ArrayList<>();
		for (Map.Entry<String, String> entry : FormUnlockConfig.get().masterRequired.entrySet()) {
			if (entry.getValue().equals(entityId)) teaches.add(FormUnlocks.prettyName(entry.getKey()));
		}
		if (teaches.isEmpty()) return;

		TrainingDataProvider.get(player).ifPresent(data -> {
			if (!data.addMetMaster(entityId)) return;
			player.sendSystemMessage(Component.translatable("dmzbudokai.forms.master_met",
					event.getTarget().getType().getDescription(), String.join(", ", teaches)).withStyle(ChatFormatting.AQUA));
			DmzBridge.stats(player).ifPresent(stats -> FormUnlocks.update(player, stats, data));
		});
	}

	/** Display name for a master entity id, falling back to the id itself. */
	public static Component masterName(String entityId) {
		ResourceLocation id = ResourceLocation.tryParse(entityId);
		EntityType<?> type = id == null ? null : ForgeRegistries.ENTITY_TYPES.getValue(id);
		return type != null && ForgeRegistries.ENTITY_TYPES.containsKey(id) ? type.getDescription() : Component.literal(entityId);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
		if (player.tickCount % 20 != 10) return;

		DmzBridge.stats(player).ifPresent(stats ->
				TrainingDataProvider.get(player).ifPresent(data -> FormUnlocks.update(player, stats, data)));
	}
}

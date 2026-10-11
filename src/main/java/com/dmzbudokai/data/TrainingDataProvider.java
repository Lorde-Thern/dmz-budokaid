package com.dmzbudokai.data;

import com.dmzbudokai.DMZBudokai;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TrainingDataProvider implements ICapabilitySerializable<CompoundTag> {
	public static final Capability<TrainingData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	public static final ResourceLocation ID = new ResourceLocation(DMZBudokai.MOD_ID, "training");

	private final TrainingData data = new TrainingData();
	private final LazyOptional<TrainingData> optional = LazyOptional.of(() -> data);

	public static Optional<TrainingData> get(Player player) {
		return player.getCapability(CAPABILITY).resolve();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return data.save();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		data.load(nbt);
	}

	@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static final class ModEvents {
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
			event.register(TrainingData.class);
		}
	}

	@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
	public static final class ForgeEvents {
		@SubscribeEvent
		public static void attach(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof Player) {
				TrainingDataProvider provider = new TrainingDataProvider();
				event.addCapability(ID, provider);
				event.addListener(provider.optional::invalidate);
			}
		}

		@SubscribeEvent
		public static void clone(PlayerEvent.Clone event) {
			Player original = event.getOriginal();
			original.reviveCaps();
			get(original).ifPresent(oldData -> get(event.getEntity()).ifPresent(newData -> newData.copyFrom(oldData)));
			original.invalidateCaps();
		}
	}
}

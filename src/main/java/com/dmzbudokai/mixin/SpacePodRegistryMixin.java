package com.dmzbudokai.mixin;

import com.dmzbudokai.planets.PlanetRegistry;
import com.dragonminez.common.spacepod.SpacePodDestinationDefinition;
import com.dragonminez.common.spacepod.SpacePodDestinationRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds Budokai planets to Dragon Mine Z's space pod destinations.
 * <p>
 * DMZ's destination file uses {@code "replace": true}, which would wipe anything a datapack added before it,
 * so instead the planets are merged into DMZ's list every time it's read or synced. All targets are DMZ's own
 * static methods (not obfuscated), so no remapping is needed.
 */
@Mixin(value = SpacePodDestinationRegistry.class, remap = false)
public abstract class SpacePodRegistryMixin {
	@Shadow
	private static List<SpacePodDestinationDefinition> serverDestinations;

	@Unique
	private static List<SpacePodDestinationDefinition> dmzbudokai$base = List.of();
	@Unique
	private static List<SpacePodDestinationDefinition> dmzbudokai$merged;
	@Unique
	private static int dmzbudokai$mergedVersion = -1;

	@Unique
	private static void dmzbudokai$merge() {
		boolean dmzReloaded = serverDestinations != dmzbudokai$merged;
		if (!dmzReloaded && dmzbudokai$mergedVersion == PlanetRegistry.version()) return;
		if (dmzReloaded) dmzbudokai$base = serverDestinations;

		List<SpacePodDestinationDefinition> combined = new ArrayList<>(dmzbudokai$base);
		Set<String> ids = new HashSet<>();
		for (SpacePodDestinationDefinition destination : combined) ids.add(destination.id());
		for (SpacePodDestinationDefinition planet : PlanetRegistry.destinations()) {
			if (ids.add(planet.id())) combined.add(planet);
		}
		dmzbudokai$merged = List.copyOf(combined);
		dmzbudokai$mergedVersion = PlanetRegistry.version();
		serverDestinations = dmzbudokai$merged;
	}

	@Inject(method = "getServerDestinations", at = @At("HEAD"))
	private static void dmzbudokai$beforeList(CallbackInfoReturnable<List<SpacePodDestinationDefinition>> cir) {
		dmzbudokai$merge();
	}

	@Inject(method = "getServerDestination", at = @At("HEAD"))
	private static void dmzbudokai$beforeLookup(String destinationId, CallbackInfoReturnable<SpacePodDestinationDefinition> cir) {
		dmzbudokai$merge();
	}

	@Inject(method = "syncToPlayer", at = @At("HEAD"))
	private static void dmzbudokai$beforeSync(net.minecraft.server.level.ServerPlayer player, CallbackInfo ci) {
		dmzbudokai$merge();
	}
}

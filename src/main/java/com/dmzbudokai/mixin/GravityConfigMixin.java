package com.dmzbudokai.mixin;

import com.dmzbudokai.planets.PlanetRegistry;
import com.dragonminez.common.config.GeneralServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Gives Budokai planets their gravity (Planet Vegeta: 10x) through Dragon Mine Z's own gravity system.
 * An entry for the same dimension in DMZ's gravityPerWorld config still wins.
 */
@Mixin(value = GeneralServerConfig.GravityConfig.class, remap = false)
public abstract class GravityConfigMixin {
	@Inject(method = "getWorldGravity", at = @At("HEAD"), cancellable = true)
	private void dmzbudokai$planetGravity(String dimensionId, CallbackInfoReturnable<Double> cir) {
		if (dimensionId == null) return;
		Map<String, Double> dmzTable = ((GeneralServerConfig.GravityConfig) (Object) this).getGravityPerWorld();
		if (dmzTable != null && dmzTable.containsKey(dimensionId)) return;
		Double gravity = PlanetRegistry.configuredGravity(dimensionId);
		if (gravity != null) cir.setReturnValue(Math.max(0.0, gravity));
	}
}

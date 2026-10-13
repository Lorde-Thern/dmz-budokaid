package com.dmzbudokai.mixin;

import com.dmzbudokai.config.BudokaiConfig;
import com.dragonminez.common.stats.character.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Limitless mode: stops Dragon Mine Z clamping each stat to its configured maxValue (it does this
 * when "maxLevelValueInsteadOfStats" is off, including every time player data loads).
 */
@Mixin(value = Stats.class, remap = false)
public abstract class StatsMixin {
	@Inject(method = "clampStatValue", at = @At("HEAD"), cancellable = true)
	private void dmzbudokai$limitless(int value, CallbackInfoReturnable<Integer> cir) {
		if (BudokaiConfig.SPEC.isLoaded() && BudokaiConfig.LIMITLESS_MODE.get()) {
			cir.setReturnValue(Math.max(0, value));
		}
	}
}

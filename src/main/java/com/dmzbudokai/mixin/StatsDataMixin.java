package com.dmzbudokai.mixin;

import com.dmzbudokai.compat.BattlePower;
import com.dragonminez.common.stats.StatsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes Dragon Mine Z report realistic Battle Power (scouter, stats screen, ki sense...) when
 * {@code realisticBattlePower} is on. DMZ only uses BP for display and comparisons, not damage.
 * DMZ is not obfuscated, so no remapping is needed.
 */
@Mixin(value = StatsData.class, remap = false)
public abstract class StatsDataMixin {
	@Inject(method = "getBattlePowerExact", at = @At("HEAD"), cancellable = true)
	private void dmzbudokai$realisticBattlePower(CallbackInfoReturnable<Double> cir) {
		StatsData self = (StatsData) (Object) this;
		// Upgraded androids show DMZ's "off the charts" value; leave that alone.
		if (BattlePower.realisticEnabled() && !self.getStatus().isAndroidUpgraded()) {
			cir.setReturnValue(BattlePower.realistic(self));
		}
	}
}

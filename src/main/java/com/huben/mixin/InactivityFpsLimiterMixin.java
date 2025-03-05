package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.huben.addon.Addon;

import net.minecraft.client.option.InactivityFpsLimiter;;

@Mixin(InactivityFpsLimiter.class)
public abstract class InactivityFpsLimiterMixin {
    @Inject(method = "onInput", at = @At("HEAD"))
    private void onInput(CallbackInfo ci) {
        Addon.weatherPinger.onInput();
    }
}

package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.huben.addon.events.world.WeatherEvent;

import meteordevelopment.meteorclient.MeteorClient;

import static com.huben.addon.Addon.weatherLogger;
import static meteordevelopment.meteorclient.MeteorClient.mc;

import static com.huben.util.RainGradientBugfixHelper.rainGradientDelta;
import static com.huben.util.RainGradientBugfixHelper.encounteredBug;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.World;

// [1] For some reason when rain starts, after increasing the gradient to 0.2 it resets to 0.0f for one iteration.

@Mixin(World.class)
public class WorldMixin {
    
    @Inject(method = "setRainGradient" , at = @At("HEAD"))
    private void setRainGradient(float value, CallbackInfo ci) {
        if (mc.world.getRegistryKey() != World.OVERWORLD) return;
        if (weatherLogger != null && weatherLogger.isActive()) {
            weatherLogger.info("Rain gradient: " + new BigDecimal(rainGradient).setScale(2, RoundingMode.HALF_UP).floatValue());
        }
        if (value == 0.0f) {
            if (rainGradient == 0.0f) return; // We alread know it's not raining
            if (rainGradientDelta > 0.0f) { // Prevent mentioned bug [1].
                encounteredBug = true;
                return;
            }
            MeteorClient.EVENT_BUS.post(WeatherEvent.Clear.get());
        } else {
            if (encounteredBug) { // Prevent mentioned bug [1].
                encounteredBug = false;
                return;
            }
            rainGradientDelta = value - rainGradient;
            if (rainGradient > 0.0f) return; // We alread know it's raining
            if (thunderGradient == 0.0f) { // Only rain
                MeteorClient.EVENT_BUS.post(WeatherEvent.Rain.get());
            } else { // Thunderstorm
                MeteorClient.EVENT_BUS.post(WeatherEvent.Thunder.get());
            }
        }
        
    }
    @Inject(method = "setThunderGradient" , at = @At("HEAD"))
    private void setThunderGradient(float value, CallbackInfo ci) {
        if (mc.world.getRegistryKey() != World.OVERWORLD) return;
        if (weatherLogger != null && weatherLogger.isActive()) {
            weatherLogger.info("Thunder gradient: " +  new BigDecimal(thunderGradient).setScale(2, RoundingMode.HALF_UP).floatValue());
        }
        if (rainGradient == 0.0f) return; // It's not raining, so if if thunder was > 0 it wouldnt storm and if it thunder was 0 it would already be clear
        if (value == 0.0f) {
            if (thunderGradient == 0.0f) return; // We alread know it's not thundering
            MeteorClient.EVENT_BUS.post(WeatherEvent.Rain.get());
        } else {
            if (thunderGradient > 0.0f) return; // We alread know it's thundering
            MeteorClient.EVENT_BUS.post(WeatherEvent.Thunder.get());
            
        }
        
    }
    @Shadow
    public float rainGradient;
    @Shadow
    public float thunderGradient;
}

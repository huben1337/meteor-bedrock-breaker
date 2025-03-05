package com.huben.mixin;

import com.huben.util.RainGradientBugfixHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.huben.addon.events.world.WeatherEvent;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "joinWorld", at = @At("HEAD"))
    void joinWorld(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
        if (world.getRegistryKey() == World.OVERWORLD){
            RainGradientBugfixHelper.rainGradientDelta = 0.0f;
            RainGradientBugfixHelper.encounteredBug = false;
            if (world.isRaining()) {
                if (world.isThundering()) {
                    MeteorClient.EVENT_BUS.post(WeatherEvent.Thunder.get());
                } else {
                    MeteorClient.EVENT_BUS.post(WeatherEvent.Rain.get());
                }
            } else {
                MeteorClient.EVENT_BUS.post(WeatherEvent.Clear.get());
            }
        } else {
            MeteorClient.EVENT_BUS.post(WeatherEvent.None.get());
        }
    }
    
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    // Expected (Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V but found (Lnet/minecraft/class_437;ZLorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V
    public void disconnect(Screen disconnectionScreen, boolean transfer, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(WeatherEvent.None.get());
    }

    @Inject(method = "enterReconfiguration", at = @At("HEAD"))
    public void enterReconfiguration(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(WeatherEvent.None.get());
    }
}
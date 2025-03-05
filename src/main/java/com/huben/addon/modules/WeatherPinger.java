package com.huben.addon.modules;

import com.huben.addon.Addon;
import com.huben.addon.events.world.WeatherEvent;
import com.huben.gui.toast.WeatherToast;
import com.huben.settings.SoundEventSetting;
import com.huben.mixin.InactivityFpsLimiterAccessor;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;

public class WeatherPinger extends Module  {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private enum State {
        IDLE,
        PING_RAIN,
        PING_THUNDER,
        PING_CLEAR
    }

    private State state = State.IDLE;

    private final Setting<Boolean> pingOnThunder = sgGeneral.add(new BoolSetting.Builder()
        .name("ping-on-thunder")
        .description("Ping on thunder.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> thunderPingCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("thunder-ping-cool-down")
        .description("Delay between pings in ticks.")
        .defaultValue(20)
        .min(0)
        .max(40)
        .sliderMax(40)
        .visible(pingOnThunder::get)
        .build()
    );

    private final Setting<SoundEvent> pingOnThunderSound = sgGeneral.add(new SoundEventSetting.Builder()
        .name("ping-on-thunder-sound")
        .description("Sound to play on thunder.")
        .defaultValue(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value())
        .visible(pingOnThunder::get)
        .build()
    );

    private final Setting<Boolean> toastOnThunder = sgGeneral.add(new BoolSetting.Builder()
        .name("toast-on-thunder")
        .description("Toast on thunder.")
        .defaultValue(true)
        .build()
    );


    private final Setting<Boolean> pingOnRain = sgGeneral.add(new BoolSetting.Builder()
        .name("ping-on-rain")
        .description("Ping on rain.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> rainPingCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("rain-ping-cool-down")
        .description("Delay between pings in ticks.")
        .defaultValue(20)
        .min(0)
        .max(40)
        .sliderMax(40)
        .visible(pingOnRain::get)
        .build()
    );

    private final Setting<SoundEvent> pingOnRainSound = sgGeneral.add(new SoundEventSetting.Builder()
        .name("ping-on-rain-sound")
        .description("Sound to play on rain.")
        .defaultValue(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value())
        .visible(pingOnRain::get)
        .build()
    );

    private final Setting<Boolean> toastOnRain = sgGeneral.add(new BoolSetting.Builder()
        .name("toast-on-rain")
        .description("Toast on rain.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pingOnClear = sgGeneral.add(new BoolSetting.Builder()
        .name("ping-on-clear")
        .description("Ping on clear.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> clearPingCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("clear-ping-cool-down")
        .description("Delay between pings in ticks.")
        .defaultValue(20)
        .min(0)
        .max(40)
        .sliderMax(40)
        .visible(pingOnClear::get)
        .build()
    );

    private final Setting<SoundEvent> pingOnClearSound = sgGeneral.add(new SoundEventSetting.Builder()
        .name("ping-on-clear-sound")
        .description("Sound to play on clear.")
        .defaultValue(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value())
        .visible(pingOnClear::get)
        .build()
    );

    private final Setting<Boolean> toastOnClear = sgGeneral.add(new BoolSetting.Builder()
        .name("toast-on-clear")
        .description("Toast on clear.")
        .defaultValue(true)
        .build()
    );

    public WeatherPinger() {
        super(Addon.CATEGORY, "Weather Pinger", "Pings on weather changes.");
    }

    @EventHandler
    public void onClear (WeatherEvent.Clear event) {
        if (pingOnClear.get()) {
            if (state != State.PING_CLEAR) {
                if (isAfk()) {
                    state = State.PING_CLEAR;
                }
                pingClear();
            }
        } else {
            state = State.IDLE;
        }
        if (toastOnClear.get()) {
            mc.getToastManager().add(new WeatherToast.Clear());
        }
    }

    @EventHandler
    public void onRain (WeatherEvent.Rain event) {
        if (pingOnRain.get()) {
            if (state != State.PING_RAIN) {
                if (isAfk()) {
                    state = State.PING_RAIN;
                }
                pingRain();
            }
        } else {
            state = State.IDLE;
        }
        if (toastOnRain.get()) {
            mc.getToastManager().add(new WeatherToast.Rain());
        }
    }

    @EventHandler
    public void onThunder (WeatherEvent.Thunder event) {
        if (pingOnThunder.get()) {
            if (state != State.PING_THUNDER) {
                if (isAfk()) {
                    state = State.PING_THUNDER;
                }
                pingThunder();
            }
        } else {
            state = State.IDLE;
        }
        if (toastOnThunder.get()) {
            mc.getToastManager().add(new WeatherToast.Thunder());
        }
    }

    // public void setRainGradient (float value) {
    //     final WorldAccessor world = ((WorldAccessor) mc.world);
    //     if (value > 0.0f) {
    //         if (world._getRainGradient() > 0.0f) return; // We alread know it's raining
    //         if (world._getThunderGradient() == 0.0f) { // Only rain
    //             onRain();
    //         } else { // Thunderstorm
    //             onThunder();
    //         }
    //     } else {
    //         if (world._getRainGradient() == 0.0f) return; // We alread know it's not raining
    //         onClear(); // If it's not raining, it's clear
    //     }
    // }
    // 
    // public void setThunderGradient (float value) {
    //     final WorldAccessor world = ((WorldAccessor) mc.world);
    //     if (value > 0.0f) {
    //         if (world._getThunderGradient() > 0.0f) return; // We alread know it's thundering
    //         if (world._getRainGradient() == 0.0f) return; // It's not raining, so no thunder
    //         onThunder();
    //     } else {
    //         if (world._getThunderGradient() == 0.0f) return; // We alread know it's not thundering
    //         if (world._getRainGradient() == 0.0f) { // It's not raining, so it's clear
    //             onClear();
    //         } else { // It's raining
    //             onRain();
    //         }
    //     }
    // }

    private int timer = 0;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        switch (state) {
            case State.IDLE: {
                break;
            }
            case State.PING_RAIN: {
                if (timer++ < rainPingCooldown.get()) return;
                pingRain();
                break;
            }
            case State.PING_THUNDER: {
                if (timer++ < thunderPingCooldown.get()) return;
                pingThunder();
                break;
            }
            case State.PING_CLEAR: {
                if (timer++ < clearPingCooldown.get()) return;
                pingClear();
                break;
            }
        }
    }
    
    private static final int AFK_TIMEOUT = 3000;
    private boolean isAfk () {
        return ((InactivityFpsLimiterAccessor) mc.getInactivityFpsLimiter()).getLastInputTime() + AFK_TIMEOUT < Util.getMeasuringTimeMs();
    }

    private void pingRain () {
        timer = 0;
        mc.getSoundManager().play(PositionedSoundInstance.master(pingOnRainSound.get(), 1, 1));
    }
    private void pingThunder () {
        timer = 0;
        mc.getSoundManager().play(PositionedSoundInstance.master(pingOnThunderSound.get(), 1, 1));
    }
    private void pingClear () {
        timer = 0;
        mc.getSoundManager().play(PositionedSoundInstance.master(pingOnClearSound.get(), 1, 1));
    }

    public void onInput () {
        state = State.IDLE;
    }
}

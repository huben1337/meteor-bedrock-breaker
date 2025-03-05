package com.huben.addon;

import com.huben.addon.modules.BedrockBreaker;
import com.huben.addon.modules.ElytraBoostPlus;
import com.huben.addon.modules.ServerRotations;
import com.huben.addon.modules.WeatherLogger;
import com.huben.addon.modules.WeatherPinger;
import com.huben.settings.SoundEventSetting;
import com.huben.settings.SoundEventSettingScreen;
import com.huben.addon.hud.WorldWeatherHud;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Nooben Addon");
    public static final HudGroup HUD_GROUP = new HudGroup("Nooben HUD");

    public static BedrockBreaker bedrockBreaker = null;
    public static WeatherLogger weatherLogger = null;
    public static WeatherPinger weatherPinger = null;
    public static ElytraBoostPlus elytraBoostPlus = null;
    public static ServerRotations serverRotations = null;

    @Override
    public void onInitialize() {
        DefaultSettingsWidgetFactory.registerCustomFactory(SoundEventSetting.class, (theme) -> {
            return (table, _setting) -> {
                SoundEventSetting setting = (SoundEventSetting) _setting;

                WHorizontalList list = table.add(theme.horizontalList()).expandX().widget();

                WLabel slectedSound = list.add(theme.label(setting.toString())).widget();

                WButton selectButton = list.add(theme.button("Select")).widget();

                selectButton.action = () -> {
                    SoundEventSettingScreen screen = new SoundEventSettingScreen(theme, (SoundEventSetting) setting);
                    mc.setScreen(screen);
                    screen.onClosed(() -> {
                        slectedSound.set(setting.toString());
                    });
                };

                WButton resetButton = table.add(theme.button(GuiRenderer.RESET)).widget();
                resetButton.action = () -> {
                    setting.reset();
                    slectedSound.set(setting.toString());
                };
            };
        });
        LOG.info("Initializing Meteor Nooben Addon!");
        bedrockBreaker = new BedrockBreaker();
        weatherLogger = new WeatherLogger(); 
        weatherPinger = new WeatherPinger();
        elytraBoostPlus = new ElytraBoostPlus();
        serverRotations = new ServerRotations();
        final Modules modules = Modules.get();
        modules.add(bedrockBreaker);
        modules.add(weatherLogger);
        modules.add(weatherPinger);
        modules.add(elytraBoostPlus);
        modules.add(serverRotations);
        Hud.get().register(WorldWeatherHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.huben.addon";
    }
}

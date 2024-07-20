package com.huben.addon;

import com.huben.addon.modules.BedrockBreaker;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Nooben Addon");
    public static final HudGroup HUD_GROUP = new HudGroup("Bedrock Breaker");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Bedrock Breaker Addon!");

        // Modules
        Modules.get().add(new BedrockBreaker());
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

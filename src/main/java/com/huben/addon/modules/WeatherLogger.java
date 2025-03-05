package com.huben.addon.modules;

import com.huben.addon.Addon;

import meteordevelopment.meteorclient.systems.modules.Module;
public class WeatherLogger extends Module  {
    public WeatherLogger() {
        super(Addon.CATEGORY, "Weather Logger", "Logs weather events.");
    }
}

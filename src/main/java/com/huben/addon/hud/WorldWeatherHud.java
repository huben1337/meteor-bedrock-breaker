package com.huben.addon.hud;

import java.util.Objects;

import com.huben.addon.Addon;
import com.huben.addon.events.world.WeatherEvent;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;


public class WorldWeatherHud extends HudElement {
    public static final HudElementInfo<WorldWeatherHud> INFO = new HudElementInfo<>(Addon.HUD_GROUP, "World Weather", "Display world weather", WorldWeatherHud::new);


    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private int timer;

    public final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Update delay in ticks")
        .defaultValue(4)
        .onChanged(value -> {
            if (timer > value) timer = value;
        })
        .min(0)
        .build()
    );

    public final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .onChanged(value -> {
            updateTextSize(value);
        })
        .build()
    );

    public final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the text.")
        .defaultValue(0)
        .onChanged(value -> {
            updateElementSize(value);
        })
        .build()
    );

    public final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies custom text scale rather than the global one.")
        .defaultValue(false)
        .onChanged(value -> {
            if (value) {
                prevScale = 1;
                updateTextSize(1);
            } else {
                prevScale = -1;
                updateTextSize(-1);
            }
        })
        .build()
    );

    public final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .onChanged(value -> {
            prevScale = value;
            updateTextSize(value);
        })
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );


    public final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private static final String DEFAULT_TEXT = "Not available";
    
    private double textWidth = 0.0d;
    private double textHeight = 0.0d;
    private String prevText = DEFAULT_TEXT;
    private double prevScale = customScale.get() ? scale.get() : -1;
    private final double BASE_WIDTH = HudRenderer.INSTANCE.textWidth(DEFAULT_TEXT, shadow.get(), prevScale);


    public WorldWeatherHud () {
        super(INFO);
        updateTextSize();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onGameLeft (GameLeftEvent event) {
        updateText(DEFAULT_TEXT);
    }

    @Override
    public void onFontChanged () {
        updateTextSize();
    }

    private void updateTextSize () {
        updateTextSize(shadow.get(), prevScale, border.get());
    }

    private void updateTextSize (double scale) {
        updateTextSize(shadow.get(), scale, border.get());
    }

    private void updateTextSize (boolean shadow) {
        updateTextSize(shadow, prevScale, border.get());
    }

    private void updateTextSize (boolean shadow, double scale, int borderSize) {
        textWidth = HudRenderer.INSTANCE.textWidth(prevText, shadow, scale);
        textHeight = HudRenderer.INSTANCE.textHeight(shadow, scale);
        updateElementSize(borderSize);
    }

    private void updateElementSize (int borderSize) {
        int borderAdd = borderSize * 2;
        double elWidth = textWidth + borderAdd;
        double elHeight = textHeight + borderAdd;
        setSize(BASE_WIDTH + elWidth, elHeight);
    }

    private void updateText (String text) {
        prevText = text;
        textWidth = HudRenderer.INSTANCE.textWidth(text, shadow.get(), prevScale);
        double elWidth = textWidth + border.get() * 2;
        setSize(BASE_WIDTH + elWidth, getHeight());
    }

    @EventHandler
    public void onClear (WeatherEvent.Clear event) {
        updateText("Clear ☀️");
    }

    @EventHandler
    public void onRain (WeatherEvent.Rain event) {
        updateText("Rain 🌧️");
    }

    @EventHandler
    public void onThunder (WeatherEvent.Thunder event) {
        updateText("Thunder 🌩️");
    }

    @EventHandler
    public void onNoWeather (WeatherEvent.None event) {
        updateText(DEFAULT_TEXT);
    }

    @Override
    public void render(HudRenderer renderer) {
        final int borderSize = border.get();
        final boolean shadow = this.shadow.get();
        double x = getX() + borderSize;
        double y = getY() + borderSize;
        final Color lableColor = Objects.requireNonNullElse(Hud.get().textColors.get().get(0) , Color.WHITE);
        final Color valueColor = Objects.requireNonNullElse(Hud.get().textColors.get().get(1) , Color.LIGHT_GRAY);
        x = renderer.text("Weather: ", x, y, lableColor, shadow, prevScale);
        renderer.text(prevText, x, y, valueColor, shadow, prevScale);
        if (background.get()) {
            renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
        }
        
    }
}

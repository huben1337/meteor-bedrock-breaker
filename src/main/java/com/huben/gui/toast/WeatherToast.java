package com.huben.gui.toast;

import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;

public class WeatherToast extends ToastWithBackGround {

    private final Identifier ICON_TEXTURE;
    private final String TITLE;

    private WeatherToast(Identifier iconTexture, String title) {
        super();
        ICON_TEXTURE = iconTexture;
        TITLE = title;
    }

    public void onDraw(DrawContext context, TextRenderer textRenderer, long startTime) {
        final int x = BACKGROUND.CONTAINER_MIN_X;
        final int y = BACKGROUND.CONTAINER_MIN_Y;
        final int size = BACKGROUND.CONTAINER_MIN_SIZE;
        context.drawTexture(RenderLayer::getGuiTextured, ICON_TEXTURE, x, y, 0f, 0f, size, size, size, size);
        context.drawText(textRenderer, TITLE, x + size + 4, y + 4, 0xFFFFFF, false);
    }
    
    public static class Clear extends WeatherToast {
        public Clear() {
            // "It's clear!", "Weather Pinger"
            super(Identifier.of("nooben_addon", "textures/misc/clear.png"), "It's clear!");
        }
    }

    public static class Rain extends WeatherToast {
        public Rain() {
            // "It's raining!", "Weather Pinger"
            super(Identifier.of("nooben_addon", "textures/misc/rain.png"), "It's raining!");
        }
    }

    public static class Thunder extends WeatherToast {
        public Thunder() {
            // "It's thundering!", "Weather Pinger"
            super(Identifier.of("nooben_addon", "textures/misc/thunder.png"), "It's thundering!");
        }
    }
}

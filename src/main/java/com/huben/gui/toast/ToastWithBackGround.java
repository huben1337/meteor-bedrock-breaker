package com.huben.gui.toast;

import net.minecraft.util.Identifier;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;

public abstract class ToastWithBackGround extends CustomToast {
    public static class Background {
        // spceific to the used texture
        public Background (Identifier texture, int width, int height, int padding, int xOffsetStart, int xOffsetEnd, int yOffsetStart, int yOffsetEnd) {
            TEXTURE = texture;
            PADDING = padding;
            CONTAINER_MIN_X = PADDING + xOffsetStart;
            CONTAINER_MIN_Y = PADDING + yOffsetStart;
            CONTAINER_MAX_X = width - PADDING - xOffsetEnd;
            CONTAINER_MAX_Y = height - PADDING - yOffsetEnd;
            CONTAINER_HEIGHT = CONTAINER_MAX_Y - CONTAINER_MIN_Y;
            CONTAINER_WIDTH = CONTAINER_MAX_X - CONTAINER_MIN_X;
            CONTAINER_MIN_SIZE = Math.min(CONTAINER_HEIGHT, CONTAINER_WIDTH);
            CONTAINER_MAX_SIZE = Math.max(CONTAINER_HEIGHT, CONTAINER_WIDTH);
        }
        public Background (Identifier texture, int width, int height, int padding) {
            this(texture, width, height, padding, 0, 0, 0, 0);
        }
        public final Identifier TEXTURE;//  = Identifier.ofVanilla("toast/advancement");
    
        public final int PADDING; // = 0;

        public final int CONTAINER_MIN_X; // = PADDING + 3;
        public final int CONTAINER_MIN_Y; // = PADDING + 3;
        public final int CONTAINER_MAX_X; // = CONTAINER_MIN_X + CONTAINER_WIDTH;
        public final int CONTAINER_MAX_Y; // = CONTAINER_MIN_Y + CONTAINER_HEIGHT;
    
        public final int CONTAINER_HEIGHT; // = HEIGHT - 4 - 2 * PADDING;
        public final int CONTAINER_WIDTH; // = WIDTH - 4 - 2 * PADDING;
    
        public final int CONTAINER_MIN_SIZE; // = Math.min(CONTAINER_HEIGHT, CONTAINER_WIDTH);
        public final int CONTAINER_MAX_SIZE; // = Math.max(CONTAINER_HEIGHT, CONTAINER_WIDTH);
    }

    private static final Background DEFAULT_BACKGROUND = new Background(Identifier.ofVanilla("toast/advancement"), 160, 32, 4);

    public ToastWithBackGround (long duration, int width, int height, Background background) {
        super(duration, width, height);
        BACKGROUND = background;
    }

    public ToastWithBackGround (long duration, Background background) {
        super(duration);
        BACKGROUND = background;
    }

    public ToastWithBackGround (long duration) {
        super(duration);
        BACKGROUND = DEFAULT_BACKGROUND;
    }

    public ToastWithBackGround (Background background) {
        super();
        BACKGROUND = background;
    }

    public ToastWithBackGround () {
        super();
        BACKGROUND = DEFAULT_BACKGROUND;
    }

    protected final Background BACKGROUND;
    
    // private static final Identifier TEXTURE2 = Identifier.of("nooben_addon", "textures/misc/clear.png");

    

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, BACKGROUND.TEXTURE, 0, 0, WIDTH, HEIGHT);
        onDraw(context, textRenderer, startTime);
    }

    public abstract void onDraw(DrawContext context, TextRenderer textRenderer, long startTime);
    
}

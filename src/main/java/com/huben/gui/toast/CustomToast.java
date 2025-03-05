package com.huben.gui.toast;

import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.Toast;

public abstract class CustomToast implements Toast {

    public CustomToast (long duration, int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.DURATION = duration;
    }

    public CustomToast (long duration) {
        this(duration, 160, 32);
    }

    public CustomToast () {
        this(6000, 160, 32);
    }

    public final int HEIGHT;
    public final int WIDTH;
    private final long DURATION;

    private long startTime = 0;
    private boolean justUpdated = true;
    private Toast.Visibility visibility = Toast.Visibility.HIDE;

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (justUpdated) {
            startTime = time;
            justUpdated = false;
        }
        visibility = time - startTime >= DURATION ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
    
}

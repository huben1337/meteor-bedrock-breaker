package com.huben.addon.events.world;

public class JoinWorldEvent {
    static final JoinWorldEvent INSTANCE = new JoinWorldEvent();

    public static JoinWorldEvent get() {
        return INSTANCE;
    }
}

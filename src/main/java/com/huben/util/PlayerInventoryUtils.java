package com.huben.util;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public interface PlayerInventoryUtils {
    public static void setSelectedSlotSync (int slot) {
        mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }
}

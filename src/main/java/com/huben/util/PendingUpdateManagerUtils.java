package com.huben.util;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import com.huben.mixin.ClientWorldMixin;
import com.huben.mixin.PendingUpdateManagerMixin;

import net.minecraft.client.network.PendingUpdateManager;

public interface PendingUpdateManagerUtils {
    static int incrementSequence() {
        PendingUpdateManager pendingUpdateManager = ((ClientWorldMixin) mc.world).getPendingUpdateManager();
        int sequence = ((PendingUpdateManagerMixin) pendingUpdateManager).getSequence();
        ((PendingUpdateManagerMixin) pendingUpdateManager).setSequence(sequence + 1);
        return sequence;
    }
}

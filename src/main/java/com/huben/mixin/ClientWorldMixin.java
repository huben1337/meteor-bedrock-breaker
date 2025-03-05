package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.PendingUpdateManager;

@Mixin(ClientWorld.class)
public interface ClientWorldMixin {
    @Accessor("pendingUpdateManager")
    public PendingUpdateManager getPendingUpdateManager();
}

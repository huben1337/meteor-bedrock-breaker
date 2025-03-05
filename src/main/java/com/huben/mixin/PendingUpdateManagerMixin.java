package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.network.PendingUpdateManager;

@Mixin(PendingUpdateManager.class)
public interface PendingUpdateManagerMixin {
    @Accessor("sequence")
    public int getSequence();
    @Accessor("sequence")
    public void setSequence(int sequence);
}

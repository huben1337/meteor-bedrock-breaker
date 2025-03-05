package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerMixin {
    @Invoker("interactBlockInternal")
    public ActionResult invokeInteractBlockInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);
    @Invoker("sendSequencedPacket")
    public void invokeSendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);
    @Invoker("syncSelectedSlot")
    public void invokeSyncSelectedSlot();
}

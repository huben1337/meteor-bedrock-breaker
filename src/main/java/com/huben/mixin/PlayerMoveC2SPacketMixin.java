package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Mixin(PlayerMoveC2SPacket.class)
public abstract class PlayerMoveC2SPacketMixin {
    PlayerMoveC2SPacketMixin () {
        x = 0;
        y = 0;
        z = 0;
        yaw = 0;
        pitch = 0;
        onGround = false;
        horizontalCollision = false;
        changePosition = false;
        changeLook = false;
    }
    @Shadow protected final double x;
    @Shadow protected final double y;
    @Shadow protected final double z;
    @Shadow protected final float yaw;
    @Shadow protected final float pitch;
    @Shadow protected final boolean onGround;
    @Shadow protected final boolean horizontalCollision;
    @Shadow protected final boolean changePosition;
    @Shadow protected final boolean changeLook;
}




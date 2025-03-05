package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.huben.util.PlayerMoveC2SPacketUtils;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Mixin(PlayerMoveC2SPacket.LookAndOnGround.class)
public class PlayerMoveC2SPacketMixin$LookAndOnGround extends PlayerMoveC2SPacketMixin {
    /**
     * Writes the packet with custom yaw and pitch if desired.
     * @reason Enables server side only rotations.
     * @author huben1337
     */
    @Overwrite
    private void write (PacketByteBuf buf) {
        buf.writeFloat(PlayerMoveC2SPacketUtils.getDesiredYaw(this.yaw));
        buf.writeFloat(PlayerMoveC2SPacketUtils.getDesiredPitch(this.pitch));
        buf.writeByte(PlayerMoveC2SPacketUtils.toFlag(this.onGround, this.horizontalCollision));
     }
}




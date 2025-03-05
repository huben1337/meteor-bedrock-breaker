package com.huben.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlockPlacer {
    static public int place (MinecraftClient mc, BlockPos blockPos, ItemStack stack, int sequence) {
        BlockHitResult blockHitResult = new BlockHitResult(Vec3d.ofCenter(blockPos, 0), Direction.UP, blockPos, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, sequence));
        stack.useOnBlock(new ItemUsageContext(mc.player, Hand.MAIN_HAND, blockHitResult));
        return sequence + 1;
    }

    static public int placeMany (MinecraftClient mc, IndexedBlockPosGetter getIndexedPosition, int count, ItemStack stack, int sequence) {
        for (int i = 0; i < count; i++) {
            sequence = place(mc, getIndexedPosition.get(i), stack, sequence);
        }
        return sequence;
    }

    static public int placeMany (MinecraftClient mc, IndexedBlockPosGetter getIndexedPosition, int count, ItemStack stack, int sequence, PlayerMoveC2SPacketUtils.Yaw yaw) {
        PlayerMoveC2SPacketUtils.lockYawSync(yaw.value);
        sequence = placeMany(mc, getIndexedPosition, count, stack, sequence);
        PlayerMoveC2SPacketUtils.unlockYaw();
        return sequence;
    }

    static public int placeMany (MinecraftClient mc, IndexedBlockPosGetter getIndexedPosition, int count, ItemStack stack, int sequence, PlayerMoveC2SPacketUtils.Pitch pitch) {
        PlayerMoveC2SPacketUtils.lockPitchSync(pitch.value);
        sequence = placeMany(mc, getIndexedPosition, count, stack, sequence);
        PlayerMoveC2SPacketUtils.unlockPitch();
        return sequence;
    }

    static public int placeMany (MinecraftClient mc, IndexedBlockPosGetter getIndexedPosition, int count, ItemStack stack, int sequence, PlayerMoveC2SPacketUtils.Yaw yaw, PlayerMoveC2SPacketUtils.Pitch pitch) {
        PlayerMoveC2SPacketUtils.lockLookSync(yaw.value, pitch.value);
        sequence = placeMany(mc, getIndexedPosition, count, stack, sequence);
        PlayerMoveC2SPacketUtils.unlockLook();
        return sequence;
    }
}

package com.huben.util;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class DelayedBlockBreaker {
    public DelayedBlockBreaker (int count, Runnable onDone, IndexedBlockPosGetter getIndexedPosition) {
        this.count = count;
        this.sequencePairs = new SequencePair[count];
        this.onDone = onDone;
        this.getIndexedPosition = getIndexedPosition;
    }
    private final int count;
    private int index = 0;
    private final SequencePair[] sequencePairs;
    private float progress = 0.0f;
    private final Runnable onDone;
    private final IndexedBlockPosGetter getIndexedPosition;
    // private String targetId = null;

    static final float MINE_SPEED = 1.0f;
    static final boolean SEND_HAND_SWING = true;

    public DelayedBlockBreaker setTargetId (String targetId) {
        // this.targetId = targetId;
        return this;
    }

    public void tick () {
        final BlockPos blockPos = getIndexedPosition.get(index);
        final BlockState blockState = mc.world.getBlockState(blockPos);
        final SequencePair breakSequencePair = sequencePairs[index];
        if (breakSequencePair == null) {
            int sequence = PendingUpdateManagerUtils.incrementSequence();
            sequencePairs[index] = new SequencePair(sequence);;
            progress = blockState.calcBlockBreakingDelta(mc.player, mc.player.getWorld(), blockPos);
            if (progress >= 1.0f) {
                mc.interactionManager.breakBlock(blockPos);
                next();
            }
        } else {
            progress += blockState.calcBlockBreakingDelta(mc.player, mc.player.getWorld(), blockPos);
            if (progress >= 1.0f) {
                mc.interactionManager.breakBlock(blockPos);
                int sequence = PendingUpdateManagerUtils.incrementSequence();
                assert breakSequencePair.getLast() == -1;
                breakSequencePair.setLast(sequence);
                next();
            }
        }
        if (SEND_HAND_SWING) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void next () {
        progress = 0.0f;
        index++;
        if (index >= count) {
            onDone.run();
        } else {
            tick();
        }
    }

    public void apply () {
        if (index != count) {
            throw new IllegalStateException("index != count");
        }
        final ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        for (int i = 0; i < count; i++) {
            final SequencePair breakSequencePair = sequencePairs[i];
            if (breakSequencePair == null) {
                throw new IllegalStateException("breakSequencePair is null at index " + i);
            }
            BlockPos blockPos = getIndexedPosition.get(i);
            assert breakSequencePair.hasFirst();
            networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos), breakSequencePair.getFirst()));
            if (breakSequencePair.hasLast()) {
                // System.out.println("Has last targetId: " + targetId);
                networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos), breakSequencePair.getLast()));
            }
        }
    }
}
package com.huben.util;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public abstract class PlayerMoveC2SPacketUtils {
    public static class Yaw {
        final float value;
        public Yaw (float value) {
            this.value = value;
        }
        public void lockTo () {
            PlayerMoveC2SPacketUtils.lockYaw(value);
        }
        public void lockToSync () {
            PlayerMoveC2SPacketUtils.lockYawSync(value);
        }
        public static final Yaw NORTH = new Yaw(0.0f);
        public static final Yaw EAST = new Yaw(90.0f);
        public static final Yaw SOUTH = new Yaw(180.0f);
        public static final Yaw WEST = new Yaw(-90.0f);
        public static void unlock () {
            PlayerMoveC2SPacketUtils.unlockYaw();
        }
    }
    public static class Pitch {
        final float value;
        public Pitch (float value) {
            this.value = value;
        }
        public void lockTo () {
            PlayerMoveC2SPacketUtils.lockPitch(value);
        }
        public void lockToSync () {
            PlayerMoveC2SPacketUtils.lockPitchSync(value);
        }
        public static final Pitch UP = new Pitch(-90.0f);
        public static final Pitch FLAT = new Pitch(0.0f);
        public static final Pitch DOWN = new Pitch(90.0f);
        public static void unlock () {
            PlayerMoveC2SPacketUtils.unlockPitch();
        }
    }
    static float lockedYaw = Float.NaN;
    static float lockedPitch = Float.NaN;
    public static int toFlag(boolean changePosition, boolean changeLook) {
        int i = 0;
        if (changePosition) {
            i |= 1;
        }

        if (changeLook) {
            i |= 2;
        }

        return i;
    }

    public static void lockLook (float yaw, float pitch) {
        lockedYaw = yaw;
        lockedPitch = pitch;
    }
    public static void lockLookSync (float yaw, float pitch) {
        syncLook(yaw, pitch);
        lockedYaw = yaw;
        lockedPitch = pitch;
    }
    public static void unlockLook () {
        lockedYaw = Float.NaN;
        lockedPitch = Float.NaN;
    }    
    public static void unlockLookSync () {
        lockedYaw = Float.NaN;
        lockedPitch = Float.NaN;
        syncLook();
    }  

    public static void lockYaw (float yaw) {
        lockedYaw = yaw;
    }
    public static void lockYawSync (float yaw) {
        syncYaw(yaw);
        lockedYaw = yaw;
    }
    public static void unlockYaw () {
        lockedYaw = Float.NaN;
    }
    public static void unlockYawSync () {
        lockedYaw = Float.NaN;
        syncLook();
    }
    public static boolean isYawLocked () {
        return !Float.isNaN(lockedYaw);
    }
    public static float getLockedYaw () {
        return lockedYaw;
    }
    public static float getDesiredYaw (float yaw) {
        return isYawLocked() ? getLockedYaw() : yaw;
    }


    public static void lockPitch (float pitch) {
        lockedPitch = pitch;
    }
    public static void lockPitchSync (float pitch) {
        syncPitch(pitch);
        lockedPitch = pitch;
    }
    public static void unlockPitch () {
        lockedPitch = Float.NaN;
    }
    public static void unlockPitchSync () {
        lockedPitch = Float.NaN;
        syncLook();
    }
    public static boolean isPitchLocked () {
        return !Float.isNaN(lockedPitch);
    }
    public static float getLockedPitch () {
        return lockedPitch;
    }
    public static float getDesiredPitch (float pitch) {
        return isPitchLocked() ? getLockedPitch() : pitch;
    }

    public static void syncLook () {
        syncLook(mc.player.getYaw(), mc.player.getPitch());
    }
    public static void syncYaw (float yaw) {
        syncLook(yaw, mc.player.getPitch());
    }
    public static void syncPitch (float pitch) {
        syncLook(mc.player.getYaw(), pitch);
    }
    public static void syncLook (float yaw, float pitch) {
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
    }
}

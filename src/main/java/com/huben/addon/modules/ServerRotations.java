package com.huben.addon.modules;

import static com.huben.addon.Addon.bedrockBreaker;

import com.huben.addon.Addon;
import com.huben.util.PlayerMoveC2SPacketUtils;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ServerRotations extends Module  {

    private final SettingGroup sgYaw = settings.createGroup("Yaw");
    private final SettingGroup sgPitch = settings.createGroup("Pitch");
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> unlockSync = sgGeneral.add(new BoolSetting.Builder()
        .name("Unlock Sync")
        .description("Unlocks your rotation and syncs it to the server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> lockYaw = sgYaw.add(new BoolSetting.Builder()
        .name("Lock Yaw")
        .description("Locks your yaw.")
        .defaultValue(true)
        .onChanged(value -> {
            if (!this.isActive()) return;
            if (value) {
                lockYaw();
            } else {
                PlayerMoveC2SPacketUtils.unlockYaw();
                if (unlockSync.get()) {
                    PlayerMoveC2SPacketUtils.syncLook();
                }
                
            }
        })
        .build()
    );

    private float customYaw = 0;

    private final Setting<Boolean> lockCurrentYaw = sgYaw.add(new BoolSetting.Builder()
        .name("Lock Current Yaw")
        .description("Locks your current yaw.")
        .defaultValue(true)
        .visible(lockYaw::get)
        .onChanged(value -> {
            if (!this.isActive()) return;
            if (value) {
                PlayerMoveC2SPacketUtils.lockYawSync(mc.player.getYaw());
            } else {
                PlayerMoveC2SPacketUtils.lockYawSync(customYaw);
            }
        })
        .build()
    );

    @SuppressWarnings("unused")
    private final Setting<Double> yawAngle = sgYaw.add(new DoubleSetting.Builder()
        .name("yaw-angle")
        .description("Yaw angle in degrees.")
        .defaultValue(0)
        .sliderMax(360)
        .max(360)
        .visible(() -> lockYaw.get() && !lockCurrentYaw.get())
        .onChanged(value -> {
            customYaw = value.floatValue();
            if (!this.isActive()) return;
            PlayerMoveC2SPacketUtils.lockYawSync(value.floatValue());
        })
        .build()
    );



    private final Setting<Boolean> lockPitch = sgPitch.add(new BoolSetting.Builder()
        .name("Lock Pitch")
        .description("Locks your pitch.")
        .defaultValue(true)
        .onChanged(value -> {
            if (!this.isActive()) return;
            if (value) {
                lockPitch();
            } else {
                PlayerMoveC2SPacketUtils.unlockPitch();
            }
        })
        .build()
    );

    private float customPitch = 0;

    private final Setting<Boolean> lockCurrentPitch = sgPitch.add(new BoolSetting.Builder()
        .name("Lock Current Pitch")
        .description("Locks your current pitch.")
        .defaultValue(true)
        .visible(lockPitch::get)
        .onChanged(value -> {
            if (!this.isActive()) return;
            if (value) {
                PlayerMoveC2SPacketUtils.lockPitchSync(mc.player.getPitch());
            } else {
                PlayerMoveC2SPacketUtils.lockPitchSync(customPitch);
            }
        })
        .build()
    );

    @SuppressWarnings("unused")
    private final Setting<Double> pitchAngle = sgPitch.add(new DoubleSetting.Builder()
        .name("pitch-angle")
        .description("Pitch angle in degrees.")
        .defaultValue(0)
        .range(-90, 90)
        .sliderRange(-90, 90)
        .visible(() -> lockPitch.get() && !lockCurrentPitch.get())
        .onChanged(value -> {
            customPitch = value.floatValue();
            if (!this.isActive()) return;
            PlayerMoveC2SPacketUtils.lockPitchSync(value.floatValue());
        })
        .build()
    );

    public ServerRotations() {
        super(Addon.CATEGORY, "Server Rotations", "Locks your roatation server side.");
    }

    @Override
    public void toggle() {
        if (bedrockBreaker != null && bedrockBreaker.isActive()) {
            info("You cannot use this module while Bedrock Breaker is active.");
            return;
        }
        super.toggle();
    }

    public void onActivate () {
        if (lockYaw.get()) {
            lockYaw();
        }
        if (lockPitch.get()) {
            lockPitch();
        }
    }

    private void lockYaw () {
        PlayerMoveC2SPacketUtils.lockYawSync(lockCurrentYaw.get() ? mc.player.getYaw() : customYaw);
    }

    private void lockPitch () {
        PlayerMoveC2SPacketUtils.lockPitchSync(lockCurrentPitch.get() ? mc.player.getPitch() : customPitch);
    }

    public void onDeactivate () {
        PlayerMoveC2SPacketUtils.unlockLookSync();
    }

    
}

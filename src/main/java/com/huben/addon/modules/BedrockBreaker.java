package com.huben.addon.modules;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

import com.huben.addon.Addon;

import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class BedrockBreaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Keybind> breakBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("break-bind")
        .description("Bind to break the bedrock.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_ENTER))
        .build()
    );

    private final Setting<Integer> breakWidth = sgGeneral.add(new IntSetting.Builder()
        .name("break-width")
        .description("The width of the bounding box.")
        .defaultValue(11)
        .min(1)
        .max(11)
        .sliderMax(11)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the bounding box.")
        .defaultValue(new SettingColor(16,106,144, 100))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the bounding box.")
        .defaultValue(new SettingColor(16,106,144, 255))
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("box-mode")
        .description("How the shape for the bounding box is rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    public BedrockBreaker() {
        super(Addon.CATEGORY, "bedrock-breaker", "An module to make breaking bedrock easier.");
    }

    @Override
    public void onActivate() {
        /* if (mc.player.isCreative()) {
            info("Dumbass. You dont need this module in creative mode.");
            return;
        } */

        info("Waiting for user input...");
    }

    @Override
    public void onDeactivate() {
        currentState = State.IDLE;
        this.releasePackets();
        BedrockBreaker.clearPackets();
    }

    private static ArrayList<Packet<?>> delayedPackets = new ArrayList<>();

    private Vector3i pos1 = new Vector3i(0, 0, 0);
    private Vector2i pos2 = new Vector2i(0, 0);

    private int direction = 0;

    int timer = 0;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        cancelRotations = false;
        assert mc.player != null;
        direction = Math.floorMod(Math.round((mc.player.getRotationClient().y % 360) / 90), 4);

        double pX = mc.player.getX();
        double pY = mc.player.getY();
        double pZ = mc.player.getZ();


        int pX_ = (int) Math.floor(pX);
        int pY_ = (int) Math.floor(pY);
        int pZ_ = (int) Math.floor(pZ);

        double hitboxOffset = 0.3;
        double range = breakWidth.get() / 2d;
        int left = (int) Math.ceil(range);
        int right = (int) Math.floor(range);

        switch (direction) {
            case 0:
                pZ_ = (int) Math.floor(pZ + hitboxOffset);
                pos1.set(pX_ - right, pY_, pZ_ + 1);
                pos2.set(pX_ + left, pZ_ + 2);
                break;
            case 1:
                pX_ = (int) Math.floor(pX - hitboxOffset);
                pos1.set(pX_ - 1, pY_, pZ_ - right);
                pos2.set(pX_, pZ_ + left );
                break;
            case 2:
                pZ_ = (int) Math.floor(pZ - hitboxOffset);
                pos1.set(pX_ - right, pY_, pZ_ - 1);
                pos2.set(pX_ + left, pZ_);
                break;
            case 3:
                pX_ = (int) Math.floor(pX + hitboxOffset);
                pos1.set(pX_ + 1, pY_, pZ_ - right);
                pos2.set(pX_ + 2, pZ_ + left);
                break;
        }


        if (currentState == State.PLACING) {
            if (placeBlocks()) {
                currentState = State.BREAKING;
                info("Placed blocks. Now breaking...");
            } else {
                currentState = State.IDLE;
            }
        } else if (currentState == State.BREAKING) {
            if (breakBlocks()) {
                currentState = State.REPLACING;
                info("Broken blocks. Now replacing...");
            }
        } else if (currentState == State.REPLACING) {
            if (replaceBlocks()) {
                info("Replaced blocks.");
            } else {
                info("Failed to replace blocks. Waiting for user input...");
            }
            currentState = State.DONE;

        } else if (currentState == State.DONE) {
            timer++;
            if (timer >= 2) {
                timer = 0;
                currentState = State.IDLE;
                releasePackets();
                info("Done! Waiting for user input...");
            }
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press || !breakBind.get().isPressed() || mc.currentScreen != null) {
            return;
        }
        if (currentState == State.IDLE) {
            info("Placing blocks...");
            currentState = State.PLACING;
        } else if (currentState == State.WAIT_TO_BREAK) {
            info("Breaking blocks...");
            currentState = State.BREAKING;
        } else if (currentState == State.WAIT_TO_REPLACE) {
            info("Replacing blocks...");
            currentState = State.REPLACING;
            return;
        }
    }

    private BlockPos[] pistonPositions = null;
    private BlockPos[] torchPositions = null;

    private boolean placeBlocks () {
        if (mc.player == null) return false;
        int pistonSlot = -1;
        int pistonCount = 0;

        int torchSlot = -1;
        int torchCount = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.PISTON)) {
                if (pistonSlot == -1) pistonSlot = i;
                pistonCount += stack.getCount();
            } else if (stack.isOf(Items.REDSTONE_TORCH)) {
                if (torchSlot == -1) torchSlot = i;
                torchCount += stack.getCount();
            }
        }

        if (pistonSlot == -1 || torchSlot == -1) {
            info("No piston or torch found. Waiting for user input...");
            return false;
        }

        int width = breakWidth.get();

        if (pistonCount < width * 2 || torchCount < width) {
            info("Not enough blocks found. Waiting for user input...");
            return false;
        }

        BlockPos start = new BlockPos(pos1.x, pos1.y, pos1.z);

        pistonPositions = new BlockPos[width];
        torchPositions = new BlockPos[width];

        for (int i = 0; i < width; i++) {
            BlockPos pistonBlockPos = null;
            BlockPos torchBlockPos = switch (direction) {
                case 0 -> {
                    pistonBlockPos = start.add(i, 0, 0);
                    yield start.add(i, 0, -1);
                }
                case 1 -> {
                    pistonBlockPos = start.add(0, 0, i);
                    yield start.add(1, 0, i);
                }
                case 2 -> {
                    pistonBlockPos = start.add(i, 0, 0);
                    yield start.add(i, 0, 1);
                }
                case 3 -> {
                    pistonBlockPos = start.add(0, 0, i);
                    yield start.add(-1, 0, i);
                }
                default -> null;
            };

            if (pistonBlockPos == null
                || torchBlockPos == null
                || !BlockUtils.canPlaceBlock(pistonBlockPos, false, Blocks.PISTON)
                || !BlockUtils.canPlaceBlock(torchBlockPos, false, Blocks.REDSTONE_TORCH)
            ) {
                info("Blocks could not be placed. Waiting for user input...");
                return false;
            }

            pistonPositions[i] = pistonBlockPos;
            torchPositions[i] = torchBlockPos;
        }

        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90, mc.player.isOnGround(),         mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0));

        cancelRotations = true;

        int previousSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = pistonSlot;
        for (BlockPos pistonBlockPos : pistonPositions) {
            BlockHitResult pistonBhr = new BlockHitResult(Vec3d.ofCenter(pistonBlockPos, 0), Direction.UP, pistonBlockPos, false);
            assert mc.interactionManager != null;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, pistonBhr);
        }

        mc.player.getInventory().selectedSlot = torchSlot;
        for (BlockPos torchBlockPos : torchPositions) {
            BlockHitResult torchBhr = new BlockHitResult(Vec3d.ofCenter(torchBlockPos, 0), Direction.UP, torchBlockPos, false);
            assert mc.interactionManager != null;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, torchBhr);
        }

        mc.player.getInventory().selectedSlot = previousSlot;
        assert mc.interactionManager != null;
        ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();

        return true;
    }

    private boolean breakBlocks () {
        if (torchPositions == null || pistonPositions == null) return false;
        boolean done = true;
        for (BlockPos torchBlockPos : torchPositions) {
            assert mc.world != null;
            if (!mc.world.getBlockState(torchBlockPos).isAir()) {
                done = false;
                BlockUtils.breakBlock(torchBlockPos, true);
            }

        }
        for (BlockPos pistonBlockPos : pistonPositions) {
            assert mc.world != null;
            if (!mc.world.getBlockState(pistonBlockPos).isAir()) {
                BlockUtils.breakBlock(pistonBlockPos, true);
                return false;
            }
        }

        return done;
    }

    private boolean replaceBlocks () {
        if (pistonPositions == null) return false;
        int pistonSlot = -1;
        int pistonCount = 0;
        for (int i = 0; i < 9; i++) {
            assert mc.player != null;
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.PISTON)) {
                if (pistonSlot == -1) pistonSlot = i;
                pistonCount += stack.getCount();
            }
        }
        if (pistonSlot == -1 || pistonCount < breakWidth.get()) return false;

        int previousSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = pistonSlot;



        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), -89, mc.player.isOnGround(), mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0));

        cancelRotations = true;

        for (BlockPos pistonBlockPos : pistonPositions) {
            if (!BlockUtils.canPlaceBlock(pistonBlockPos, false, Blocks.PISTON)) return false;
            BlockHitResult pistonBhr = new BlockHitResult(Vec3d.ofCenter(pistonBlockPos), Direction.DOWN, pistonBlockPos, false);
            assert mc.interactionManager != null;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, pistonBhr);
        }

        mc.player.getInventory().selectedSlot = previousSlot;
        assert mc.interactionManager != null;
        ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();

        return true;
    }

    private State currentState = State.IDLE;

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (currentState != State.BREAKING) {
            double x1 =  pos1.x;
            double y =  pos1.y;
            double z1 =  pos1.z;
            double x2 =  pos2.x;
            double z2 =  pos2.y;
            event.renderer.side(x1, y, z1, x2, y, z1, x2, y, z2, x1, y, z2, sideColor.get(), lineColor.get(), shapeMode.get());
        }
    }

    public static final Class<?>[] blockedPackets = {
        PlayerActionC2SPacket.class,
        PlayerInputC2SPacket.class,
        PlayerInteractBlockC2SPacket.class,
        PlayerInteractItemC2SPacket.class,
        UpdateSelectedSlotC2SPacket.class
    };

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSendPacket(PacketEvent.Send event) {
        Packet<?> packet = event.packet;
        if (doDelayPckets() && Arrays.stream(blockedPackets).anyMatch(c -> c.isInstance(packet))) {
            delayPacket(packet);
            event.cancel();
        }
        if (cancelRotations && packet instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }

    private boolean doDelayPckets () {
        return (currentState == State.BREAKING || currentState == State.WAIT_TO_REPLACE || currentState == State.REPLACING);
    }

    boolean cancelRotations = false;

    static enum State {
        IDLE,
        PLACING,
        WAIT_TO_BREAK,
        BREAKING,
        WAIT_TO_REPLACE,
        REPLACING,
        DONE,
    }

    public static void delayPacket(Packet<?> p) {
        delayedPackets.add(p);
    }

    public static void clearPackets() {
        delayedPackets.clear();
    }

    private void releasePackets() {
        for (Packet<?> packet : delayedPackets) {
            Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packet);
        }
        delayedPackets.clear();
    }
}

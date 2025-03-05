package com.huben.addon.modules;

import static com.huben.addon.Addon.serverRotations;

import org.lwjgl.glfw.GLFW;

import com.huben.util.BlockPlacer;
import com.huben.util.DelayedBlockBreaker;
import com.huben.util.PlayerInventoryUtils;
import com.huben.util.PlayerMoveC2SPacketUtils;
import com.huben.util.XZPlane;
import com.huben.addon.Addon;
import com.huben.mixin.ClientWorldMixin;
import com.huben.mixin.PendingUpdateManagerMixin;

import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.item.Items;


public class BedrockBreaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> placeApplyTicks = sgGeneral.add(new IntSetting.Builder()
        .name("place-apply-ticks")
        .description("Ticks to apply block placing.")
        .defaultValue(0)
        .min(0)
        .max(40)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> breakTorchesApplyTicks = sgGeneral.add(new IntSetting.Builder()
        .name("break-torches-apply-ticks")
        .description("Ticks to apply torch breaking.")
        .defaultValue(0)
        .min(0)
        .max(40)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> breakPistonsApplyTicks = sgGeneral.add(new IntSetting.Builder()
        .name("break-pistons-apply-ticks")
        .description("Ticks to apply piston breaking.")
        .defaultValue(0)
        .min(0)
        .max(40)
        .sliderMax(10)
        .build()
    );

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
        .max(63)
        .sliderMax(13)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("box-mode")
        .description("How the shape for the bounding box is rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> targetSideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the bounding box.")
        .defaultValue(new SettingColor(16,106,144, 100))
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .build()
    );

    private final Setting<SettingColor> targetLineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the bounding box.")
        .defaultValue(new SettingColor(16,106,144, 255))
        .visible(() -> shapeMode.get() != ShapeMode.Sides)
        .build()
    );

    private final Setting<SettingColor> torchSideColor = sgRender.add(new ColorSetting.Builder()
        .name("torch-side-color")
        .description("The side color of the bounding box.")
        .defaultValue(new SettingColor(255,106,100, 50))
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .build()
    );

    private final Setting<SettingColor> torchLineColor = sgRender.add(new ColorSetting.Builder()
        .name("torch-line-color")
        .description("The line color of the bounding box.")
        .defaultValue(new SettingColor(255,100,144, 50))
        .visible(() -> shapeMode.get() != ShapeMode.Sides)
        .build()
    );

    public BedrockBreaker() {
        super(Addon.CATEGORY, "bedrock-breaker", "A module to make breaking bedrock easier.");
    }

    @Override
    public void toggle() {
        if (serverRotations != null && serverRotations.isActive()) {
            info("You cannot use this module while Server Rotations is active.");
            return;
        }
        super.toggle();
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
        PlayerMoveC2SPacketUtils.unlockPitch();
        currentState = State.IDLE;
    }


    private XZPlane targetPlane = new XZPlane(0, 0, 0, 0, 0);
    private XZPlane torchPlane = new XZPlane(0, 0, 0, 0, 0);
    private int width = 0;

    int ticks = 0;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        switch (currentState) {
        case State.IDLE: {
            int direction = Math.floorMod(Math.round((mc.player.getRotationClient().y % 360) / 90), 4);

            double pX = mc.player.getX();
            double pY = mc.player.getY();
            double pZ = mc.player.getZ();


            int pX_ = (int) Math.floor(pX);
            int pY_ = (int) Math.floor(pY);
            int pZ_ = (int) Math.floor(pZ);

            final double hitboxOffset = 0.3d;
            double range = breakWidth.get() / 2.0d;
            int left = (int) Math.ceil(range);
            int right = (int) Math.floor(range);

            switch (direction) {
                case 0:
                    pZ_ = (int) Math.floor(pZ + hitboxOffset);
                    torchPlane = new XZPlane(pX_ - right, pX_ + left, pY_, pZ_, pZ_ + 1);
                    targetPlane = torchPlane.translate(0, 0, 1);
                    break;
                case 1:
                    pX_ = (int) Math.floor(pX - hitboxOffset);
                    torchPlane = new XZPlane(pX_, pX_ + 1, pY_, pZ_ - right, pZ_ + left);
                    targetPlane = torchPlane.translate(-1, 0, 0);
                    break;
                case 2:
                    pZ_ = (int) Math.floor(pZ - hitboxOffset);
                    torchPlane = new XZPlane(pX_ - right, pX_ + left, pY_, pZ_, pZ_ + 1);
                    targetPlane = torchPlane.translate(0, 0, -1);
                    break;
                case 3:
                    pX_ = (int) Math.floor(pX + hitboxOffset);
                    torchPlane = new XZPlane(pX_, pX_ + 1, pY_, pZ_ - right, pZ_ + left);
                    targetPlane = torchPlane.translate(1, 0, 0);
                    break;
            }

            break;
        }
        case State.PLACE: {
            placeBlocks();
            break;
        }
        case State.APPLY_PLACE: {
            if (++ticks >= placeApplyTicks.get()) {
                ticks = 0;
                currentState = State.BREAK_TOCHES;
            }
            break;
        }
        case State.BREAK_TOCHES: {
            torchBreaker.tick();
            break;
        }
        case State.APPLY_BREAK_TORCHES: {
            if (++ticks >= breakTorchesApplyTicks.get()) {
                ticks = 0;
                currentState = State.BREAK_PISTONS;
            }
            break;
        }
        case State.BREAK_PISTONS: {
            pistonBreaker.tick();
            break;
        }
        case State.APPLY_BREAK_PISTONS: {
            if (++ticks >= breakPistonsApplyTicks.get()) {
                ticks = 0;
                releasePackets();
            }
            break;
        }
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press || !breakBind.get().isPressed() || mc.currentScreen != null) return;
        switch (currentState) {
            case State.IDLE:
                info("Placing blocks...");
                currentState = State.PLACE;
                break;
            default:
        }
    }

    private BlockPos getXZPlaneBlockPos (XZPlane plane, int index) {
        int delataX = plane.x2 - plane.x1;
        int delataZ = plane.z2 - plane.z1;
        return new BlockPos(plane.x1 + (index % delataX), plane.y, plane.z1 + (index % delataZ));
    }

    private DelayedBlockBreaker pistonBreaker = null;

    private DelayedBlockBreaker torchBreaker = null;

    private void placeBlocks () {
        int pistonSlot = -1;
        ItemStack pistonStack = null;

        int torchSlot = -1;
        ItemStack torchStack = null;

        width = breakWidth.get();
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (pistonSlot == -1 && stack.isOf(Items.PISTON) && stack.getCount() >= width) {
                pistonSlot = i;
                pistonStack = stack;
            } else if (torchSlot == -1 && stack.isOf(Items.REDSTONE_TORCH) && stack.getCount() >= width) {
                torchSlot = i;
                torchStack = stack;
            }
        }

        if (pistonSlot == -1 || torchSlot == -1) {
            info("Not enough blocks found. Waiting for user input...");
            currentState = State.IDLE;
            return;
        }

        for (int i = 0; i < width; i++) {
            BlockPos pistonBlockPos = getXZPlaneBlockPos(targetPlane, i);
            BlockPos torchBlockPos = getXZPlaneBlockPos(torchPlane, i);

            if (!BlockUtils.canPlaceBlock(pistonBlockPos, false, Blocks.PISTON) || !BlockUtils.canPlaceBlock(torchBlockPos, false, Blocks.REDSTONE_TORCH)) {
                info("Blocks could not be placed. Waiting for user input...");
                currentState = State.IDLE;
                return;
            }
        }

        int previousSlot = mc.player.getInventory().selectedSlot;

        PendingUpdateManager pendingUpdateManager = ((ClientWorldMixin) mc.world).getPendingUpdateManager();
        int sequence = pendingUpdateManager.getSequence();

        PlayerInventoryUtils.setSelectedSlotSync(pistonSlot);
        sequence = BlockPlacer.placeMany(mc, (index) -> {
            return getXZPlaneBlockPos(targetPlane, index);
        }, width, pistonStack, sequence, PlayerMoveC2SPacketUtils.Pitch.DOWN);

        PlayerInventoryUtils.setSelectedSlotSync(torchSlot);
        sequence = BlockPlacer.placeMany(mc, (index) -> {
            return getXZPlaneBlockPos(torchPlane, index);
        }, width, torchStack, sequence);

        ((PendingUpdateManagerMixin) pendingUpdateManager).setSequence(sequence);

        PlayerInventoryUtils.setSelectedSlotSync(previousSlot);

        torchBreaker = new DelayedBlockBreaker(
            width,
            () -> {

                pistonBreaker = new DelayedBlockBreaker(
                    width,
                    () -> {
                        if (breakPistonsApplyTicks.get() == 0) {
                            releasePackets();
                        } else {
                            currentState = State.APPLY_BREAK_PISTONS;
                        }
                    },
                    (index) -> {
                        return getXZPlaneBlockPos(targetPlane, index);
                    }
                ).setTargetId("pistonBreaker");

                if (breakTorchesApplyTicks.get() == 0) {
                    currentState = State.BREAK_PISTONS;
                    pistonBreaker.tick();
                } else {
                    currentState = State.APPLY_BREAK_TORCHES;
                }
            },
            (index) -> {
                return getXZPlaneBlockPos(torchPlane, index);
            }
        ).setTargetId("torchBreaker");

        if (placeApplyTicks.get() == 0) {
            currentState = State.BREAK_TOCHES;
            torchBreaker.tick();
        } else {
            currentState = State.APPLY_PLACE;
        }
    }

    private State currentState = State.IDLE;

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (currentState == State.IDLE) {
            targetPlane.render(event.renderer, targetSideColor.get(), targetLineColor.get(), shapeMode.get());
            torchPlane.render(event.renderer, torchSideColor.get(), torchLineColor.get(), shapeMode.get());
        }
    }

    static enum State {
        IDLE,
        PLACE,
        APPLY_PLACE,
        BREAK_TOCHES,
        APPLY_BREAK_TORCHES,
        BREAK_PISTONS,
        APPLY_BREAK_PISTONS,
    }

    private void releasePackets() {
        info("Done! Waiting for user input...");
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        assert networkHandler != null;

        assert torchBreaker != null;
        torchBreaker.apply();

        assert pistonBreaker != null;
        pistonBreaker.apply();


        int pistonSlot = -1;
        ItemStack pistonStack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (pistonSlot == -1 && stack.isOf(Items.PISTON) && stack.getCount() >= width) {
                pistonSlot = i;
                pistonStack = stack;
                break;
            }
        }
        if (pistonSlot == -1) {
            info("Not enough pistons found for replacement.");
            currentState = State.IDLE;
            return;
        };

        int previousSlot = mc.player.getInventory().selectedSlot;

        PlayerInventoryUtils.setSelectedSlotSync(pistonSlot); 


        PendingUpdateManager pendingUpdateManager = ((ClientWorldMixin) mc.world).getPendingUpdateManager();
        int sequence = pendingUpdateManager.getSequence();

        for (int i = 0; i < width; i++) {
            BlockPos pistonBlockPos = getXZPlaneBlockPos(targetPlane, i);
            if (!BlockUtils.canPlaceBlock(pistonBlockPos, false, Blocks.PISTON)) {
                info("Couldn't find valid place for piston at " + pistonBlockPos.toString() + ". Skipping...");
                continue;
            };
        }

        sequence = BlockPlacer.placeMany(mc, (index) -> {
            return getXZPlaneBlockPos(targetPlane, index);
        }, width, pistonStack, sequence, PlayerMoveC2SPacketUtils.Pitch.UP);

        ((PendingUpdateManagerMixin) pendingUpdateManager).setSequence(sequence);

        PlayerInventoryUtils.setSelectedSlotSync(previousSlot); 

        currentState = State.IDLE;
    }
}

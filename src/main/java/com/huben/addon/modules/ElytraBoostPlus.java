package com.huben.addon.modules;

import com.huben.addon.Addon;
import com.huben.addon.events.world.JoinWorldEvent;
import com.huben.mixin.FireworkRocketEntityAccessor;
import com.huben.util.MyFireworkRocketEntity;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.events.meteor.ActiveModulesChangedEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraBoost;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ElytraBoostPlus extends Module  {
    
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final ElytraBoost meteorElytraBoost = Modules.get().get(ElytraBoost.class);

    private final Setting<Boolean> dontConsumeFirework = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-consume")
        .description("Prevents fireworks from being consumed when using Elytra Boost.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyBoost = sgGeneral.add(new BoolSetting.Builder()
        .name("only-boost")
        .description("Prevents firework interaction with blocks and entities.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> boostInstead = sgGeneral.add(new BoolSetting.Builder()
        .name("Use Elytra Boost")
        .description("Whether to boost of interacting with blocks or entities")
        .defaultValue(true)
        .visible(onlyBoost::get)
        .build()
    );

    private final Setting<Integer> fireworkLevel = sgGeneral.add(new IntSetting.Builder()
        .name("firework-duration")
        .description("The duration of the firework.")
        .defaultValue(0)
        .range(0, 255)
        .sliderMax(255)
        .build()
    );

    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("play-sound")
        .description("Plays the firework sound when a boost is triggered.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SoundCategory> soundChannel = sgGeneral.add(new EnumSetting.Builder<SoundCategory>()
        .name("sound-channel")
        .description("The sound channel to play the firework sound on.")
        .defaultValue(SoundCategory.AMBIENT)
        .visible(playSound::get)
        .build()
    );
    

    public ElytraBoostPlus() {
        super(Addon.CATEGORY, "elytra-boost-plus", "Boosts your elytra as if you used a firework.");
    }

    @Override
    public void toggle() {
        if (isActive()) {
            currentRocket = null;
        } else {
            if (meteorElytraBoost != null && meteorElytraBoost.isActive()) {
                meteorElytraBoost.toggle();
            }
        }
        super.toggle();
    }

    @EventHandler
    private void onActiveModulesChanged(ActiveModulesChangedEvent event) {
        if (meteorElytraBoost != null && meteorElytraBoost.isActive()) {
            toggle();
        }
    }

    @EventHandler
    private void onJoinWorld(JoinWorldEvent event) {
        currentRocket = null;
    }

    @EventHandler
    private void onInteractEntity (InteractEntityEvent event) {
        onInteractBlockOrEntity(event.hand, event);
    }

    @EventHandler
    private void onInteractBlock (InteractBlockEvent event) {
        onInteractBlockOrEntity(event.hand, event);
    }

    private void onInteractBlockOrEntity(Hand hand, Cancellable event) {
        if (onlyBoost.get() && heldIsFirework(hand)) {
            event.cancel();
            if (!boostInstead.get()) return;
            if (dontConsumeFirework.get()) {
                boostClient();
            } else {
                mc.interactionManager.interactItem(mc.player, hand);
            }
        }
    }    

    @EventHandler
    private void onInteractItem(InteractItemEvent event) {
        if (dontConsumeFirework.get() && heldIsFirework(event.hand)) {
            event.toReturn = ActionResult.PASS;
            boostClient();
        }
    }

    private boolean heldIsFirework(Hand hand) {
        return mc.player.getStackInHand(hand).getItem() instanceof FireworkRocketItem;
    }

    MyFireworkRocketEntity currentRocket = null;

    private void boostClient() {
        if (!Utils.canUpdate()) return;
        if (mc.player.isGliding()) {
            if (currentRocket != null && currentRocket.isAlive()) {
                ((FireworkRocketEntityAccessor) currentRocket).setLife(0);
                return;
            }
            currentRocket = MyFireworkRocketEntity.create(fireworkLevel.get());
            if (playSound.get()) mc.world.playSoundFromEntity(mc.player, currentRocket, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, soundChannel.get(), 3.0F, 1.0F);
            mc.world.addEntity(currentRocket);
        }
    }
}

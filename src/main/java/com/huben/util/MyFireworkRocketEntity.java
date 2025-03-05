package com.huben.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import com.huben.mixin.FireworkRocketEntityAccessor;

public class MyFireworkRocketEntity extends FireworkRocketEntity {
    // private static final ItemStack itemStack = Items.FIREWORK_ROCKET.getDefaultStack();

    public static MyFireworkRocketEntity create (int fireworkLevel) {
        ItemStack itemStack = Items.FIREWORK_ROCKET.getDefaultStack();
        itemStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(fireworkLevel, itemStack.get(DataComponentTypes.FIREWORKS).explosions()));
        return new MyFireworkRocketEntity(mc.world, itemStack, mc.player);
    }

    MyFireworkRocketEntity(World world, ItemStack itemStack, LivingEntity shooter) {
        super(world, itemStack, shooter);
    }

    @Override
    public void tick() {
        super.tick();
        FireworkRocketEntityAccessor accessor = (FireworkRocketEntityAccessor)this;
        if (accessor.getLife() > accessor.getLifeTime()) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        this.discard();
    }

    @Override
    protected void onBlockHit(net.minecraft.util.hit.BlockHitResult blockHitResult) {
        this.discard();
    }
}

package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.projectile.FireworkRocketEntity;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Accessor("life")
    public int getLife();

    @Accessor("life")
    public void setLife(int life);

    @Accessor("lifeTime")
    public int getLifeTime();

    @Accessor("lifeTime")
    public void setLifeTime(int lifeTime);
}
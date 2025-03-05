package com.huben.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.World;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("rainGradient")
    public float _getRainGradient ();
    @Accessor("thunderGradient")
    public float _getThunderGradient ();
}

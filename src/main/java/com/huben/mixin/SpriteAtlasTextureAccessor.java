package com.huben.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@Mixin(SpriteAtlasTexture.class)
public interface SpriteAtlasTextureAccessor {
    @Accessor("sprites")
    public Map<Identifier, Sprite> getSprites ();
}


package com.huben.settings;

import java.util.function.Consumer;


import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
 
public class SoundEventSetting extends Setting<SoundEvent> {

    public SoundEventSetting(String name, String description, SoundEvent defaultValue, Consumer<SoundEvent> onChanged, Consumer<Setting<SoundEvent>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

    }

    @Override
    protected SoundEvent parseImpl(String str) {
        //System.out.println("Parsing sound event: " + str);
        for (SoundEvent possibleValue : Registries.SOUND_EVENT) {
            if (str.equals(possibleValue.id().toString())) {
                // System.out.println("Found sound event: " + str);
                return possibleValue;
            }
        }
        // System.out.println("Invalid sound event: " + str);

        return null;
    }

    @Override
    protected boolean isValueValid(SoundEvent value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", value.id().toString());
        // System.out.println("Saved sound event: " + value.id().toString());
        return tag;
    }

    @Override
    public SoundEvent load(NbtCompound tag) {
        parse(tag.getString("value"));
        // System.out.println("Loaded sound event: " + value.id().toString());
        return value;
    }

    @Override
    public String toString() {
        return value.id().toShortTranslationKey();
    }

    public static class Builder extends SettingBuilder<Builder, SoundEvent, SoundEventSetting> {
        public Builder() {
            super(null);
        }

        @Override
        public SoundEventSetting build() {
            return new SoundEventSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}

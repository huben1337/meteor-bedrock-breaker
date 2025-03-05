package com.huben.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

import org.apache.commons.lang3.StringUtils;

public class SoundEventSettingScreen extends WindowScreen {
    private final SoundEventSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public SoundEventSettingScreen(GuiTheme theme, SoundEventSetting setting) {
        super(theme, "Select sound");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };
        table = add(theme.table()).expandX().widget();
        initTable();
    }

    public void initTable() {
        for (SoundEvent sound : Registries.SOUND_EVENT) {
            if (!StringUtils.containsIgnoreCase(sound.id().toString(), filterText)) continue;
            table.add(theme.label(sound.id().toString()));

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                setting.set(sound);
                close();
            };

            table.row();
        }
    }
}
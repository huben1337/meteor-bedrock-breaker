package com.huben.util;

import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.Vec3i;

public class XZPlane {
    public XZPlane (int x1, int x2, int y, int z1, int z2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y = y;
        this.z1 = z1;
        this.z2 = z2;
    }
    public int x1, x2, y, z1, z2;
    public void render (Renderer3D renderer, SettingColor sideColor, SettingColor lineColor, ShapeMode shapeMode) {
        renderer.side(x1, y, z1, x2, y, z1, x2, y, z2, x1, y, z2, sideColor, lineColor, shapeMode);
    }

    public XZPlane translate (int x, int y, int z) {
        return new XZPlane(this.x1 + x, this.x2 + x, this.y + y, this.z1 + z, this.z2 + z);
    }
    public XZPlane translate (Vec3i vec) {
        return translate(vec.getX(), vec.getY(), vec.getZ());
    }
}

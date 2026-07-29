package com.pryzmm.splitself.mixin;

import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BackgroundRenderer.class)
public interface BackgroundRendererAccessor {
    @Accessor("red")
    static float getRed() { throw new AssertionError(); }
    @Accessor("green")
    static float getGreen() { throw new AssertionError(); }
    @Accessor("blue")
    static float getBlue() { throw new AssertionError(); }

    @Accessor("red")
    @Mutable
    static void setRed(float value) { throw new AssertionError(); }
    @Accessor("green")
    @Mutable
    static void setGreen(float value) { throw new AssertionError(); }
    @Accessor("blue")
    @Mutable
    static void setBlue(float value) { throw new AssertionError(); }
}
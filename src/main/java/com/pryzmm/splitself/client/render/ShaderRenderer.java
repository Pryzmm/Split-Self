package com.pryzmm.splitself.client.render;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

import java.util.HashMap;

public class ShaderRenderer {

    private static final ManagedShaderEffect MELT_SHADER = ShaderEffectManager.getInstance().manage(Identifier.of(SplitSelf.MOD_ID, "shaders/post/melt.json"));

    public enum Shaders {
        MELT
    }

    private static final HashMap<Shaders, Boolean> toggledShaders = new HashMap<>();
    static { for (Shaders shader : Shaders.values()) toggledShaders.put(shader, false); }

    public static void init() {
        PostHudRenderCallback.EVENT.register((counter) -> {
            if (isToggled(Shaders.MELT)) renderMelt(counter);
        });
    }

    public static void toggleShader(Shaders shader) {
        toggledShaders.put(shader, !toggledShaders.get(shader));
    }
    public static void toggleShader(Shaders shader, Boolean active) {
        toggledShaders.put(shader, active);
    }

    private static boolean isToggled(Shaders shader) {
        return toggledShaders.get(shader);
    }

    private static float meltAmount = 0;
    private static void renderMelt(RenderTickCounter counter) {
        meltAmount = Math.min(1f, meltAmount + 0.01f);
        if (meltAmount > 0f) {
            MELT_SHADER.findUniform1f("Time").set((float) (System.nanoTime() / 1.0e9));
            MELT_SHADER.findUniform1f("Amount").set(meltAmount);
            MELT_SHADER.render(counter.getTickDelta(true));
        }
    }

}

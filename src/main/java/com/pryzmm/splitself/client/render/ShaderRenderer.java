package com.pryzmm.splitself.client.render;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.util.Identifier;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class ShaderRenderer {

    private static final ManagedShaderEffect MELT_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of(SplitSelf.MOD_ID, "shaders/post/melt.json"));

    public static boolean active = false;
    private static float amount = 0f;

    public static void init() {
        PostHudRenderCallback.EVENT.register((counter) -> {
            amount = active ? Math.min(1f, amount + 0.01f) : 0;
            if (amount > 0f) {
                MELT_SHADER.findUniform1f("Time").set((float) (System.nanoTime() / 1.0e9));
                MELT_SHADER.findUniform1f("Amount").set(amount);
                MELT_SHADER.render(counter.getTickDelta(true));
            }
        });
    }

    public static void setActive(boolean value) {
        active = value;
    }

}

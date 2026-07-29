package com.pryzmm.splitself.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.http.PartyEffect;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PartyOverlay {

    public static List<Confetti> confettiList = new ArrayList<>();

    public static class Confetti {

        public int x, y, color, size;

        public Confetti(int X, int Y, int Color, int Size) {
            x = X;
            y = Y;
            color = Color;
            size = Size;
            confettiList.add(this);
        }

    }

    public static boolean confettiToggled = false;
    public static void toggleConfetti() {
        confettiToggled = !confettiToggled;
    }

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (overlayVisible) {
                renderOverlay(drawContext);
            }
        });
    }

    public static boolean overlayVisible = false;
    public static void toggleOverlay() {
        overlayVisible = !overlayVisible;
    }

    public static void renderOverlay(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();

        matrices.translate(0, 0, 1001);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableDepthTest();

        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.enablePolygonOffset();

        List<Confetti> removalList = new ArrayList<>();
        for (Confetti confetti : confettiList) {
            confetti.y = (int) (confetti.y + (confetti.size * 1.5));
            if (confetti.y >= screenHeight + confetti.size) removalList.add(confetti);
        }
        confettiList.removeAll(removalList);
        if (Math.random() * 5 <= 1 && confettiToggled) {
            confettiList.add(new Confetti(
                (int) (Math.random() * screenWidth), 0,  0xFF000000 | Color.decode(PartyEffect.generateHexColor()).getRGB(), (int) ((Math.random() * 3) + 1)
            ));
        }

        confettiList.forEach(c -> drawContext.fill(c.x, c.y, c.x + (c.size * 2), c.y + (c.size * 4), c.color));

        RenderSystem.disablePolygonOffset();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

}

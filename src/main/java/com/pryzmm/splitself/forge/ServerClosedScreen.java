package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Story screen shown when the transition video completes. */
final class ServerClosedScreen extends Screen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            "splitself", "textures/screen/dirt_background.png");

    private final java.util.UUID transitionId = ClientTransitionEffects.token();
    ServerClosedScreen() { super(Component.translatable("disconnect.closed")); }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.toTitle"),
                button -> { if (ClientTransitionEffects.active(transitionId)) Minecraft.getInstance().setScreen(new FakeMenuScreen()); })
                .bounds(width / 2 - 100, height / 2 + 15, 200, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(BACKGROUND, 0, 0, width, height, 0, 0, 1920, 1080, 1920, 1080);
        RenderSystem.disableBlend();
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, Component.translatable("disconnect.lost").withStyle(ChatFormatting.GRAY),
                width / 2, height / 2 - 15, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("multiplayer.disconnect.server_shutdown"),
                width / 2, height / 2, 0xFFFFFF);
    }

    @Override public void renderBackground(GuiGraphics graphics) {}
    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean isPauseScreen() { return true; }
}

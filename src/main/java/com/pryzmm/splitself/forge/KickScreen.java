package com.pryzmm.splitself.forge;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Fake timeout: returning to title actually closes this screen and requests the source world effect. */
final class KickScreen extends Screen {
    private static final ResourceLocation IMAGE = new ResourceLocation("splitself", "textures/screen/dirt_background.png");
    private final UUID token;
    private boolean answered;
    KickScreen(UUID token) { super(Component.translatable("disconnect.lost")); this.token = token; }
    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.toTitle"), button -> {
            if (answered) return;
            answered = true;
            StoryNetwork.answerKick(token);
            onClose();
        }).bounds(width / 2 - 100, height / 2 + 15, 200, 20).build());
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        try { g.blit(IMAGE, 0, 0, width, height, 0, 0, 1920, 1080, 1920, 1080); }
        finally { com.mojang.blaze3d.systems.RenderSystem.disableBlend(); }
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, Component.translatable("disconnect.lost").withStyle(ChatFormatting.GRAY), width/2, height/2-15, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("disconnect.timeout"), width/2, height/2, 0xFFFFFF);
    }
    @Override public void renderBackground(GuiGraphics g) {}
    @Override public boolean isPauseScreen() { return true; }
    @Override public boolean shouldCloseOnEsc() { return false; }
}

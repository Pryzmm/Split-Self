package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ServerClosedScreen extends Screen {

    public static final Identifier OVERLAY_IMAGE = Identifier.of(SplitSelf.MOD_ID, "textures/screen/dirt_background.png");

    public ServerClosedScreen() {
        super(Text.translatable("disconnect.closed"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.toTitle"),
            button -> {
                if (client != null) {
                    client.setScreen(new FakeMenuScreen());
                }
            }
        ).position((this.width / 2) - 100, (this.height / 2) + 15)
        .size(200, 20)
        .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderOverlayImage(context, this.width, this.height, OVERLAY_IMAGE);
        super.render(context, mouseX, mouseY, delta);
        renderOverlayContent(context, this.width, this.height);
    }

    public void renderOverlayContent(DrawContext drawContext, int screenWidth, int screenHeight) {
        Text connectionLostText = Text.translatable("disconnect.lost").formatted(Formatting.GRAY);
        int connectionLostWidth = this.textRenderer.getWidth(connectionLostText);
        drawContext.drawTextWithShadow(
            this.textRenderer,
            connectionLostText,
            (screenWidth - connectionLostWidth) / 2,
            (screenHeight / 2) - 15,
            0xFFFFFF
        );

        Text shutdownText = Text.translatable("multiplayer.disconnect.server_shutdown");
        int shutdownWidth = this.textRenderer.getWidth(shutdownText);
        drawContext.drawTextWithShadow(
            this.textRenderer,
            shutdownText,
            (screenWidth - shutdownWidth) / 2,
            screenHeight / 2,
            0xFFFFFF
        );
    }

    public void renderOverlayImage(DrawContext drawContext, int screenWidth, int screenHeight, Identifier image) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        drawContext.drawTexture(image, 0, 0, screenWidth, screenHeight, 0, 0, 1920, 1080, 1920, 1080);

        RenderSystem.disableBlend();
    }

    @Override
    public boolean shouldPause() { return true; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
}
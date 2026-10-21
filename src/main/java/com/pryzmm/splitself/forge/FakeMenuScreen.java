package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Original FakeMenuScreen: choices start the story, never open a real menu. */
final class FakeMenuScreen extends Screen {
    private static final ResourceLocation OVERLAY = new ResourceLocation(
            "textures/gui/title/background/panorama_overlay.png");
    private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
    private final LogoRenderer logo = new LogoRenderer(false);
    private SplashRenderer splash;
    private boolean melting;
    private final java.util.UUID transitionId = ClientTransitionEffects.token();

    FakeMenuScreen() { super(Component.translatable("narrator.screen.title")); }

    @Override protected void init() {
        if (splash == null) splash = minecraft.getSplashManager().getSplash();
        int y = height / 4 + 48;
        addChoice("menu.singleplayer", width / 2 - 100, y, 200);
        Component disabled = multiplayerDisabledReason();
        Button multiplayer = addChoice("menu.multiplayer", width / 2 - 100, y + 24, 200);
        Button online = addChoice("menu.online", width / 2 - 100, y + 48, 200);
        multiplayer.active = online.active = disabled == null;
        if (disabled != null) {
            multiplayer.setTooltip(Tooltip.create(disabled));
            online.setTooltip(Tooltip.create(disabled));
        }
        addRenderableWidget(new ImageButton(width / 2 - 124, y + 84, 20, 20,
                0, 106, 20, new ResourceLocation("textures/gui/widgets.png"), 256, 256,
                button -> startMelt(), Component.translatable("narrator.button.language")));
        addChoice("menu.options", width / 2 - 100, y + 84, 98);
        addChoice("menu.quit", width / 2 + 2, y + 84, 98);
        addRenderableWidget(new ImageButton(width / 2 + 104, y + 84, 20, 20,
                0, 0, 20, new ResourceLocation("textures/gui/accessibility.png"), 32, 64,
                button -> startMelt(), Component.translatable("narrator.button.accessibility")));
        Component credits = TitleScreen.COPYRIGHT_TEXT;
        int creditsWidth = font.width(credits);
        addRenderableWidget(new PlainTextButton(width - creditsWidth - 2, height - 10,
                creditsWidth, 10, credits, button -> startMelt(), font));
        // No Realms service requests from this story screen.
    }

    private Button addChoice(String key, int x, int y, int buttonWidth) {
        return addRenderableWidget(Button.builder(Component.translatable(key), button -> startMelt())
                .bounds(x, y, buttonWidth, 20).build());
    }

    private Component multiplayerDisabledReason() {
        if (minecraft.allowsMultiplayer()) return null;
        var ban = minecraft.multiplayerBan();
        if (ban == null) return Component.translatable("title.multiplayer.disabled");
        return Component.translatable(ban.expires() == null
                ? "title.multiplayer.disabled.banned.permanent"
                : "title.multiplayer.disabled.banned.temporary");
    }

    private void startMelt() {
        if (!ClientTransitionEffects.active(transitionId)) return;
        // Quirk kept: original isMelting stays set for this screen after C2S.
        if (!melting) melting = ClientMenuEffects.startMelt();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        panorama.render(partialTick, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(OVERLAY, 0, 0, width, height, 0, 0, 16, 128, 16, 128);
        RenderSystem.disableBlend();
        super.render(graphics, mouseX, mouseY, partialTick);
        logo.renderLogo(graphics, width, 1.0F);
        if (splash != null) splash.render(graphics, width, font, 0xFF000000);
        String version = "Minecraft " + SharedConstants.getCurrentVersion().getName();
        if (minecraft.isDemo()) version += " Demo";
        else if (!"release".equalsIgnoreCase(minecraft.getVersionType()))
            version += "/" + minecraft.getVersionType();
        if (Minecraft.checkModStatus().shouldReportAsModified()) version += I18n.get("menu.modded");
        graphics.drawString(font, version, 2, height - 10, 0xFFFFFF);
    }

    @Override public void renderBackground(GuiGraphics graphics) {}
    @Override public boolean isPauseScreen() { return true; }
    @Override public boolean shouldCloseOnEsc() { return false; }
}

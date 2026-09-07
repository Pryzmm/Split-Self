package com.pryzmm.splitself.screen;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.client.render.ShaderRenderer;
import com.pryzmm.splitself.packet.packets.TransitionPacket;
import com.pryzmm.splitself.sound.ModSounds;
import com.pryzmm.splitself.world.ClientTickScheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FakeMenuScreen extends Screen {
    private static final Text NARRATOR_SCREEN_TITLE = Text.translatable("narrator.screen.title");
    private static final Text COPYRIGHT = Text.translatable("title.credits");
    @Nullable
    private SplashTextRenderer splashText;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationGui;
    private float backgroundAlpha;
    private boolean doBackgroundFade;
    private long backgroundFadeStart;
    private final LogoDrawer logoDrawer;

    public FakeMenuScreen() {
        this(false);
    }

    public FakeMenuScreen(boolean doBackgroundFade) {
        this(doBackgroundFade, null);
    }

    public FakeMenuScreen(boolean doBackgroundFade, @Nullable LogoDrawer logoDrawer) {
        super(NARRATOR_SCREEN_TITLE);
        this.backgroundAlpha = 1.0F;
        this.doBackgroundFade = doBackgroundFade;
        this.logoDrawer = Objects.requireNonNullElseGet(logoDrawer, () -> new LogoDrawer(false));
    }

    private boolean isRealmsNotificationsGuiDisplayed() {
        return this.realmsNotificationGui != null;
    }

    public boolean shouldPause() {
        return true;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        assert this.client != null;
        if (this.splashText == null) {
            this.splashText = this.client.getSplashTextLoader().get();
        }

        int i = this.textRenderer.getWidth(COPYRIGHT);
        int j = this.width - i - 2;
        int l = this.height / 4 + 48;
        this.initWidgetsNormal(l);

        TextIconButtonWidget textIconButtonWidget = this.addDrawableChild(AccessibilityOnboardingButtons.createLanguageButton(20, (bu) -> startMeltEffect(), true));
        textIconButtonWidget.setPosition(this.width / 2 - 124, l + 72 + 12);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), (bu) -> startMeltEffect()).dimensions(this.width / 2 - 100, l + 72 + 12, 98, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), (bu) -> startMeltEffect()).dimensions(this.width / 2 + 2, l + 72 + 12, 98, 20).build());
        TextIconButtonWidget textIconButtonWidget2 = this.addDrawableChild(AccessibilityOnboardingButtons.createAccessibilityButton(20, (bu) -> startMeltEffect(), true));
        textIconButtonWidget2.setPosition(this.width / 2 + 104, l + 72 + 12);
        this.addDrawableChild(new PressableTextWidget(j, this.height - 10, i, 10, COPYRIGHT, (bu) -> startMeltEffect(), this.textRenderer));
        if (this.realmsNotificationGui == null) {
            this.realmsNotificationGui = new RealmsNotificationsScreen();
        }

    }

    private void initWidgetsNormal(int y) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), (bu) -> startMeltEffect()).dimensions(this.width / 2 - 100, y, 200, 20).build());
        Text text = this.getMultiplayerDisabledText();
        boolean bl = text == null;
        Tooltip tooltip = text != null ? Tooltip.of(text) : null;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.multiplayer"), (bu) -> startMeltEffect()).dimensions(this.width / 2 - 100, y + 24, 200, 20).tooltip(tooltip).build()).active = bl;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.online"), (bu) -> startMeltEffect()).dimensions(this.width / 2 - 100, y + 24 * 2, 200, 20).tooltip(tooltip).build()).active = bl;
    }

    @Nullable
    private Text getMultiplayerDisabledText() {
        assert this.client != null;
        if (this.client.isMultiplayerEnabled()) {
            return null;
        } else if (this.client.isUsernameBanned()) {
            return Text.translatable("title.multiplayer.disabled.banned.name");
        } else {
            BanDetails banDetails = this.client.getMultiplayerBanDetails();
            if (banDetails != null) {
                return banDetails.expires() != null ? Text.translatable("title.multiplayer.disabled.banned.temporary") : Text.translatable("title.multiplayer.disabled.banned.permanent");
            } else {
                return Text.translatable("title.multiplayer.disabled");
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert this.client != null;
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }

        float f = 1.0F;
        if (this.doBackgroundFade) {
            float g = (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0F;
            if (g > 1.0F) {
                this.doBackgroundFade = false;
                this.backgroundAlpha = 1.0F;
            } else {
                g = MathHelper.clamp(g, 0.0F, 1.0F);
                f = MathHelper.clampedMap(g, 0.5F, 1.0F, 0.0F, 1.0F);
                this.backgroundAlpha = MathHelper.clampedMap(g, 0.0F, 0.5F, 0.0F, 1.0F);
            }

            this.setWidgetAlpha(f);
        }

        this.renderPanoramaBackground(context, delta);
        int i = MathHelper.ceil(f * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            super.render(context, mouseX, mouseY, delta);
            this.logoDrawer.draw(context, this.width, f);
            if (this.splashText != null && !(Boolean)this.client.options.getHideSplashTexts().getValue()) {
                this.splashText.render(context, this.width, this.textRenderer, i);
            }

            String string = "Minecraft " + SharedConstants.getGameVersion().getName();
            if (this.client.isDemo()) string = string + " Demo";
            else string = string + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType());

            if (MinecraftClient.getModStatus().isModded()) string = string + I18n.translate("menu.modded");

            context.drawTextWithShadow(this.textRenderer, string, 2, this.height - 10, 16777215 | i);
            if (this.isRealmsNotificationsGuiDisplayed() && f >= 1.0F) {
                RenderSystem.enableDepthTest();
                this.realmsNotificationGui.render(context, mouseX, mouseY, delta);
            }

        }
    }

    private void setWidgetAlpha(float alpha) {
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget clickableWidget) clickableWidget.setAlpha(alpha);
        }
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    protected void renderPanoramaBackground(DrawContext context, float delta) {
        ROTATING_PANORAMA_RENDERER.render(context, this.width, this.height, this.backgroundAlpha, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        else return this.isRealmsNotificationsGuiDisplayed() && this.realmsNotificationGui.mouseClicked(mouseX, mouseY, button);
    }

    public void removed() {
        if (this.realmsNotificationGui != null) this.realmsNotificationGui.removed();
    }


    public void onDisplayed() {
        super.onDisplayed();
        if (this.realmsNotificationGui != null) this.realmsNotificationGui.onDisplayed();
    }

    private boolean isMelting = false;
    private void startMeltEffect() {
        if (isMelting) return;
        isMelting = true;
        ClientTickScheduler.schedule(60, () -> {
            ShaderRenderer.toggleShader(ShaderRenderer.Shaders.MELT, true);
            assert client != null;
            SoundInstance sound = PositionedSoundInstance.master(ModSounds.SCRATCH, 1.0F);
            client.getSoundManager().play(sound);
            ClientTickScheduler.schedule(25, () -> {
                assert client != null;
                client.getSoundManager().stop(sound);
                ShaderRenderer.toggleShader(ShaderRenderer.Shaders.MELT, false);
                ClientPlayNetworking.send(new TransitionPacket());
            });
        });
    }

}
package com.pryzmm.splitself.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.List;

// THE BIG ONE. This is where the Darks Souls Boss Soundtrack gets to start
// WAAAY more complicated than I thought it should be, took me some hours
// I kept variables in the beginning of it to make it configurable through the code

// This class draws a banner, icons and text. It also scales it depending on the GUI scale (my best approach to this issue)
// It has an animation and delay for it. Plays sounds when you click or when it shows up.
// I'm using a re-wrap technique to ensure that the text keeps itself on where it should be (that was pain)
// And finally, it sets every button fuction
public class Lang_Toaster_Overlay extends ClickableWidget {

    private Text customTitle;
    private Text customBody;

    // ======== PNG ART SIZE (background) ========
    // Set these to the texture actual size.
    private static final int TEX_W = 160;
    private static final int TEX_H = 32;

    // =================== GLOBAL SCALING ===================
    private static final float WIDTH_FRACTION   = 0.36f;    // portion of GUI width
    private static final int   MIN_DRAW_W_ABS   = 320;      // absolute floor
    private static final float MIN_DRAW_W_FRAC  = 0.22f;    // relative floor
    private static final int   MAX_DRAW_W       = TEX_W;    // just to avoid upscaling it, made this
    private static final int   MARGIN           = 2;        // top-right margin (the distance from the borders of screen)
    private static final float SAFE_RIGHT_STRIP_FRAC = 0.36f; // cap so we don't invade the center (can be a bit less)

    // =================== ANIMATION / SFX ===================
    // (Animation + appear SFX happen only when instant=false)
    private static final long  APPEAR_DELAY_MS  = 500L;
    private static final float ANIM_IN_SPEED    = 0.12f;
    private static final float ANIM_OUT_SPEED   = 0.18f;
    private static final boolean PLAY_APPEAR_SOUND = true;
    private static final boolean PLAY_CLICK_SOUND  = true;

    // =================== TEXTURES  ===================
    private static final Identifier BG_TEX       = Identifier.of("splitself", "textures/gui/lang_toast/background_lang_notif.png");
    private static final Identifier ICON_LANG    = Identifier.of("splitself", "textures/gui/lang_toast/icon_lang.png");
    private static final Identifier ICON_LANG_H  = Identifier.of("splitself", "textures/gui/lang_toast/icon_lang_h.png");
    private static final Identifier ICON_CLOSE   = Identifier.of("splitself", "textures/gui/lang_toast/icon_close.png");
    private static final Identifier ICON_CLOSE_H = Identifier.of("splitself", "textures/gui/lang_toast/icon_close_h.png");

    // =================== DESIGN-SPACING LAYOUT ===================
    // Side icons
    private static final int ICON_W = 22, ICON_H = 22;
    private static final int PAD_SIDE = 6;
    private static final int ICON_Y = (TEX_H - ICON_H) / 2;
    private static final int ICON_LANG_X  = PAD_SIDE;
    private static final int ICON_CLOSE_X = TEX_W - PAD_SIDE - ICON_W;

    // Text lane (between icons)
    private static final int TEXT_GAP_FROM_ICON = 4;  // menor = faixa de texto mais larga
    private static final int TEXT_TOP    = 6;
    private static final int TEXT_BOTTOM = TEX_H - 6;

    // Typography (to mess with text sizes)
    private static final float TITLE_BASE_SCALE = 0.85f;
    private static final float BODY_BASE_SCALE  = 0.80f;
    private static final float MIN_TEXT_SCALE   = 0.60f;
    private static final int   TITLE_BODY_GAP   = 1;
    private static final int   LINE_BASE_HEIGHT = 9;

    // =================== SESSION STATE (only X) ===================
    private static boolean SUPPRESS_UNTIL_RESTART  = false;
    public  static boolean isSuppressedUntilRestart() { return SUPPRESS_UNTIL_RESTART; }
    public  static void    suppressUntilRestart()     { SUPPRESS_UNTIL_RESTART = true; }

    // =================== DEPENDENCIES ===================
    private final Screen parent;
    private final Text niceName;
    private final Runnable onOpenLanguage;

    // =================== RUNTIME STATE ===================
    private final boolean instant;
    private boolean closing = false;
    private boolean playedAppearSound = false;

    // Delay starts on the first render frame (this is because of an issue I had, so I will leave it here if ever needed)
    private long firstRenderAtMs = -1L;

    private float scale;
    private int drawW, drawH, drawX;
    private float animY;

    // hitboxes (design space to hover mouse or click)
    private int hbLangX,  hbLangY,  hbLangW,  hbLangH;
    private int hbCloseX, hbCloseY, hbCloseW, hbCloseH;

    public Lang_Toaster_Overlay(Screen parent,
                                Text niceName,
                                Runnable onOpenLanguage,
                                boolean instant) {
        super(0, 0, 0, 0, Text.empty());
        this.parent = parent;
        this.niceName = niceName;
        this.onOpenLanguage = onOpenLanguage;
        this.instant = instant;

        recomputeScaleAndAnchor();

        if (instant) {
            animY = MARGIN;
            playedAppearSound = true;
        } else {
            animY = -drawH - 8;
            firstRenderAtMs = -1L;
        }
    }

    // Another constructor, this one is for .json
    public Lang_Toaster_Overlay(Screen parent,
                                Text customTitle,
                                Text customBody,
                                Runnable onOpenLanguage,
                                boolean instant) {
        this(parent, Text.empty(), onOpenLanguage, instant);
        this.customTitle = customTitle;
        this.customBody  = customBody;
    }

    // ---------- tiny helpers that do tiny little things with math ----------

    private static Text trOr(String key, String fallbackEn) {
        Language L = Language.getInstance();
        return L.hasTranslation(key) ? Text.translatable(key) : Text.literal(fallbackEn);
    }


    private void recomputeScaleAndAnchor() {
        int minDyn   = Math.min(MIN_DRAW_W_ABS, Math.round(parent.width * MIN_DRAW_W_FRAC));
        int targetW  = Math.round(parent.width * WIDTH_FRACTION);

        // base + per-screen cap so we don't overlap the center/logo area
        int maxByScreen = Math.max(80, Math.round(parent.width * SAFE_RIGHT_STRIP_FRAC));
        drawW = Math.max(minDyn, Math.min(MAX_DRAW_W, Math.min(targetW, maxByScreen)));

        drawW = Math.min(drawW, TEX_W);

        scale = drawW / (float) TEX_W;
        drawH = Math.round(TEX_H * scale);
        drawX = parent.width - drawW - MARGIN;

        setX(drawX); setWidth(drawW); setHeight(drawH);

        // hitboxes
        hbLangX = ICON_LANG_X;  hbLangY = ICON_Y;  hbLangW = ICON_W; hbLangH = ICON_H;
        hbCloseX = ICON_CLOSE_X; hbCloseY = ICON_Y; hbCloseW = ICON_W; hbCloseH = ICON_H;
    }


    // The nightmare that took something about 63 Minecraft runs to finally get somewhere
    // It's not that much commented cause it was in portuguese and would be pain to
    @Override
    protected void renderWidget(DrawContext dc, int mouseX, int mouseY, float delta) {
        recomputeScaleAndAnchor();

        if (!instant) {
            long now = System.currentTimeMillis();
            if (firstRenderAtMs < 0L) {
                firstRenderAtMs = now;
                return;
            }
            if (now - firstRenderAtMs < APPEAR_DELAY_MS) {
                return;
            }
            if (!playedAppearSound && PLAY_APPEAR_SOUND) {
                MinecraftClient.getInstance().getSoundManager()
                        .play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_IN, 1.0F));
                playedAppearSound = true;
            }
        }

        // ========== slide animation ==========
        final int targetY = MARGIN;
        if (!closing) {
            animY = instant ? targetY : animY + (targetY - animY) * ANIM_IN_SPEED;
        } else {
            animY += ((-drawH - 12) - animY) * ANIM_OUT_SPEED;
            if (animY < -drawH - 11) { this.visible = false; return; }
        }

        dc.getMatrices().push();
        try {
            dc.getMatrices().translate(drawX, animY, 0);
            dc.getMatrices().scale(scale, scale, 1f);

            try {
                dc.drawTexture(BG_TEX, 0, 0, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);
            } catch (Exception e) {
                dc.fill(0, 0, TEX_W, TEX_H, 0xCC1E1E1E);
                dc.drawBorder(0, 0, TEX_W, TEX_H, 0xFF585858);
            }

            int lx = (int)((mouseX - drawX) / scale);
            int ly = (int)((mouseY - animY) / scale);
            boolean hLang  = inside(hbLangX,  hbLangY,  hbLangW,  hbLangH,  lx, ly);
            boolean hClose = inside(hbCloseX, hbCloseY, hbCloseW, hbCloseH, lx, ly);

            drawIcon(dc, hLang ? ICON_LANG_H : ICON_LANG,   hbLangX,  hbLangY);
            drawIcon(dc, hClose ? ICON_CLOSE_H: ICON_CLOSE,  hbCloseX, hbCloseY);

            final int textLeft  = ICON_LANG_X  + ICON_W + TEXT_GAP_FROM_ICON;
            final int textRight = ICON_CLOSE_X - TEXT_GAP_FROM_ICON;
            final int textW     = Math.max(0, textRight - textLeft);
            final int textCX    = textLeft + textW / 2;
            final int textTop   = TEXT_TOP;
            final int textBot   = TEXT_BOTTOM;

            var tr = MinecraftClient.getInstance().textRenderer;

            final Text title = (customTitle != null)
                    ? customTitle
                    : trOr("splitself.nudge.title", "Translation available");

            int rawTitleW = tr.getWidth(title);
            float titleScale = Math.min(TITLE_BASE_SCALE,
                    Math.max(MIN_TEXT_SCALE, (float) textW / Math.max(1, rawTitleW)));
            int titlePixH = Math.round(LINE_BASE_HEIGHT * titleScale);

            dc.getMatrices().push();
            dc.getMatrices().translate(textCX, textTop + titlePixH / 2f, 0);
            dc.getMatrices().scale(titleScale, titleScale, 1f);
            dc.drawText(tr, title, -rawTitleW / 2, -5, 0xFFFFFF, false);
            dc.getMatrices().pop();

            final Text body = (customBody != null)
                    ? customBody
                    : Text.translatable("splitself.nudge.body", niceName);

            float bodyScale = BODY_BASE_SCALE;

            int availH = Math.max(0, (textBot - textTop) - (titlePixH + TITLE_BODY_GAP));
            int lineH  = LINE_BASE_HEIGHT;

            List<OrderedText> lines = java.util.List.of();
            for (int iter = 0; iter < 4; iter++) {
                int wrapW = Math.max(1, Math.round(textW / bodyScale));
                lines = tr.wrapLines(body, wrapW);

                int totalH = (int) Math.ceil(lines.size() * lineH * bodyScale);
                if (totalH <= availH || bodyScale <= MIN_TEXT_SCALE + 1e-3f) {
                    break;
                }
                float needed = (float) availH / Math.max(1, lines.size() * lineH);
                bodyScale = Math.max(MIN_TEXT_SCALE, Math.min(bodyScale * 0.92f, needed));
            }

            int bodyY = textTop + titlePixH + TITLE_BODY_GAP;
            dc.getMatrices().push();
            dc.getMatrices().translate(textCX, bodyY, 0);
            dc.getMatrices().scale(bodyScale, bodyScale, 1f);
            for (int i = 0; i < lines.size(); i++) {
                OrderedText line = lines.get(i);
                int w = tr.getWidth(line);
                dc.drawText(tr, line, -w / 2, i * lineH, 0xD0D0D0, false);
            }
            dc.getMatrices().pop();

        } finally {
            dc.getMatrices().pop();
        }
    }

    private void drawIcon(DrawContext c, Identifier tex, int x, int y) {
        try {
            c.drawTexture(tex, x, y, 0, 0, ICON_W, ICON_H, ICON_W, ICON_H);
        } catch (Exception e) {
            c.fill(x, y, x + ICON_W, y + ICON_H, 0xFF444444);
            c.drawBorder(x, y, ICON_W, ICON_H, 0xFF888888);
        }
    }

    // So... this was a suggestion a friend of mine gave me. I think there's probably a better way to make a button
    // work, but I'll stick to this as when I checked how much consuming it was (on hardware) was close to none.
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible) return false;

        // Se for instantâneo, não há delay
        if (!instant && firstRenderAtMs >= 0L) {
            long now = System.currentTimeMillis();
            if (now - firstRenderAtMs < APPEAR_DELAY_MS) return false;
        }

        int lx = (int)((mx - drawX) / scale);
        int ly = (int)((my - animY) / scale);

        var sm = MinecraftClient.getInstance().getSoundManager();
        if (inside(hbLangX, hbLangY, hbLangW, hbLangH, lx, ly)) {
            if (PLAY_CLICK_SOUND) sm.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onOpenLanguage.run();
            closing = true; return true;
        }
        if (inside(hbCloseX, hbCloseY, hbCloseW, hbCloseH, lx, ly)) {
            if (PLAY_CLICK_SOUND) sm.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            suppressUntilRestart();
            closing = true; return true;
        }
        return false;
    }

    // THE CRAZY THING, my calculus teacher told I would use something similar one day. AND HERE IT IS!
    private boolean inside(int rx, int ry, int rw, int rh, int mx, int my) {
        return mx >= rx && mx < rx + rw && my >= ry && my < ry + rh;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }
}

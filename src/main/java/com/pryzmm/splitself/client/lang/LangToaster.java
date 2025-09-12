package com.pryzmm.splitself.client.lang;

import com.pryzmm.splitself.screen.Lang_Toaster_Overlay;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.text.Text;

import java.util.Locale;

// The idea of this class is to verify if it should or not show the Toast and, if so... it builds the body text and title.
// Both texts can be changed at lang_overlay_mgs.json per language
// I hardcoded them in the json file cause I want the Toast to inform the player about his language using their own language
public final class LangToaster {

    private LangToaster() {}

    public static void addToast(MinecraftClient client, TitleScreen screen) {

        boolean already = Screens.getButtons(screen).stream().anyMatch(b -> b instanceof Lang_Toaster_Overlay);
        if (already) return;

        var rm = client.getResourceManager();
        String currentGameLang = client.getLanguageManager().getLanguage();

        // 3-layer decision (OS lang -> Region if EN -> IP). Empty => no toast.
        var prefer = LanguageAdvisor.preferLanguageForPlayer(rm, currentGameLang);
        //var prefer = java.util.Optional.of("en_us");
        if (prefer.isEmpty()) return;

        String code = prefer.get();

        // session-suppressed by X
        if (Lang_Toaster_Overlay.isSuppressedUntilRestart()) return;

        // load JSON only once
        if (!LangToastMessages.isLoaded()) LangToastMessages.reload(rm);

        String pretty = LanguageAdvisor.prettyName(code).getString();
        Locale os = Locale.getDefault();

        var lines = LangToastMessages.resolve(os, code);

        Text title = lines.titleText();
        Text body  = lines.bodyText(pretty);

        // Currently, the Toast will have animation every time player gets to the main menu (the game restarts it)
        // That is, unless they click on the X, that will remove it untill client restart or languages are same.
        boolean instant = false;

        Runnable openLang = () -> client.execute(() -> client.setScreen(
                new LanguageOptionsScreen(screen, client.options, client.getLanguageManager())));

        Screens.getButtons(screen).add(new Lang_Toaster_Overlay(
                screen, title, body, openLang, instant
        ));
    }
}

package com.pryzmm.splitself.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry.class)
public class LanguageEntryMixin {

    @Shadow
    @Final
    String languageCode;
    @Unique
    private static final Set<String> SUPPORTED_LANGUAGE_CODES = Set.of("cs_cz", "en_us", "es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "fr_ca", "hu_hu", "it_it", "nl_be", "nl_nl", "pl_pl", "pt_br", "ro_ro", "ru_ru", "sv_se", "tr_tr", "zh_cn");

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
    private void addStarToSupportedLanguages(DrawContext instance, TextRenderer textRenderer, Text text, int centerX, int y, int color, Operation<Void> original) {
        if (SUPPORTED_LANGUAGE_CODES.contains(this.languageCode)) {
            instance.drawCenteredTextWithShadow(textRenderer, Text.literal("").append(Text.literal("★ ").formatted(Formatting.GOLD)).append(text), centerX, y, color);
        } else {
            original.call(instance, textRenderer, text, centerX, y, color);
        }
    }
}
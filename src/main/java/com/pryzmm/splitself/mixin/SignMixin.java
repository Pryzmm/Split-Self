package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public class SignMixin {

    @Shadow private SignText frontText;
    @Shadow private SignText backText;

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (frontText != null) {
            frontText = translateSignText(frontText);
        }
        if (backText != null) {
            backText = translateSignText(backText);
        }
    }

    @Inject(method = "getText", at = @At("RETURN"), cancellable = true)
    private void translateSignTextOnGet(boolean front, CallbackInfoReturnable<SignText> cir) {
        SignText originalText = cir.getReturnValue();
        if (originalText != null && needsTranslation(originalText)) {
            SignText translatedText = translateSignText(originalText);
            cir.setReturnValue(translatedText);
        }
    }

    @Unique
    private SignText translateSignText(SignText signText) {
        Text[] translatedMessages = new Text[4];
        boolean hasChanges = false;

        for (int i = 0; i < 4; i++) {
            Text originalLine = signText.getMessage(i, false);
            Text translatedLine = translateText(originalLine);
            translatedMessages[i] = translatedLine;

            if (translatedLine != originalLine) {
                hasChanges = true;
            }
        }

        if (hasChanges) {
            return new SignText(
                    translatedMessages,
                    translatedMessages,
                    signText.getColor(),
                    signText.isGlowing()
            );
        }

        return signText;
    }

    @Unique
    private boolean needsTranslation(SignText signText) {
        for (int i = 0; i < 4; i++) {
            String content = signText.getMessage(i, false).getString();
            if (content.startsWith("SS|")) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private Text translateText(Text text) {
        String content = text.getString();
        if (content.startsWith("SS|")) {
            content = "sign.splitself." + content.substring(3);
            return SplitSelf.translate(content);
        }
        return text;
    }
}
package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
class TitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!SplitSelf.ShriekInstalled) {
            System.out.println("Shriek not installed, adding Shriek button");
            addShriekButton();
        } else {
            System.out.println("Shriek installed, skipping Shriek button");
        }
    }

    @Unique
    private void addShriekButton() {
        TitleScreen screen = (TitleScreen) (Object) this;
        try {
            TexturedButtonWidget ShriekButton = new TexturedButtonWidget(
                    10, 10, 30, 30,
                    new ButtonTextures(
                            Identifier.of(SplitSelf.MOD_ID, "widget/shriek"),
                            Identifier.of(SplitSelf.MOD_ID, "widget/shriek_focused")
                    ),
                    button -> {
                        Util.getOperatingSystem().open("https://modrinth.com/mod/shriek");
                    }
            );

            ((ScreenAccessor) screen).invokeAddDrawableChild(ShriekButton);

        } catch (Exception e) {
            System.out.println("Failed to add Shriek button: " + e.getMessage());
        }
    }
}
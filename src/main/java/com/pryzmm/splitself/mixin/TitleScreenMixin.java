package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.data.ClientData;
import com.pryzmm.splitself.file.ZipFunc;
import com.pryzmm.splitself.screen.LoadingResourcesScreen;
import com.pryzmm.splitself.screen.PreMainScreen;
import net.minecraft.client.MinecraftClient;
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

        if (ZipFunc.needsVideoDownloads()) MinecraftClient.getInstance().setScreen(new LoadingResourcesScreen());
        else if (!PreMainScreen.viewedScreen) MinecraftClient.getInstance().setScreen(new PreMainScreen());

        SplitSelfClient.panorama = ClientData.getPanoramaStage();

        if (!SplitSelf.ShriekInstalled) addShriekButton();

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
                button -> Util.getOperatingSystem().open("https://modrinth.com/mod/shriek")
            );

            ((ScreenAccessor) screen).invokeAddDrawableChild(ShriekButton);

        } catch (Exception e) {
            SplitSelf.LOGGER.info("Failed to add Shriek button: {}", e.getMessage());
        }
    }
}
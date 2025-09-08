package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.SplitSelfClient;
import com.pryzmm.splitself.config.CustomConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onButtonCreate(CallbackInfo ci) {
        ButtonWidget creditsButton = SplitSelfClient.findButtonByText(this, "options.credits_and_attribution");
        if (creditsButton != null) {
            ButtonWidget customButton = ButtonWidget.builder(
                            (SplitSelf.translate("config.splitself.title")),
                            button -> {
                                assert client != null;
                                client.setScreen(new CustomConfigScreen(this));
                            }
                    )
                    .dimensions(5, 5, 150, 20)
                    .build();
            this.addDrawableChild(customButton);

        }
    }
}
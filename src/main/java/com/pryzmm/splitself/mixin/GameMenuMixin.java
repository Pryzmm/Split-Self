package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.events.EventManager;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mixin(GameMenuScreen.class)
class GameMenuMixin extends Screen {

    @Unique
    private static final Random RANDOM = new Random();

    @Unique
    Map<ButtonWidget, Integer> buttonX = new HashMap<>();
    @Unique
    Map<ButtonWidget, Integer> buttonY = new HashMap<>();

    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onPauseMenuInit(CallbackInfo ci) {
        if (EventManager.PAUSE_SHAKE) {
            moveAllButtons();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        if (EventManager.PAUSE_SHAKE) {
            moveAllButtons();
        }
    }

    @Unique
    private void moveAllButtons() {
        for (Element child : this.children()) {
            if (child instanceof ButtonWidget button) {
                if (!buttonX.containsKey(button)) {
                    buttonX.put(button, button.getX());
                    buttonY.put(button, button.getY());
                }
                button.setX(buttonX.get(button) + ((RANDOM.nextInt(0, 2) * 2) - 1));
                button.setY(buttonY.get(button) + ((RANDOM.nextInt(0, 2) * 2) - 1));
            }
        }
    }
}
package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.events.EventManager;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(GameMenuScreen.class)
class GameMenuMixin {

    @Unique
    Map<ButtonWidget, Integer> buttonX = new HashMap<>();
    @Unique
    Map<ButtonWidget, Integer> buttonY = new HashMap<>();

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
        GameMenuScreen screen = (GameMenuScreen) (Object) this;
        try {
            Field childrenField = Screen.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Element> children = (List<Element>) childrenField.get(screen);

            for (Element child : children) {
                Random random = new Random();
                if (child instanceof ButtonWidget button) {
                    if (!buttonX.containsKey(button)) {
                        buttonX.put(button, button.getX());
                        buttonY.put(button, button.getY());
                    }
                    button.setX(buttonX.get(button) + ((random.nextInt(0, 2) * 2) - 1));
                    button.setY(buttonY.get(button) + ((random.nextInt(0, 2) * 2) - 1));
                }
            }
        } catch (Exception ignored) {}
    }
}
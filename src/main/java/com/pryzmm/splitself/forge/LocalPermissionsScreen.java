package com.pryzmm.splitself.forge;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class LocalPermissionsScreen extends Screen {
    private final Screen parent;

    LocalPermissionsScreen(Screen parent) {
        super(Component.translatable("config.splitself.local_permissions"));
        this.parent = parent;
    }

    @Override protected void init() {
        toggle("externalEffects", 0, ExternalConsent.ENABLED::get, ExternalClientEvents::setEnabled);
        toggle("computerInteractions", 1, ExternalConsent.COMPUTER_INTERACTIONS::get, ExternalClientEvents::setComputerEnabled);
        toggle("personalInformation", 2, ExternalConsent.PERSONAL_INFORMATION::get, ExternalClientEvents::setPersonalEnabled);
        toggle("microphone", 3, ExternalConsent.MICROPHONE::get, ExternalClientEvents::setMicrophoneEnabled);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    private void toggle(String key, int row, BooleanSupplier value, Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(Component.translatable("config.splitself.permission." + key)
                .append(": ").append(Component.translatable(value.getAsBoolean() ? "options.on" : "options.off")), button -> {
                    setter.accept(!value.getAsBoolean());
                    rebuildWidgets();
                }).bounds(width / 2 - 150, 45 + row * 24, 300, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int x, int y, float delta) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xffffff);
        int textY = 146;
        for (var line : font.split(Component.translatable("config.splitself.permissions_help"), Math.max(80, width - 24))) {
            graphics.drawCenteredString(font, line, width / 2, textY, 0xaaaaaa);
            textY += font.lineHeight + 1;
        }
        super.render(graphics, x, y, delta);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}

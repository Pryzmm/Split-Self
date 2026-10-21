package com.pryzmm.splitself.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class PoemScreen extends Screen {
    PoemScreen() { super(Component.empty()); }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("..."), button -> onClose())
                .bounds(width / 2 - 50, height - 50, 100, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        int y = 10;
        boolean pii = ExternalConsent.personalAllowed();
        String name = SimpleEventRules.poemName(minecraft.player == null ? "" : minecraft.player.getName().getString(),
                pii, pii ? System.getProperty("user.name", "") : "",
                Component.translatable("events.splitself.redacted_name").getString());
        for (int line : SimpleEventRules.POEM_LINES) {
            Component text = line == 0 ? Component.empty() : line == 9
                    ? Component.translatable("events.splitself.poemScreen.line9", name)
                    : Component.translatable("events.splitself.poemScreen.line" + line);
            graphics.drawCenteredString(font, text, width / 2, y, 0xFFFFFF);
            y += 12;
        }
    }

    @Override public boolean isPauseScreen() { return true; }
}

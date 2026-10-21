package com.pryzmm.splitself.forge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Original warning layout and PII-only control for both editions, adapted to Forge 1.20.1. */
final class ClientWarningScreen extends Screen {
    private int textScroll, maxScroll;

    private ClientWarningScreen() {
        super(Component.translatable("warning.splitself.title"));
    }

    static void open() { net.minecraft.client.Minecraft.getInstance().setScreen(new ClientWarningScreen()); }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("warning.splitself.continue"), button -> {
            ExternalClientEvents.writeStoryFile("begin");
            ClientPanorama.resetMain();
            onClose();
        }).bounds(width / 2 - 105, height - 50, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("warning.splitself.PII.toggle"), button ->
                ExternalClientEvents.setPersonalEnabled(!ExternalConsent.PERSONAL_INFORMATION.get()))
                .bounds(width / 2 + 5, height - 50, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        boolean pii = ExternalConsent.PERSONAL_INFORMATION.get();
        Component status = Component.translatable(pii ? "warning.splitself.PII.enabled" : "warning.splitself.PII.disabled")
                .withStyle(pii ? ChatFormatting.GREEN : ChatFormatting.RED);
        List<Component> paragraphs = List.of(
                Component.translatable("warning.splitself.line1"),
                Component.translatable(EditionPolicy.defaultsEnabled()?"warning.splitself.line2":"warning.splitself.line2.safe"),
                Component.translatable(EditionPolicy.defaultsEnabled()?"warning.splitself.line3":"warning.splitself.line3.safe"),
                Component.translatable(EditionPolicy.defaultsEnabled()?"warning.splitself.line4":"warning.splitself.line4.safe").withStyle(ChatFormatting.YELLOW),
                Component.empty(),
                Component.translatable("warning.splitself.line5").withStyle(ChatFormatting.RED),
                Component.translatable("warning.splitself.line6").withStyle(ChatFormatting.GRAY),
                Component.empty(),
                Component.translatable("warning.splitself.line7").append(status));
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component paragraph : paragraphs) {
            var wrapped = font.split(paragraph, Math.max(40, width - 32));
            if (wrapped.isEmpty()) lines.add(FormattedCharSequence.EMPTY);
            else lines.addAll(wrapped);
        }
        int top = Math.min(70, Math.max(36, height - 170));
        int bottom = Math.max(top + 12, height - 62);
        maxScroll = Math.max(0, lines.size() * 12 - (bottom - top));
        textScroll = Math.min(textScroll, maxScroll);
        graphics.enableScissor(8, top, width - 8, bottom);
        int y = top - textScroll;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(font, line, width / 2, y, 0xFFFFFF);
            y += 12;
        }
        graphics.disableScissor();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0 && mouseY < height - 62) {
            textScroll = Math.max(0, Math.min(maxScroll, textScroll - (int) (delta * 12)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override public boolean isPauseScreen() { return true; }
    @Override public boolean shouldCloseOnEsc() { return false; }
}

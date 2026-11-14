package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/** The five positions and images from the pinned MemoryScreen source. */
final class MemoryBookScreen extends Screen {
    private static final float SCALE = 1.3f;
    private static final int BOOK_WIDTH = 286;
    private static final int BOOK_HEIGHT = 180;
    private static final int SLOT_WIDTH = 76;
    private static final int SLOT_HEIGHT = 40;
    private static final List<Memory> MEMORIES = List.of(
            new Memory("house", 24, 23, 1),
            new Memory("blu", 53, 72, 2),
            new Memory("mines", 27, 121, 4),
            new Memory("pillar", 184, 32, 8),
            new Memory("creeper", 161, 102, 16));
    private final int bitMask;
    private String selected;

    MemoryBookScreen(int bitMask) {
        super(Component.empty());
        this.bitMask = bitMask;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(SplitSelfForge.MOD_ID, "textures/gui/" + name + ".png");
    }

    private int left() { return (width - Math.round(BOOK_WIDTH * SCALE)) / 2; }
    private int top() { return (height - Math.round(BOOK_HEIGHT * SCALE)) / 2; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (selected != null) {
            renderImage(graphics, selected);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        int x = left(), y = top();
        int w = Math.round(BOOK_WIDTH * SCALE), h = Math.round(BOOK_HEIGHT * SCALE);
        graphics.blit(texture("memory_book"), x, y, 0, 0, w, h, w, h);
        for (Memory memory : MEMORIES) {
            int sx = x + Math.round(memory.x * SCALE);
            int sy = y + Math.round(memory.y * SCALE);
            int sw = Math.round(SLOT_WIDTH * SCALE), sh = Math.round(SLOT_HEIGHT * SCALE);
            String image = (bitMask & memory.bit) != 0 ? memory.id : "missing_memory";
            graphics.blit(texture("memories/" + image), sx, sy, 0, 0, sw, sh, sw, sh);
        }
        graphics.blit(texture("memory_book_overlay"), x, y, 0, 0, w, h, w, h);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderImage(GuiGraphics graphics, String image) {
        int x = (width - 320) / 2;
        int y = (height - 168) / 2 - 30;
        graphics.blit(texture("memories/" + image), x, y, 0, 0, 320, 168, 320, 168);
        graphics.renderOutline(x, y, 320, 168, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("memory.splitself." + image),
                width / 2, y + 172, 0xFFFFFFFF);
        List<FormattedCharSequence> lines = font.split(
                Component.translatable("memory.splitself." + image + ".desc"), Math.min(320, width - 20));
        int textY = y + 182;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(font, line, width / 2, textY, 0xFF777777);
            textY += font.lineHeight + 1;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selected == null) {
            for (Memory memory : MEMORIES) {
                int x = left() + Math.round(memory.x * SCALE);
                int y = top() + Math.round(memory.y * SCALE);
                if ((bitMask & memory.bit) != 0 && mouseX >= x && mouseX < x + Math.round(SLOT_WIDTH * SCALE)
                        && mouseY >= y && mouseY < y + Math.round(SLOT_HEIGHT * SCALE)) {
                    selected = memory.id;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && selected != null) {
            selected = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return true; }

    private record Memory(String id, int x, int y, int bit) {}
}

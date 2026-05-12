package com.pryzmm.splitself.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.data.WorldData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

public class MemoryScreen extends Screen {

    private static final float sizeMulti = 1.3f;
    private static long frameOffset = 0L;

    private static final List<Memory> memories = new ArrayList<>();
    static {
        memories.add(new Memory("house", 24, 23));
        memories.add(new Memory("blu", 53, 72));
        memories.add(new Memory("mines", 27, 121));
        memories.add(new Memory(null, 184, 32));
        memories.add(new Memory(null, 161, 102));
    }

    public MemoryScreen() {
        super(Text.empty());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        frameOffset++;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.drawTexture(Identifier.of(SplitSelf.MOD_ID, "textures/gui/memory_book.png"),
            (int) (((float) context.getScaledWindowWidth() / 2) - ((286 * sizeMulti) / 2)), (int) (((float) context.getScaledWindowHeight() / 2) - ((180 * sizeMulti) / 2)),
            0, 0,
            (int) (286 * sizeMulti), (int) (180 * sizeMulti),
            (int) (286 * sizeMulti), (int) (180 * sizeMulti));

        for (Memory memory : memories) drawMemory(context, memory);

        context.drawTexture(Identifier.of(SplitSelf.MOD_ID, "textures/gui/memory_book_overlay.png"),
            (int) (((float) context.getScaledWindowWidth() / 2) - ((286 * sizeMulti) / 2)), (int) (((float) context.getScaledWindowHeight() / 2) - ((180 * sizeMulti) / 2)),
            0, 0,
            (int) (286 * sizeMulti), (int) (180 * sizeMulti),
            (int) (286 * sizeMulti), (int) (180 * sizeMulti));

        RenderSystem.disableBlend();
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    private void drawMemory(DrawContext context, Memory memory) {
        int xOffset = (int) (((float) context.getScaledWindowWidth() / 2) - ((286 * sizeMulti) / 2));
        int yOffset = (int) (((float) context.getScaledWindowHeight() / 2) - ((180 * sizeMulti) / 2));
        int drawX = xOffset + (int) (memory.x * sizeMulti);
        int drawY = yOffset + (int) (memory.y * sizeMulti);
        int endX  = xOffset + (int) ((memory.x + 76) * sizeMulti);
        int endY  = yOffset + (int) ((memory.y + 40) * sizeMulti);
        if (memory.image == null || !WorldData.getUnlockedMemories().contains(memory.image)) {
            context.drawTexture(Identifier.of(SplitSelf.MOD_ID, "textures/gui/memories/missing_memory.png"), drawX, drawY, 0, 0, endX - drawX, endY - drawY, (int) (76 * sizeMulti), (int) (40 * sizeMulti));
            int hashOffset = memory.image != null ? memory.image.hashCode() % 256 : 0;
            int timeOffset = Math.toIntExact((long) (frameOffset + (Math.random() * 20))) % 256;
            context.drawTexture(Identifier.of(SplitSelf.MOD_ID, "textures/gui/memories/missing_memory_glitch.png"), drawX, drawY, hashOffset + timeOffset, hashOffset + timeOffset, endX - drawX, endY - drawY, (int) (76 * sizeMulti), (int) (40 * sizeMulti));
        }
        else context.drawTexture(Identifier.of(SplitSelf.MOD_ID, "textures/gui/memories/" + memory.image + ".png"), drawX, drawY, 0, 0, endX - drawX, endY - drawY, (int) (76 * sizeMulti), (int) (40 * sizeMulti));
    }

    public record Memory(String image, int x, int y) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Memory memory : memories) {
                if (isClickingMemory(mouseX, mouseY, memory) && memory.image != null && WorldData.getUnlockedMemories().contains(memory.image)) {
                    MinecraftClient.getInstance().setScreen(new MemoryImageScreen(memory, this));
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isClickingMemory(double mouseX, double mouseY, Memory memory) {
        int xOffset = (int) (((float) this.width / 2) - ((286 * sizeMulti) / 2));
        int yOffset = (int) (((float) this.height / 2) - ((180 * sizeMulti) / 2));
        int drawX = xOffset + (int) (memory.x * sizeMulti);
        int drawY = yOffset + (int) (memory.y * sizeMulti);
        int endX  = xOffset + (int) ((memory.x + 76) * sizeMulti);
        int endY  = yOffset + (int) ((memory.y + 40) * sizeMulti);
        return mouseX >= drawX && mouseX < endX && mouseY >= drawY && mouseY < endY;
    }

}
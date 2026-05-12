package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;

public class MemoryImageScreen extends Screen {

    private static MemoryScreen.Memory memory = null;
    private final Screen parent;

    public MemoryImageScreen(MemoryScreen.Memory memory, Screen parent) {
        super(Text.empty());
        MemoryImageScreen.memory = memory;
        this.parent = parent;
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int xOffset = (int) (((float) context.getScaledWindowWidth() / 2) - ((float) 320 / 2));
        int yOffset = (int) (((float) context.getScaledWindowHeight() / 2) - ((float) 168 / 2) - 30);

        context.drawTexture(Identifier.of(SplitSelf.MOD_ID, "textures/gui/memories/" + memory.image() + ".png"),
            xOffset, yOffset,
            0, 0,
            320, 168,
            320, 168);
        context.drawBorder(xOffset, yOffset, 320, 168, 0xFFFFFFFF);

        Text title = SplitSelf.translate("memory.splitself." + memory.image());
        Text description = SplitSelf.translate("memory.splitself." + memory.image() + ".desc");
        context.drawText(this.textRenderer, SplitSelf.translate("memory.splitself." + memory.image()),
            (context.getScaledWindowWidth() / 2) - (this.textRenderer.getWidth(title.getString()) / 2),
            yOffset + 172,
            0xFFFFFFFF, true);

        int maxWidth = Math.min(320, context.getScaledWindowWidth() - 20);
        List<OrderedText> descLines = this.textRenderer.wrapLines(description, maxWidth);
        int descY = yOffset + 182;
        for (OrderedText line : descLines) {
            context.drawText(this.textRenderer, line,
                (context.getScaledWindowWidth() / 2) - (this.textRenderer.getWidth(line) / 2),
                descY,
                0xFF777777, true);
            descY += this.textRenderer.fontHeight + 1;
        }

    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

}
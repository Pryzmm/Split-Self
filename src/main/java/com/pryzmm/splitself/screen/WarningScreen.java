package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.file.DesktopFileUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class WarningScreen extends Screen {

    public WarningScreen() {
        super(Text.literal("Split Self"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Continue"),
                        button -> {
                            DesktopFileUtil.createFileOnDesktop("begin.txt", "Can't you see yourself?");
                        }
                ).position(this.width / 2 - 50, this.height - 50)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        Text str1 = Text.literal("Centered Text");
        String[] lines = {
                "This mod is a horror game, and will break your world.",
                "Your PC will be interacted with outside of the game.",
                "It is not a virus, and will not cause damage to you or your device.",
                "",
                "If you do not want this, download the safe version off the Github."
        };
        int y = 90;
        for (String line : lines) {
            Text lineText = Text.literal(line);
            int x = (this.width - this.textRenderer.getWidth(lineText)) / 2;
            context.drawTextWithShadow(this.textRenderer, lineText, x, y, 0xFFFFFF);
            y += 12; // Increase Y for next line (12 pixels between lines)
        }
    }

        @Override
        public boolean shouldPause () {
            return false;
        }
    }


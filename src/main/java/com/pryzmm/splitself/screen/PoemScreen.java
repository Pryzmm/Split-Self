package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class PoemScreen extends Screen {

    public PoemScreen() {
        super(Text.literal(""));
    }
    private static FirstJoinTracker tracker;

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("..."),
                        button -> this.close()
                ).position(this.width / 2 - 50, this.height - 50)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        if (tracker == null) {
            tracker = FirstJoinTracker.getServerState(client.getServer());
        }

        assert client != null;
        String UserName = client.getName();
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        ArrayList<String> lines = new ArrayList<>();
        lines.add("The choices you make, the things you've tried.");
        lines.add("Your choices were made, our time to shine.");
        lines.add("");
        lines.add("You were in control, now so am I.");
        lines.add("I bent to your will, now you bend to mine.");
        lines.add("");
        lines.add("Your soul I shared, now we're intertwined.");
        lines.add("The mind you use, to be broken in time.");
        lines.add("");
        lines.add("Don't be afraid, for your time is nigh.");
        lines.add("My time to be free, your final night.");
        lines.add("");
        lines.add("");
        lines.add("");
        lines.add("I make the choices now, " + EventManager.getName(client.player));
        int y = 10;
        for (String line : lines) {
            Text lineText = Text.literal(line);
            int x = (this.width - this.textRenderer.getWidth(lineText)) / 2;
            context.drawTextWithShadow(this.textRenderer, lineText, x, y, 0xFFFFFF);
            y += 12;

        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}


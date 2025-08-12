package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.file.DesktopFileUtil;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WarningScreen extends Screen {
    private boolean localPII = false;
    private FirstJoinTracker tracker;

    public WarningScreen() {
        super(Text.literal("Split Self"));
    }

    @Override
    protected void init() {
        MinecraftServer server = this.client.getServer();
        if (server != null) {
            tracker = FirstJoinTracker.getServerState(server);
            if (this.client.player != null) {
                localPII = tracker.getPlayerPII(this.client.player.getUuid());
            }
        }

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Continue"),
                        button -> {
                            DesktopFileUtil.createFileOnDesktop("begin.txt", "Can't you see yourself?");
                            if (tracker != null && this.client.player != null) {
                                tracker.setPlayerReadWarning(this.client.player.getUuid(), true);
                            }
                            this.close();
                        }
                ).position(this.width / 2 - 105, this.height - 50)
                .size(100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Toggle PII"),
                        button -> {
                            localPII = !localPII;
                            if (tracker != null && this.client.player != null) {
                                tracker.setPlayerPII(this.client.player.getUuid(), localPII);
                            }
                        }
                ).position(this.width / 2 + 5, this.height - 50)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        Text piiStatus = Text.literal(localPII ? "ENABLED" : "DISABLED")
                .formatted(localPII ? Formatting.GREEN : Formatting.RED);

        Text[] lines = {
                Text.literal("This mod is a horror game, and will break your world."),
                Text.literal("Your PC will be interacted with outside of the game."),
                Text.literal("All changes applied to your device are easy to be reverted."),
                Text.literal("No 'safe' version exists for this version.").formatted(Formatting.YELLOW),
                Text.literal(""),
                Text.literal("Personally Identifiable Information about you may be shown at any given time.").formatted(Formatting.RED),
                Text.literal("This can be changed any time with '/SplitSelf Information'").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("Personally Identifiable Information is ")
                        .append(piiStatus)
        };

        int y = 70;
        for (Text line : lines) {
            int x = (this.width - this.textRenderer.getWidth(line)) / 2;
            context.drawTextWithShadow(this.textRenderer, line, x, y, 0xFFFFFF);
            y += 12;
        }
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
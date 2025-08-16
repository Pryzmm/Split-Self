package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.file.DesktopFileUtil;
import com.pryzmm.splitself.world.FirstJoinTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(WarningScreen.class);
    private static boolean localPII = false;
    private FirstJoinTracker tracker;

    public WarningScreen() {
        super(SplitSelf.translate("warning.splitself.title"));
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
                        Text.literal(SplitSelf.translate("warning.splitself.continue").getString()),
                        button -> {
                            DesktopFileUtil.createFileOnDesktop(SplitSelf.translate("files.splitself.begin.title").getString() + ".txt", SplitSelf.translate("files.splitself.begin.message").getString());
                            if (tracker != null && this.client.player != null) {
                                tracker.setPlayerReadWarning(this.client.player.getUuid(), true);
                            }
                            this.close();
                        }
                ).position(this.width / 2 - 105, this.height - 50)
                .size(100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        SplitSelf.translate("warning.splitself.PII.toggle"),
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

        Text piiStatus = Text.literal(localPII ? SplitSelf.translate("warning.splitself.PII.enabled").getString() : SplitSelf.translate("warning.splitself.PII.disabled").getString())
                .formatted(localPII ? Formatting.GREEN : Formatting.RED);

        Text[] lines = {
                Text.literal(SplitSelf.translate("warning.splitself.line1").getString()),
                Text.literal(SplitSelf.translate("warning.splitself.line2").getString()),
                Text.literal(SplitSelf.translate("warning.splitself.line3").getString()),
                Text.literal(SplitSelf.translate("warning.splitself.line4").getString()).formatted(Formatting.YELLOW),
                Text.literal(""),
                Text.literal(SplitSelf.translate("warning.splitself.line5").getString()).formatted(Formatting.RED),
                Text.literal(SplitSelf.translate("warning.splitself.line6").getString()).formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal(SplitSelf.translate("warning.splitself.line7").getString())
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
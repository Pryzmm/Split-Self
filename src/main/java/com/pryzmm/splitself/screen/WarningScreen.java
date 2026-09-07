package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.data.ClientData;
import com.pryzmm.splitself.file.DesktopFileUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import java.net.URI;

public class WarningScreen extends Screen {
    private static boolean localPII = false;

    public WarningScreen() {
        super(Text.translatable("warning.splitself.title"));
    }

    @Override
    protected void init() {
        assert this.client != null;
        MinecraftServer server = this.client.getServer();
        if (server != null) {
            localPII = ClientData.getPII();
        }

        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("warning.splitself.continue"),
            button -> {
                DesktopFileUtil.createFileOnDesktop(Text.translatable("files.splitself.begin.title").getString() + ".txt", Text.translatable("files.splitself.begin.message").getString());
                ClientData.setPanoramaStage("main");
                this.close();
            }
        ).position(this.width / 2 - 105, this.height - 50)
        .size(100, 20)
        .build());
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("warning.splitself.PII.toggle"),
            button -> {
                localPII = !localPII;
                ClientData.setPII(localPII);
            }
        ).position(this.width / 2 + 5, this.height - 50)
        .size(100, 20)
        .build());
        if (!SplitSelf.IS_UNSAFE_VERSION) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("warning.splitself.download_unsafe"),
                button -> Util.getOperatingSystem().open(URI.create("https://modrinth.com/mod/split-self"))
            ).position((this.width / 2 - 100), this.height - 25)
            .size(200, 20)
            .build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        Text piiStatus = (localPII ? Text.translatable("warning.splitself.PII.enabled") : Text.translatable("warning.splitself.PII.disabled")).formatted(localPII ? Formatting.GREEN : Formatting.RED);

        Text[] lines = {
            Text.translatable("warning.splitself.line1"),
            (SplitSelf.IS_UNSAFE_VERSION) ? Text.translatable("warning.splitself.line2") : Text.translatable("warning.splitself.line2.safe"),
            (SplitSelf.IS_UNSAFE_VERSION) ? Text.translatable("warning.splitself.line3") : Text.translatable("warning.splitself.line3.safe"),
            (SplitSelf.IS_UNSAFE_VERSION) ? Text.translatable("warning.splitself.line4").formatted(Formatting.YELLOW) : Text.translatable("warning.splitself.line4.safe").formatted(Formatting.YELLOW),
            Text.literal(""),
            Text.translatable("warning.splitself.line5").formatted(Formatting.RED),
            Text.translatable("warning.splitself.line6").formatted(Formatting.GRAY),
            Text.literal(""),
            Text.translatable("warning.splitself.line7").append(piiStatus)
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
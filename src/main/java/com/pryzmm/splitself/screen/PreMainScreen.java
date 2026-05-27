package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.file.ZipFunc;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PreMainScreen extends Screen {

    private final boolean hasDownloadedExternals = ZipFunc.downloadedFiles;
    private final boolean hasShriek = SplitSelf.ShriekInstalled;
    private final boolean hasInternet = isInternetAvailable();

    public static boolean viewedScreen = false;

    private static final Identifier installedIcon = Identifier.of(SplitSelf.MOD_ID, "textures/gui/sprites/icon/success.png");
    private static final Identifier notInstalledIcon = Identifier.of(SplitSelf.MOD_ID, "textures/gui/sprites/icon/fail.png");

    public PreMainScreen() { super(Text.empty()); }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.continue"),
            button -> {
                viewedScreen = true;
                this.close();
            }
        ).position(this.width / 2 - 50, this.height - 50)
        .size(100, 20)
        .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        LogoDrawer logoDrawer = new LogoDrawer(false);
        logoDrawer.draw(context, context.getScaledWindowWidth(), 255);

        Text t1 = Text.translatable("misc.splitself.best_experience");
        context.drawText(textRenderer, t1, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t1) / 2), (context.getScaledWindowHeight() / 4) + 30, 0xFFFFFFFF, false);

        Text t2 = Text.translatable("misc.splitself.best_experience.internet");
        context.drawText(textRenderer, t2, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t2) / 2) + 6, (context.getScaledWindowHeight() / 4) + 50, 0xFFFFFFFF, false);
        context.drawTexture(hasInternet ? installedIcon : notInstalledIcon, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t2) / 2) - 9, (context.getScaledWindowHeight() / 4) + 48, 0, 0, 12, 12, 12, 12);

        Text t3 = Text.translatable("misc.splitself.best_experience.externals");
        context.drawText(textRenderer, t3, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t3) / 2) + 6, (context.getScaledWindowHeight() / 4) + 62, 0xFFFFFFFF, false);
        context.drawTexture(hasDownloadedExternals ? installedIcon : notInstalledIcon, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t3) / 2) - 9, (context.getScaledWindowHeight() / 4) + 60, 0, 0, 12, 12, 12, 12);

        Text t4 = Text.translatable("misc.splitself.best_experience.shriek");
        context.drawText(textRenderer, t4, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t4) / 2) + 6, (context.getScaledWindowHeight() / 4) + 74, 0xFFFFFFFF, false);
        context.drawTexture(hasShriek ? installedIcon : notInstalledIcon, (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(t4) / 2) - 9, (context.getScaledWindowHeight() / 4) + 72, 0, 0, 12, 12, 12, 12);
    }



    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private static final String TEST_URL = "https://google.com/generate_204";

    public static boolean isInternetAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEST_URL))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(3))
                .build();
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) { return false; }
    }

}

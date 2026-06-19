package com.pryzmm.splitself.screen;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.client.SplitSelfClient;
import com.pryzmm.splitself.file.ZipFunc;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LoadingResourcesScreen extends Screen {

    public LoadingResourcesScreen() {
        super(Text.empty());

        if (ZipFunc.needsVideoDownloads()) {
            SplitSelf.LOGGER.info("Performing one-time video download");
            // The link below downloads all necessary files for the mod to function as intended.
            // If it cannot be successfully downloaded, it will try to work around wherever downloaded files are used via streaming or other methods.
            ZipFunc.downloadAndExtract("https://www.dropbox.com/scl/fo/zbc2w6xo70rluzd0t8g50/AIx7n0e8yuQqTpw9G1-bhUI?rlkey=0j7fos8c24tckpxo5vtj50rj4&st=9wv05gpz&dl=1", ZipFunc.getDest())
                .thenAccept(v -> SplitSelfClient.loadedResources = true)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        SplitSelf.LOGGER.error("Download failed: {}", e.getMessage());
                        SplitSelfClient.resourcesFailed = true;
                        SplitSelfClient.loadedResources = true;
                    }
                });
        } else {
            SplitSelf.LOGGER.info("Skipping one-time video download");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        LogoDrawer logoDrawer = new LogoDrawer(false);
        logoDrawer.draw(context, context.getScaledWindowWidth(), 255);
        if (!SplitSelfClient.loadedResources) {
            String text = "Loading external resources... " + ZipFunc.getDownloadedBytes() / 1000000 + "MB/" + ZipFunc.getTotalBytes() / 1000000 + "MB";
            context.drawText(textRenderer, Text.literal(text), (context.getScaledWindowWidth() / 2) - (textRenderer.getWidth(text) / 2), context.getScaledWindowHeight() / 2, 0xFFFFFFFF, false);
            context.drawBorder((context.getScaledWindowWidth() / 2) - 100, context.getScaledWindowHeight() / 2 + 10, 200, 10, 0xFFFFFFFF);
            context.fill((context.getScaledWindowWidth() / 2) - 98, context.getScaledWindowHeight() / 2 + 12, ((context.getScaledWindowWidth() / 2) - 98) + (int) ((ZipFunc.getDownloadProgress() >= 0 ? ZipFunc.getDownloadProgress() : 0) * 196), context.getScaledWindowHeight() / 2 + 18, 0xFFFFFFFF);
        } else MinecraftClient.getInstance().setScreen(new PreMainScreen());

    }

    @Override
    public void close() {
        if (SplitSelfClient.loadedResources || SplitSelfClient.resourcesFailed) super.close();
    }

}

package com.pryzmm.splitself;

import com.pryzmm.splitself.block.entity.ModBlockEntities;
import com.pryzmm.splitself.client.ClientDetector;
import com.pryzmm.splitself.client.render.ImageFrameBlockEntityRenderer;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheForgottenModel;
import com.pryzmm.splitself.entity.client.TheForgottenRenderer;
import com.pryzmm.splitself.entity.client.TheOtherModel;
import com.pryzmm.splitself.entity.client.TheOtherRenderer;
import com.pryzmm.splitself.file.BrowserHistoryReader;
import com.pryzmm.splitself.screen.SkyImageRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class SplitSelfClient implements ClientModInitializer {

    public static PlayerEntity player;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER, TheOtherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER_SLIM, TheOtherModel::getSlimTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheOther, TheOtherRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(TheForgottenModel.THEFORGOTTEN, TheForgottenModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheForgotten, TheForgottenRenderer::new);

        SkyImageRenderer.register();
        BlockEntityRendererFactories.register(ModBlockEntities.IMAGE_FRAME_BLOCK_ENTITY, ImageFrameBlockEntityRenderer::new);

        ClientPlayConnectionEvents.JOIN.register((clientPlayNetworkHandler, packetSender, minecraftClient) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (ClientDetector.isFeatherClient()) {
                client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("misc.splitself.featherClient").getString()).formatted(Formatting.YELLOW), false);
            }
            if (!net.minecraft.util.Util.getOperatingSystem().toString().toLowerCase().contains("win")) {
                client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("misc.splitself.windowsSupport").getString()).formatted(Formatting.RED), false);
            }
            player = MinecraftClient.getInstance().player;

            System.out.println("getting history");
            List<BrowserHistoryReader.HistoryEntry> history = BrowserHistoryReader.getHistory();
            for (BrowserHistoryReader.HistoryEntry historyEntry : history) {
                System.out.println(historyEntry.title);
                if (historyEntry.title.contains("9Minecraft")) {
                    client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("misc.splitself.9Minecraft").getString()).formatted(Formatting.YELLOW), false);
                    break;
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen titleScreen) {
                ButtonWidget multiplayerButton = findButtonByText(titleScreen, "menu.multiplayer");
                if (multiplayerButton != null) {
                    multiplayerButton.active = false;
                    multiplayerButton.setTooltip(Tooltip.of(SplitSelf.translate("misc.splitself.multiplayer")));
                }
                ButtonWidget realmsButton = findButtonByText(titleScreen, "menu.online");
                if (realmsButton != null) {
                    realmsButton.setTooltip(Tooltip.of(SplitSelf.translate("misc.splitself.realms")));
                    realmsButton.active = false;
                    }
            }
        });
    }

    public static ButtonWidget findButtonByText(Screen screen, String translation) {
        return screen.children().stream()
                .filter(element -> element instanceof ButtonWidget)
                .map(element -> (ButtonWidget) element)
                .filter(button -> button.getMessage().getString().equals(
                        Text.translatable(translation).getString()))
                .findFirst()
                .orElse(null);
    }
}

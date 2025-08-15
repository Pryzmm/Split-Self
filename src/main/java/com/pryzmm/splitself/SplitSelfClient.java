package com.pryzmm.splitself;

import com.pryzmm.splitself.client.ClientDetector;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheOtherModel;
import com.pryzmm.splitself.entity.client.TheOtherRenderer;
import com.pryzmm.splitself.events.ScreenOverlay;
import com.pryzmm.splitself.screen.SkyImageRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public class SplitSelfClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER, TheOtherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER_SLIM, TheOtherModel::getSlimTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheOther, TheOtherRenderer::new);

        SkyImageRenderer.register();

        ClientPlayConnectionEvents.JOIN.register((clientPlayNetworkHandler, packetSender, minecraftClient) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (ClientDetector.isFeatherClient()) {
                client.getServer().getPlayerManager().broadcast(Text.literal(SplitSelf.translate("misc.splitself.featherClient").getString()).formatted(Formatting.RED), false);
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen) {
                if (client.player != null) {
                    Random random = new Random();
                    if (random.nextInt(1000) == 0) {
                        ScreenOverlay.executeInventoryScreen(client.player);
                    }
                }
            }
            if (screen instanceof TitleScreen titleScreen) {
                ButtonWidget multiplayerButton = findButtonByText(titleScreen, "menu.multiplayer");
                multiplayerButton.active = false;
                multiplayerButton.setTooltip(Tooltip.of(SplitSelf.translate("misc.splitself.multiplayer")));
                ButtonWidget realmsButton = findButtonByText(titleScreen, "menu.online");
                realmsButton.setTooltip(Tooltip.of(SplitSelf.translate("misc.splitself.realms")));
                realmsButton.active = false;
            }
        });
    }

    private ButtonWidget findButtonByText(TitleScreen screen, String translation) {
        return screen.children().stream()
                .filter(element -> element instanceof ButtonWidget)
                .map(element -> (ButtonWidget) element)
                .filter(button -> button.getMessage().getString().equals(
                        Text.translatable(translation).getString()))
                .findFirst()
                .orElse(null);
    }
}

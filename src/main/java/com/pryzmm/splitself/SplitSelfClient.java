package com.pryzmm.splitself;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheOtherModel;
import com.pryzmm.splitself.entity.client.TheOtherRenderer;
import com.pryzmm.splitself.events.ScreenOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

import java.util.Random;

public class SplitSelfClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER, TheOtherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER_SLIM, TheOtherModel::getSlimTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheOther, TheOtherRenderer::new);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen) {
                if (client.player != null) {
                    Random random = new Random();
                    if (random.nextInt(1000) == 0) {
                        ScreenOverlay.executeInventoryScreen(client.player);
                    }
                }
            }
        });

    }
}

package com.pryzmm.splitself;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheOtherModel;
import com.pryzmm.splitself.entity.client.TheOtherRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class SplitSelfClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER, TheOtherModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheOther, TheOtherRenderer::new);
    }
}

package com.pryzmm.splitself.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SplitSelfClientEvents {
    private SplitSelfClientEvents() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(UIButtonRenderer.LAYER,UIButtonRenderer::layer);
        event.registerLayerDefinition(TheForgottenModel.LAYER, TheForgottenModel::layer);
        event.registerLayerDefinition(TheOtherModel.CLASSIC, () -> TheOtherModel.layer(false));
        event.registerLayerDefinition(TheOtherModel.SLIM, () -> TheOtherModel.layer(true));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SplitSelfForge.IMAGE_FRAME_ENTITY.get(),ImageFrameRenderer::new);
        event.registerEntityRenderer(SplitSelfForge.UI_BUTTON.get(), UIButtonRenderer::new);
        event.registerEntityRenderer(SplitSelfForge.THE_FORGOTTEN.get(), TheForgottenRenderer::new);
        event.registerEntityRenderer(SplitSelfForge.THE_OTHER.get(), TheOtherRenderer::new);
    }
}

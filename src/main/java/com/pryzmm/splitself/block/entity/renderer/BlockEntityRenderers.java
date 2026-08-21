package com.pryzmm.splitself.block.entity.renderer;

import com.pryzmm.splitself.block.entity.ModBlockEntities;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class BlockEntityRenderers {

    public static void register() {
        BlockEntityRendererFactories.register(ModBlockEntities.IMAGE_FRAME_BLOCK_ENTITY, ImageFrameBlockEntityRenderer::new);
    }

}

package com.pryzmm.splitself.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ImageFrameBlockEntity extends BlockEntity {

    public ImageFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IMAGE_FRAME_BLOCK_ENTITY, pos, state);
    }
}

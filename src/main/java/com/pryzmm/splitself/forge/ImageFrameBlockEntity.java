package com.pryzmm.splitself.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stores the original frame's ImageId without accessing local screenshots. */
final class ImageFrameBlockEntity extends BlockEntity {
    private Integer imageId;

    ImageFrameBlockEntity(BlockPos pos, BlockState state) {
        super(SplitSelfForge.IMAGE_FRAME_ENTITY.get(), pos, state);
    }

    Integer imageId() {
        return imageId;
    }

    void setImageId(Integer imageId) {
        this.imageId = imageId;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (imageId != null) tag.putInt("ImageId", imageId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        imageId = tag.contains("ImageId") ? tag.getInt("ImageId") : null;
    }
}

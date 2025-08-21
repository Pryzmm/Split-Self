package com.pryzmm.splitself.block.entity;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<ImageFrameBlockEntity> IMAGE_FRAME_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(SplitSelf.MOD_ID, "image_frame"),
                    BlockEntityType.Builder.create(ImageFrameBlockEntity::new, ModBlocks.IMAGE_FRAME).build()
            );

    public static void registerBlockEntities() {}
}
package com.pryzmm.splitself.world;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class IntegrityProcessor extends StructureProcessor {
    private final float integrity;

    public IntegrityProcessor(float integrity) {
        this.integrity = Math.max(0.0f, Math.min(1.0f, integrity));
    }

    @Override
    public @Nullable StructureBlockInfo process(WorldView worldView, BlockPos pos, BlockPos pivot, StructureBlockInfo originalBlockInfo, StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        super.process(worldView, pos, pivot, originalBlockInfo, currentBlockInfo, data);
        if (integrity <= 0.0f) {
            return null;
        }

        if (integrity >= 1.0f) {
            return currentBlockInfo;
        }

        if (new Random().nextFloat() <= integrity) {
            return currentBlockInfo;
        } else {
            return null;
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return null;
    }
}
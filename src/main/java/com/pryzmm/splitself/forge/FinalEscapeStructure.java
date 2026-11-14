package com.pryzmm.splitself.forge;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** Pinned FinalEscapeStructure: flat search, Y-8, unrotated authored exit. */
final class FinalEscapeStructure extends Structure {
    static final Codec<FinalEscapeStructure> CODEC = simpleCodec(FinalEscapeStructure::new);
    FinalEscapeStructure(StructureSettings settings) { super(settings); }

    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, pieces -> {
            BlockPos center = context.chunkPos().getMiddleBlockPosition(0);
            BlockPos flat = context.chunkGenerator() instanceof GrassEmptyChunkGenerator grass
                    ? grass.findFlatCenter(center.getX(), center.getZ(), 2) : null;
            if (flat == null) {
                int y = context.chunkGenerator().getBaseHeight(center.getX(), center.getZ(),
                        Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                flat = new BlockPos(center.getX(), y, center.getZ());
            }
            pieces.addPiece(new Piece(context.structureTemplateManager(), flat.below(8)));
        });
    }
    @Override public StructureType<?> type() { return SplitSelfForge.FINAL_ESCAPE.get(); }

    static final class Piece extends TemplateStructurePiece {
        private static final ResourceLocation TEMPLATE = new ResourceLocation("splitself", "final_escape");
        Piece(StructureTemplateManager manager, BlockPos pos) {
            super(SplitSelfForge.FINAL_ESCAPE_PIECE.get(), 0, manager, TEMPLATE,
                    TEMPLATE.toString(), settings(), pos);
        }
        Piece(StructureTemplateManager manager, CompoundTag tag) {
            super(SplitSelfForge.FINAL_ESCAPE_PIECE.get(), tag, manager, id -> settings());
        }
        private static StructurePlaceSettings settings() {
            return new StructurePlaceSettings().setIgnoreEntities(false).setKnownShape(true)
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }
        @Override protected void handleDataMarker(String marker, BlockPos pos,
                ServerLevelAccessor level, RandomSource random, BoundingBox box) {}
    }
}

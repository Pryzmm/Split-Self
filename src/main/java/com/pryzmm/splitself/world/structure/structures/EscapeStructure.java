package com.pryzmm.splitself.world.structure.structures;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pryzmm.splitself.world.DeadCoralChunkGenerator;
import com.pryzmm.splitself.world.structure.Structures;
import com.pryzmm.splitself.world.structure.pieces.EscapePiece;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import java.util.Optional;

public class EscapeStructure extends Structure {
    public static final MapCodec<EscapeStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Structure.configCodecBuilder(instance)).apply(instance, EscapeStructure::new));

    public EscapeStructure(Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        return Structure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> {
            BlockPos chunkCenter = context.chunkPos().getCenterAtY(0);
            BlockPos flatPos = null;
            if (context.chunkGenerator() instanceof DeadCoralChunkGenerator deadCoral) flatPos = deadCoral.findFlatCenter(chunkCenter.getX(), chunkCenter.getZ(), 6);
            if (flatPos == null) {
                int y = context.chunkGenerator().getHeight(
                    chunkCenter.getX(), chunkCenter.getZ(),
                    Heightmap.Type.WORLD_SURFACE_WG,
                    context.world(), context.noiseConfig()
                );
                flatPos = new BlockPos(chunkCenter.getX(), y, chunkCenter.getZ());
            }
            BlockPos finalPos = new BlockPos(flatPos.getX(), DeadCoralChunkGenerator.MIN_GENERATION_Y, flatPos.getZ());
            collector.addPiece(new EscapePiece(context.structureTemplateManager(), finalPos));
        });
    }

    @Override
    public StructureType<?> getType() {
        return Structures.ESCAPE;
    }

}
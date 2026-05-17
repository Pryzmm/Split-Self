package com.pryzmm.splitself.world.structure.pieces;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.events.StructureManager;
import com.pryzmm.splitself.world.structure.StructurePieces;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MinesPiece extends SimpleStructurePiece {
    public static final String PIECE = "es_mines";
    private static final Identifier TEMPLATE_ID = Identifier.of(SplitSelf.MOD_ID, PIECE);
    private static final Set<BlockPos> PLACED = Collections.synchronizedSet(new HashSet<>());

    public MinesPiece(StructureTemplateManager manager, BlockPos pos) {
        super(StructurePieces.MINES, 0, manager, TEMPLATE_ID,
            TEMPLATE_ID.toString(), createPlacementData(), pos);

        try {
            String resourcePath = "/data/" + SplitSelf.MOD_ID + "/structures/" + PIECE + ".nbt";
            InputStream inputStream = MinesPiece.class.getResourceAsStream(resourcePath);
            if (inputStream != null) {
                NbtCompound nbt = NbtIo.readCompressed(inputStream, NbtSizeTracker.ofUnlimitedBytes());
                inputStream.close();
                NbtList sizeNbt = nbt.getList("size", 3);
                int sizeX = sizeNbt.getInt(0);
                int sizeY = sizeNbt.getInt(1);
                int sizeZ = sizeNbt.getInt(2);
                this.boundingBox = new BlockBox(
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + sizeX - 1,
                    pos.getY() + sizeY - 1,
                    pos.getZ() + sizeZ - 1
                );
            }
        } catch (Exception e) {
            SplitSelf.LOGGER.error("Failed to read mines template size: {}", e.getMessage());
        }
    }

    public MinesPiece(StructureTemplateManager manager, NbtCompound nbt) {
        super(StructurePieces.MINES, nbt, manager, id -> createPlacementData());
    }

    private static StructurePlacementData createPlacementData() {
        return new StructurePlacementData()
            .setRotation(BlockRotation.NONE)
            .setMirror(BlockMirror.NONE)
            .setIgnoreEntities(false);
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator generator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        if (!PLACED.add(this.pos)) {
            return;
        }
        world.toServerWorld().getServer().execute(() -> StructureManager.placeStructure(
            world.toServerWorld(), this.pos, PIECE,
            BlockRotation.NONE, BlockMirror.NONE, 1.0f, false
        ));
    }

    @Override
    protected void handleMetadata(String metadata, BlockPos pos, net.minecraft.world.ServerWorldAccess world, Random random, BlockBox boundingBox) {}

}
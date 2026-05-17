package com.pryzmm.splitself.world.structure;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.world.structure.pieces.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;

public class StructurePieces {
    public static StructurePieceType HOUSE;
    public static StructurePieceType BURIAL;
    public static StructurePieceType MINES;
    public static StructurePieceType PILLAR;
    public static StructurePieceType CREEPER;
    public static StructurePieceType ESCAPE;

    public static void register() {
        HOUSE = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(SplitSelf.MOD_ID, HousePiece.PIECE), (manager, nbt) -> new HousePiece(manager.structureTemplateManager(), nbt));
        BURIAL = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(SplitSelf.MOD_ID, BurialPiece.PIECE), (manager, nbt) -> new BurialPiece(manager.structureTemplateManager(), nbt));
        MINES = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(SplitSelf.MOD_ID, MinesPiece.PIECE), (manager, nbt) -> new MinesPiece(manager.structureTemplateManager(), nbt));
        PILLAR = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(SplitSelf.MOD_ID, PillarPiece.PIECE), (manager, nbt) -> new PillarPiece(manager.structureTemplateManager(), nbt));
        CREEPER = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(SplitSelf.MOD_ID, CreeperPiece.PIECE), (manager, nbt) -> new CreeperPiece(manager.structureTemplateManager(), nbt));
        ESCAPE = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(SplitSelf.MOD_ID, EscapePiece.PIECE), (manager, nbt) -> new EscapePiece(manager.structureTemplateManager(), nbt));
    }
}
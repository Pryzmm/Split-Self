package com.pryzmm.splitself.world.structure;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.world.structure.pieces.*;
import com.pryzmm.splitself.world.structure.structures.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class Structures {
    public static StructureType<HouseStructure> HOUSE;
    public static StructureType<BurialStructure> BURIAL;
    public static StructureType<MinesStructure> MINES;
    public static StructureType<PillarStructure> PILLAR;
    public static StructureType<CreeperStructure> CREEPER;
    public static StructureType<EscapeStructure> ESCAPE;

    public static void register() {
        HOUSE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(SplitSelf.MOD_ID, HousePiece.PIECE), () -> HouseStructure.CODEC);
        BURIAL = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(SplitSelf.MOD_ID, BurialPiece.PIECE), () -> BurialStructure.CODEC);
        MINES = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(SplitSelf.MOD_ID, MinesPiece.PIECE), () -> MinesStructure.CODEC);
        PILLAR = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(SplitSelf.MOD_ID, PillarPiece.PIECE), () -> PillarStructure.CODEC);
        CREEPER = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(SplitSelf.MOD_ID, CreeperPiece.PIECE), () -> CreeperStructure.CODEC);
        ESCAPE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(SplitSelf.MOD_ID, EscapePiece.PIECE), () -> EscapeStructure.CODEC);
    }
}
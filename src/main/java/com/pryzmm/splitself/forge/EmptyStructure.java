package com.pryzmm.splitself.forge;

import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraftforge.registries.*;

/** Six pinned Empty structures with a distinct registered type/piece/codec per source name. */
final class EmptyStructure extends Structure{
    private static final Map<String,RegistryObject<StructureType<EmptyStructure>>> TYPES=new HashMap<>();
    private static final Map<String,RegistryObject<StructurePieceType>> PIECES=new HashMap<>();
    private final String name;
    EmptyStructure(StructureSettings settings,String name){super(settings);this.name=name;}
    static void register(DeferredRegister<StructureType<?>> types,DeferredRegister<StructurePieceType> pieces){
        for(String name:EmptyStructureRules.NAMES){Codec<EmptyStructure> codec=simpleCodec(settings->new EmptyStructure(settings,name));
            TYPES.put(name,types.register(name,()->()->codec));
            PIECES.put(name,pieces.register(name,()->(context,tag)->new Piece(context.structureTemplateManager(),tag,name)));
        }
    }
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context){
        return onTopOfChunkCenter(context,Heightmap.Types.WORLD_SURFACE_WG,builder->{
            BlockPos center=context.chunkPos().getMiddleBlockPosition(0);
            BlockPos flat=context.chunkGenerator() instanceof DeadCoralChunkGenerator dead?dead.findFlatCenter(center.getX(),center.getZ(),EmptyStructureRules.radius(name)):null;
            if(flat==null)flat=new BlockPos(center.getX(),context.chunkGenerator().getBaseHeight(center.getX(),center.getZ(),Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState()),center.getZ());
            builder.addPiece(new Piece(context.structureTemplateManager(),EmptyStructureRules.origin(name,flat),name));
        });
    }
    @Override public StructureType<?> type(){return TYPES.get(name).get();}
    private static StructurePlaceSettings settings(){return new StructurePlaceSettings().setIgnoreEntities(false).setKnownShape(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);}
    static final class Piece extends TemplateStructurePiece{
        Piece(StructureTemplateManager manager,BlockPos pos,String name){super(PIECES.get(name).get(),0,manager,new ResourceLocation("splitself",name),"splitself:"+name,settings(),pos);}
        Piece(StructureTemplateManager manager,CompoundTag tag,String name){super(PIECES.get(name).get(),tag,manager,id->settings());}
        @Override protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor level,RandomSource random,BoundingBox box){}
    }
}

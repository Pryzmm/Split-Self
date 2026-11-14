package com.pryzmm.splitself.forge;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** All world access is on the owning server thread; no unloaded chunk generation. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID)
public final class BatchWorldEvents {
    private record Job(ServerLevel world, BlockPos origin, boolean coral, BatchEventRules.Layers layers) {}
    private static final class Mining {
        final ServerPlayer player;
        final ServerLevel world;
        int step;
        long next;
        Mining(ServerPlayer player, long now) { this.player=player; world=player.serverLevel(); next=now; }
        void sound(long now) {
            world.playSound(null,player.blockPosition(),SoundEvents.STONE_BREAK,SoundSource.BLOCKS,
                    BatchEventRules.miningVolume(step++),1);
            next=now+1000;
        }
    }
    private static final List<Job> JOBS = new ArrayList<>();
    private static final List<Mining> MINING = new ArrayList<>();
    private BatchWorldEvents() {}
    static boolean run(ServerPlayer player, String event) {
        ServerLevel world = player.serverLevel();
        long now = System.nanoTime()/1_000_000;
        if (event.equals("UNDERGROUNDMINING")) {
            Mining mining=new Mining(player,now); mining.sound(now); MINING.add(mining); return true;
        }
        if (event.equals("LIFT")) return lift(player);
        if (event.equals("DESTROYCHUNK")) {
            var random = new java.util.Random();
            BlockPos center = BatchEventRules.destroyCenter(player.position(),random.nextInt(40),random.nextInt(40),
                    bed(player),EventCatalog.baseSafeRadius());
            JOBS.add(new Job(world,center.offset(-8,0,-8),false,
                    new BatchEventRules.Layers(world.getMaxBuildHeight(),world.getMinBuildHeight(),-1,50,now)));
            return true;
        }
        if (event.equals("DEADCHUNK")) {
            var random = new java.util.Random();
            JOBS.add(new Job(world,BatchEventRules.deadOrigin(shifted(player),random.nextDouble(),random.nextDouble()),true,
                    new BatchEventRules.Layers(world.getMinBuildHeight(),world.getMaxBuildHeight()-1,1,10,now)));
            return true;
        }
        return false;
    }
    private static BlockPos bed(ServerPlayer player) {
        return player.getRespawnDimension().equals(player.level().dimension()) ? player.getRespawnPosition() : null;
    }
    static Vec3 shifted(ServerPlayer player) {
        return WorldManifestationRules.moveVectorFromBase(player.position(),bed(player),EventCatalog.baseSafeRadius());
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long now=System.nanoTime()/1_000_000;
        MINING.removeIf(mining -> {
            if (mining.world.getServer()!=event.getServer()) return false;
            if (mining.player.isRemoved()) return true;
            if (now>=mining.next) mining.sound(now);
            return mining.step>=10;
        });
        JOBS.removeIf(job -> {
            if (job.world.getServer()!=event.getServer()) return false;
            job.layers.advance(now,y -> layer(job,y));
            return job.layers.finished();
        });
    }
    private static void layer(Job job, int y) {
        BatchEventRules.visitLayer(job.origin,y,pos -> {
            if (!available(job.world,pos)) return;
            var state=job.world.getBlockState(pos);
            if (BatchEventRules.replace(state.isAir(),state.isSolidRender(job.world,pos),job.coral))
                job.world.setBlock(pos,job.coral ? Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        });
    }
    private static boolean available(ServerLevel world, BlockPos pos) {
        return !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos);
    }
    private static boolean regionAvailable(ServerLevel world, BlockPos first, BlockPos last) {
        if (world.isOutsideBuildHeight(first) || world.isOutsideBuildHeight(last)) return false;
        for(int x=first.getX();x<=last.getX();x++) for(int z=first.getZ();z<=last.getZ();z++)
            if(!world.hasChunkAt(new BlockPos(x,first.getY(),z))) return false;
        return true;
    }
    private static boolean lift(ServerPlayer player) { return lift(player,40); }
    static boolean lift(ServerPlayer player,int height) {
        return lift(player,height,shifted(player),player.serverLevel());
    }
    static boolean lift(ServerPlayer player,int height,Vec3 anchor,ServerLevel world) {
        var random=world.random;
        BlockPos center=BatchEventRules.liftCenter(anchor,random.nextDouble(),random.nextDouble(),random.nextDouble());
        if(!world.hasChunkAt(center)) return true;
        int surface=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,center.getX(),center.getZ());
        BlockPos minimum=BatchEventRules.liftMinimum(center,surface);
        BlockPos maximum=BatchEventRules.liftMaximum(center,surface);
        BlockPos destination=new BlockPos(center.getX()-7,surface+height,center.getZ()-7);
        if(!regionAvailable(world,minimum,maximum) || !regionAvailable(world,destination,destination.offset(14,21,14))) return true;
        StructureTemplate template=new StructureTemplate();
        template.fillFromWorld(world,minimum,new Vec3i(15,22,15),true,null);
        // Original writes/overwrites clone.nbt before deleting the selected area, including containers.
        try {
            var directory=player.server.getWorldPath(LevelResource.ROOT).resolve("generated/splitself/structures");
            java.nio.file.Files.createDirectories(directory);
            NbtIo.writeCompressed(template.save(new CompoundTag()),directory.resolve("clone.nbt").toFile());
        } catch (Exception failure) {
            org.slf4j.LoggerFactory.getLogger("splitself").warn("LIFT could not save clone",failure); return false;
        }
        for(int y=minimum.getY();y<=maximum.getY();y++) for(int x=minimum.getX();x<=maximum.getX();x++) for(int z=minimum.getZ();z<=maximum.getZ();z++) {
            BlockPos pos=new BlockPos(x,y,z);
            if(!world.getBlockState(pos).isAir()) { world.removeBlockEntity(pos); world.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL); }
        }
        var settings=new StructurePlaceSettings().setIgnoreEntities(true).setKnownShape(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).addProcessor(new BlockRotProcessor(.75f));
        return template.placeInWorld(world,destination,destination,settings,world.random,2);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { JOBS.clear(); MINING.clear(); }
}

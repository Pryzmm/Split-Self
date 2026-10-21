package com.pryzmm.splitself.forge;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Source StripMine: 10 consecutive two-high corridor blocks, N/S/E/W priority, skip facing direction. */
@Mod.EventBusSubscriber(modid=SplitSelfForge.MOD_ID)
public final class StripMineEncounters{
    private StripMineEncounters(){}
    static BlockPos position(BlockPos origin,Direction facing,java.util.function.Predicate<BlockPos> valid){
        for(Direction direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
            if(direction==facing)continue;boolean corridor=true;for(int i=1;i<=10;i++)if(!valid.test(origin.relative(direction,i))){corridor=false;break;}
            if(corridor)return origin.relative(direction,9);
        }return null;
    }
    static boolean spawn(ServerPlayer player){
        var world=player.serverLevel();BlockPos pos=position(player.blockPosition(),player.getDirection(),p->{
            if(!world.hasChunkAt(p)||world.isOutsideBuildHeight(p)||world.isOutsideBuildHeight(p.above(2)))return false;
            return !world.getBlockState(p).isSolidRender(world,p)&&world.getBlockState(p.below()).isSolidRender(world,p.below())&&!world.getBlockState(p.above()).isSolidRender(world,p.above())&&world.getBlockState(p.above(2)).isSolidRender(world,p.above(2));
        });
        if(pos==null)return false;var actor=SplitSelfForge.THE_FORGOTTEN.get().create(world);if(actor==null)return false;
        actor.setEncounterType(1);actor.moveTo(pos.getX(),pos.getY(),pos.getZ(),360,0);return world.addFreshEntity(actor);
    }
    @SubscribeEvent public static void mined(net.minecraftforge.event.level.BlockEvent.BreakEvent event){
        if(event.getPlayer() instanceof ServerPlayer player && (int)(Math.random()*300)<=1 && event.getPos().getY()==player.getY()+1){
            var actor=SplitSelfForge.THE_FORGOTTEN.get().create(player.serverLevel());if(actor!=null){actor.setEncounterType(1);actor.moveTo(event.getPos().getX(),event.getPos().getY()-1,event.getPos().getZ(),player.getYRot()+180,0);player.serverLevel().addFreshEntity(actor);TickScheduler.schedule(2,actor::discard);}
        }
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){if(event.phase==TickEvent.Phase.END)for(var player:event.getServer().getPlayerList().getPlayers())if(Math.random()*500<1)spawn(player);}
}

package com.pryzmm.splitself.forge;
/** Same pinned ChunkDestroyer policy as LIFT/SURROUND, with finale offset -12. */
final class FinaleChunkEffect {
    private FinaleChunkEffect(){}
    static void start(net.minecraft.server.MinecraftServer server){liftOneChunk(server);TickScheduler.schedule(Math.max(1,(long)(Math.random()*40)),()->start(server));}
    static void liftOneChunk(net.minecraft.server.MinecraftServer server){var world=server.overworld();var players=world.players().stream().filter(p->p.isAlive()&&!p.isSpectator()).toList();if(!players.isEmpty())BatchWorldEvents.lift(players.get(world.random.nextInt(players.size())),-12);}
}

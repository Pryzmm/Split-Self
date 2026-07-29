package com.pryzmm.splitself.packet;

import com.pryzmm.splitself.block.functions.EmptyTeleportBlockFunc;
import com.pryzmm.splitself.data.ClientData;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.item.MemoryItem;
import com.pryzmm.splitself.item.ModItems;
import com.pryzmm.splitself.packet.packets.*;
import com.pryzmm.splitself.world.DeadCoralChunkGenerator;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Random;

public class ServerPacketHandler {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(BrokenEffectPacket.ID, BrokenEffectPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(EndBrokenEffectPacket.ID, EndBrokenEffectPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(EventPacket.ID, EventPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ChatEventPacket.ID, ChatEventPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SleepEventPacket.ID, SleepEventPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(MemoryScreenPacket.ID, MemoryScreenPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(KickScreenPacket.ID, KickScreenPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PartyTimePacket.ID, PartyTimePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BrokenEffectPacket.ID, (payload, context) -> context.server().execute(() -> {
            for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new BrokenEffectPacket());
            }
        }));

        ServerPlayNetworking.registerGlobalReceiver(PartyTimePacket.ID, (payload, context) -> context.server().execute(() -> {
            for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new PartyTimePacket(payload.partyType()));
            }
        }));

        ServerPlayNetworking.registerGlobalReceiver(EndBrokenEffectPacket.ID, (payload, context) -> context.server().execute(() -> {
            ServerWorld emptyWorld = context.server().getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY);
            BlockPos pos = DeadCoralChunkGenerator.findGroundPos(0, 0);
            EmptyTeleportBlockFunc.updateLastLocation(context.player());
            ClientData.setPanoramaStage("empty");
            context.player().teleport(emptyWorld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null, 0, 0);
        }));

        ServerPlayNetworking.registerGlobalReceiver(EventPacket.ID, (payload, context) -> context.server().execute(() -> {
           if (context.server().isHost(context.player().getGameProfile())) {
               if (payload.event().equals("null")) EventManager.triggerRandomEvent(context.player(), null);
               else EventManager.triggerRandomEvent(context.player(), EventManager.Events.valueOf(payload.event()));
           }
        }));

        ServerPlayNetworking.registerGlobalReceiver(ChatEventPacket.ID, (payload, context) -> context.server().execute(() -> {
            context.server().execute(() -> context.player().dropItem(ModItems.FREEDOM_MUSIC_DISC));
        }));

        ServerPlayNetworking.registerGlobalReceiver(MemoryScreenPacket.ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayNetworking.send(context.player(), new MemoryScreenPacket(MemoryItem.getMemoryBitMask(context.server())));
        }));

        ServerPlayNetworking.registerGlobalReceiver(SleepEventPacket.ID, (payload, context) -> context.server().execute(() -> {
            EventManager.runSleepEvent(context.server(), payload.stage());
        }));

        ServerPlayNetworking.registerGlobalReceiver(KickScreenPacket.ID, (payload, context) -> context.server().execute(() -> {
            Vec3d playerPos = new Vec3d(
                context.player().getPos().x,
                context.player().getPos().y,
                context.player().getPos().z
            );
            playerPos = EventManager.moveVectorFromBase(context.player(), playerPos);
            Vec3d pos1 = new Vec3d(
                playerPos.x - 8,
                playerPos.y - 8,
                playerPos.z - 8
            );
            Vec3d pos2 = new Vec3d(
                playerPos.x + 7,
                playerPos.y + 7,
                playerPos.z + 7
            );
            Random random = new Random();
            for (int y = (int) pos1.getY(); y <= (int) pos2.getY(); y++) {
                for (int x = (int) pos1.getX(); x <= (int) pos2.getX(); x++) {
                    for (int z = (int) pos1.getZ(); z <= (int) pos2.getZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!context.player().getWorld().getBlockState(pos).isAir() && random.nextBoolean()) {
                            context.player().getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
        }));
    }

}

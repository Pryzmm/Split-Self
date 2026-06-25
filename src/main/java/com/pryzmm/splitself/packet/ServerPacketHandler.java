package com.pryzmm.splitself.packet;

import com.pryzmm.splitself.block.functions.EmptyTeleportBlockFunc;
import com.pryzmm.splitself.data.PersistentData;
import com.pryzmm.splitself.packet.packets.BrokenEffectPacket;
import com.pryzmm.splitself.packet.packets.EndBrokenEffectPacket;
import com.pryzmm.splitself.world.DeadCoralChunkGenerator;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ServerPacketHandler {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(BrokenEffectPacket.ID, BrokenEffectPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(EndBrokenEffectPacket.ID, EndBrokenEffectPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BrokenEffectPacket.ID, (payload, context) -> context.server().execute(() -> {
            for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, new BrokenEffectPacket());
            }
        }));

        ServerPlayNetworking.registerGlobalReceiver(EndBrokenEffectPacket.ID, (payload, context) -> context.server().execute(() -> {
            ServerWorld emptyWorld = context.server().getWorld(DimensionRegistry.EMPTINESS_DIMENSION_KEY);
            BlockPos pos = DeadCoralChunkGenerator.findGroundPos(0, 0);
            EmptyTeleportBlockFunc.updateLastLocation(context.player());
            PersistentData.setPanoramaStage("empty");
            context.player().teleport(emptyWorld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null, 0, 0);
        }));
    }

}

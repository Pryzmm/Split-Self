package com.pryzmm.splitself.block.functions;

import com.pryzmm.splitself.packet.packets.TransitionPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransitionBlockFunc {

    public static final List<UUID> playersTransitioning = new ArrayList<>();

    public static void transition(ServerPlayerEntity player) {
        if (!playersTransitioning.contains(player.getUuid())) {
            playersTransitioning.add(player.getUuid());
            ServerPlayNetworking.send(player, new TransitionPacket());
        }
    }

}

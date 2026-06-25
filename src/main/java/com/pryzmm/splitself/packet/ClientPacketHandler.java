package com.pryzmm.splitself.packet;

import com.pryzmm.splitself.block.BrainBlock;
import com.pryzmm.splitself.packet.packets.BrokenEffectPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ClientPacketHandler {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(BrokenEffectPacket.ID, BrokenEffectPacket.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(BrokenEffectPacket.ID, (packet, context) -> context.client().execute(() -> {
            if (!BrainBlock.brokenEffectActive) {
                BrainBlock.brokenEffectActive = true;
                BrainBlock.playBrokenEffect(context.player(), context.client());
            }
        }));
    }

}
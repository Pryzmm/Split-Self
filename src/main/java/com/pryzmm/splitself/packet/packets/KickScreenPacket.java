package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record KickScreenPacket() implements CustomPayload {
    public static final Id<KickScreenPacket> ID = new Id<>(Identifier.of("splitself", "kick_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, KickScreenPacket> CODEC = PacketCodec.unit(new KickScreenPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EventPacket(String event) implements CustomPayload {
    public static final Id<EventPacket> ID = new Id<>(Identifier.of("splitself", "event_packet"));

    public static final PacketCodec<RegistryByteBuf, EventPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, EventPacket::event,
        EventPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SleepEventPacket(Integer stage) implements CustomPayload {
    public static final Id<SleepEventPacket> ID = new Id<>(Identifier.of("splitself", "sleep_event_packet"));

    public static final PacketCodec<RegistryByteBuf, SleepEventPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, SleepEventPacket::stage,
        SleepEventPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
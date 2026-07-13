package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GlitchEventPacket() implements CustomPayload {
    public static final Id<GlitchEventPacket> ID = new Id<>(Identifier.of("splitself", "glitch_event_packet"));

    public static final PacketCodec<RegistryByteBuf, GlitchEventPacket> CODEC = PacketCodec.unit(new GlitchEventPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
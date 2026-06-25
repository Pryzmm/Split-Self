package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EndBrokenEffectPacket() implements CustomPayload {
    public static final Id<EndBrokenEffectPacket> ID = new Id<>(Identifier.of("splitself", "broken_effect_packet_end"));

    public static final PacketCodec<RegistryByteBuf, EndBrokenEffectPacket> CODEC = PacketCodec.unit(new EndBrokenEffectPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
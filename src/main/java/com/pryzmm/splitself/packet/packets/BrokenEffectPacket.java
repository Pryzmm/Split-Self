package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BrokenEffectPacket() implements CustomPayload {
    public static final CustomPayload.Id<BrokenEffectPacket> ID = new CustomPayload.Id<>(Identifier.of("splitself", "broken_effect_packet"));

    public static final PacketCodec<RegistryByteBuf, BrokenEffectPacket> CODEC = PacketCodec.unit(new BrokenEffectPacket());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
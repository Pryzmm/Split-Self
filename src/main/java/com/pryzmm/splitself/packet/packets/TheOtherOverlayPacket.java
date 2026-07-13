package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TheOtherOverlayPacket() implements CustomPayload {
    public static final Id<TheOtherOverlayPacket> ID = new Id<>(Identifier.of("splitself", "the_other_overlay_packet"));

    public static final PacketCodec<RegistryByteBuf, TheOtherOverlayPacket> CODEC = PacketCodec.unit(new TheOtherOverlayPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
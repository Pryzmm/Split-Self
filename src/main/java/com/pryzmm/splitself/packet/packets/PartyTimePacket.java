package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PartyTimePacket(String partyType) implements CustomPayload {
    public static final Id<PartyTimePacket> ID = new Id<>(Identifier.of("splitself", "party_time_packet"));

    public static final PacketCodec<RegistryByteBuf, PartyTimePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, PartyTimePacket::partyType,
        PartyTimePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
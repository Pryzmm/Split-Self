package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MemoryScreenPacket(Integer memoryBitMask) implements CustomPayload {
    public static final Id<MemoryScreenPacket> ID = new Id<>(Identifier.of("splitself", "memory_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, MemoryScreenPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, MemoryScreenPacket::memoryBitMask,
        MemoryScreenPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
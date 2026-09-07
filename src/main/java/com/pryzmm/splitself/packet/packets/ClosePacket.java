package com.pryzmm.splitself.packet.packets;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClosePacket() implements CustomPayload {
    public static final Id<ClosePacket> ID = new Id<>(Identifier.of(SplitSelf.MOD_ID, "close_packet"));

    public static final PacketCodec<RegistryByteBuf, ClosePacket> CODEC = PacketCodec.unit(new ClosePacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
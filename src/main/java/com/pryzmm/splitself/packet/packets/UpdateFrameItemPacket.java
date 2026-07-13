package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateFrameItemPacket() implements CustomPayload {
    public static final Id<UpdateFrameItemPacket> ID = new Id<>(Identifier.of("splitself", "update_frame_item_packet"));

    public static final PacketCodec<RegistryByteBuf, UpdateFrameItemPacket> CODEC = PacketCodec.unit(new UpdateFrameItemPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
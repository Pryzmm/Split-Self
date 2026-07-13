package com.pryzmm.splitself.packet.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChatEventPacket(String message, Boolean talkingToTheForgotten) implements CustomPayload {
    public static final Id<ChatEventPacket> ID = new Id<>(Identifier.of("splitself", "chat_event_packet"));

    public static final PacketCodec<RegistryByteBuf, ChatEventPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ChatEventPacket::message,
        PacketCodecs.BOOL, ChatEventPacket::talkingToTheForgotten,
        ChatEventPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
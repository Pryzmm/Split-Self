package com.pryzmm.splitself.packet.packets;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FinalePacket(Boolean isIntro, Boolean deleteWorld) implements CustomPayload {
    public static final Id<FinalePacket> ID = new Id<>(Identifier.of(SplitSelf.MOD_ID, "finale_packet"));

    public static final PacketCodec<RegistryByteBuf, FinalePacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL, FinalePacket::isIntro,
        PacketCodecs.BOOL, FinalePacket::deleteWorld,
        FinalePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
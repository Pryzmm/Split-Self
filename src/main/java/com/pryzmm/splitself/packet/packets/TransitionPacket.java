package com.pryzmm.splitself.packet.packets;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TransitionPacket() implements CustomPayload {
    public static final Id<TransitionPacket> ID = new Id<>(Identifier.of(SplitSelf.MOD_ID, "transition_packet"));

    public static final PacketCodec<RegistryByteBuf, TransitionPacket> CODEC = PacketCodec.unit(new TransitionPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
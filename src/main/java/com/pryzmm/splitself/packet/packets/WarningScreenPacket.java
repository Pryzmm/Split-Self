package com.pryzmm.splitself.packet.packets;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WarningScreenPacket() implements CustomPayload {
    public static final Id<WarningScreenPacket> ID = new Id<>(Identifier.of(SplitSelf.MOD_ID, "warning_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, WarningScreenPacket> CODEC = PacketCodec.unit(new WarningScreenPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
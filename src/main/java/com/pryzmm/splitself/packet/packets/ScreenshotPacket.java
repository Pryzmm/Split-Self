package com.pryzmm.splitself.packet.packets;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ScreenshotPacket() implements CustomPayload {
    public static final Id<ScreenshotPacket> ID = new Id<>(Identifier.of(SplitSelf.MOD_ID, "screenshot_packet"));

    public static final PacketCodec<RegistryByteBuf, ScreenshotPacket> CODEC = PacketCodec.unit(new ScreenshotPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
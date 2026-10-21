package com.pryzmm.splitself.forge;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

final class MemoryNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SplitSelfForge.MOD_ID, "memory"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private MemoryNetwork() {}

    static void register() {
        CHANNEL.registerMessage(0, OpenBook.class, OpenBook::encode, OpenBook::decode,
                OpenBook::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(1, OpenWarning.class, OpenWarning::encode, OpenWarning::decode,
                OpenWarning::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    static void openBook(ServerPlayer player, int bitMask) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenBook(bitMask));
    }

    static void openWarning(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenWarning());
    }

    private record OpenWarning() {
        static void encode(OpenWarning message, FriendlyByteBuf buf) {}
        static OpenWarning decode(FriendlyByteBuf buf) { return new OpenWarning(); }
        static void handle(OpenWarning message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> ClientWarningScreen::open));
            context.get().setPacketHandled(true);
        }
    }

    private record OpenBook(int bitMask) {
        static void encode(OpenBook message, FriendlyByteBuf buf) { buf.writeVarInt(message.bitMask); }
        static OpenBook decode(FriendlyByteBuf buf) { return new OpenBook(buf.readVarInt()); }
        static void handle(OpenBook message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientMemoryScreens.open(message.bitMask)));
            context.get().setPacketHandled(true);
        }
    }
}

package com.pryzmm.splitself.forge;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

/** Separate story protocol; the existing memory packet IDs remain unchanged. */
final class StoryNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("splitself", "story"), () -> "6", "6"::equals, "6"::equals);
    private StoryNetwork() {}
    static void register() {
        CHANNEL.registerMessage(13,Speech.class,Speech::encode,Speech::decode,Speech::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(11, MemoryOffer.class, MemoryOffer::encode, MemoryOffer::decode, MemoryOffer::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(12, MemoryComplete.class, MemoryComplete::encode, MemoryComplete::decode, MemoryComplete::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(10, SleepStage.class, SleepStage::encode, SleepStage::decode,
                SleepStage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(0, Broken.class, Broken::encode, Broken::decode,
                Broken::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(1, ClientEvent.class, ClientEvent::encode, ClientEvent::decode,
                ClientEvent::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(2, Transition.class, Transition::encode, Transition::decode,
                Transition::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(3, Close.class, Close::encode, Close::decode,
                Close::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(4, FinishTransition.class, FinishTransition::encode, FinishTransition::decode,
                FinishTransition::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(5, Activity.class, Activity::encode, Activity::decode,
                Activity::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(6, Abort.class, Abort::encode, Abort::decode,
                Abort::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(8, Kick.class, Kick::encode, Kick::decode, Kick::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(9, KickAnswer.class, KickAnswer::encode, KickAnswer::decode, KickAnswer::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(7, Result.class, Result::encode, Result::decode,
                Result::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    static void speech(String text){CHANNEL.sendToServer(new Speech(text));}
    private record Speech(String text){
        static void encode(Speech m,FriendlyByteBuf b){b.writeUtf(m.text,512);}
        static Speech decode(FriendlyByteBuf b){return new Speech(b.readUtf(512));}
        static void handle(Speech m,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{if(c.getSender()!=null)ServerSpeech.receive(c.getSender(),m.text);});c.setPacketHandled(true);}
    }
    static void memoryOffer(ServerPlayer player,java.util.UUID token,int stage) { CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new MemoryOffer(token,stage)); }
    static void completeMemory(java.util.UUID token) { CHANNEL.sendToServer(new MemoryComplete(token,true)); }
    static void cancelMemory(java.util.UUID token) { CHANNEL.sendToServer(new MemoryComplete(token,false)); }
    private record MemoryOffer(java.util.UUID token,int stage) {
        static void encode(MemoryOffer m,FriendlyByteBuf b){b.writeUUID(m.token);b.writeVarInt(m.stage);}
        static MemoryOffer decode(FriendlyByteBuf b){return new MemoryOffer(b.readUUID(),b.readVarInt());}
        static void handle(MemoryOffer m,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->RemainingClientEvents.memory(m.token,m.stage)));c.setPacketHandled(true);}
    }
    private record MemoryComplete(java.util.UUID token,boolean success) {
        static void encode(MemoryComplete m,FriendlyByteBuf b){b.writeUUID(m.token);b.writeBoolean(m.success);}
        static MemoryComplete decode(FriendlyByteBuf b){return new MemoryComplete(b.readUUID(),b.readBoolean());}
        static void handle(MemoryComplete m,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{if(c.getSender()!=null)RemainingWorldEvents.completeMemory(c.getSender(),m.token,m.success);});c.setPacketHandled(true);}
    }
    static void kick(ServerPlayer player, java.util.UUID token) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Kick(token));
    }
    static void answerKick(java.util.UUID token) { CHANNEL.sendToServer(new KickAnswer(token)); }
    private record Kick(java.util.UUID token) {
        static void encode(Kick m, FriendlyByteBuf b) { b.writeUUID(m.token); }
        static Kick decode(FriendlyByteBuf b) { return new Kick(b.readUUID()); }
        static void handle(Kick m, Supplier<NetworkEvent.Context> supplier) {
            var c = supplier.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.minecraft.client.Minecraft.getInstance().setScreen(new KickScreen(m.token))));
            c.setPacketHandled(true);
        }
    }
    private record KickAnswer(java.util.UUID token) {
        static void encode(KickAnswer m, FriendlyByteBuf b) { b.writeUUID(m.token); }
        static KickAnswer decode(FriendlyByteBuf b) { return new KickAnswer(b.readUUID()); }
        static void handle(KickAnswer m, Supplier<NetworkEvent.Context> supplier) {
            var c = supplier.get();
            c.enqueueWork(() -> { if (c.getSender() != null) KickEvent.answer(c.getSender(), m.token); });
            c.setPacketHandled(true);
        }
    }
    static void broken(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Broken());
    }
    static void clientEvent(ServerPlayer player, String name) {
        if (name.equals("FACE")) sleepStage(player, MemoryData.get(player.server.overworld()).sleepStage());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientEvent(name));
    }
    static void sleepStage(ServerPlayer player, int stage) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SleepStage(stage));
    }
    private record SleepStage(int stage) {
        static void encode(SleepStage m, FriendlyByteBuf b) { b.writeVarInt(m.stage); }
        static SleepStage decode(FriendlyByteBuf b) { return new SleepStage(b.readVarInt()); }
        static void handle(SleepStage m, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientFaceEffects.stage(m.stage)));
            context.setPacketHandled(true);
        }
    }
    static void transition(ServerPlayer player, java.util.UUID id) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Transition(id));
    }
    static void close(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Close());
    }
    static void requestTransitionFinish(java.util.UUID id) { CHANNEL.sendToServer(new FinishTransition(id)); }
    static void activity(java.util.UUID id) { CHANNEL.sendToServer(new Activity(id)); }
    static void abort(java.util.UUID id) { CHANNEL.sendToServer(new Abort(id)); }
    static void transitionResult(ServerPlayer player, java.util.UUID id) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Result(id));
    }
    private record FinishTransition(java.util.UUID id) {
        static void encode(FinishTransition message, FriendlyByteBuf buf) { buf.writeUUID(message.id); }
        static FinishTransition decode(FriendlyByteBuf buf) { return new FinishTransition(buf.readUUID()); }
        static void handle(FinishTransition message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender != null) TriggerTransitionBlock.finish(sender, message.id);
            });
            context.setPacketHandled(true);
        }
    }
    private record Activity(java.util.UUID id) {
        static void encode(Activity message, FriendlyByteBuf buf) { buf.writeUUID(message.id); }
        static Activity decode(FriendlyByteBuf buf) { return new Activity(buf.readUUID()); }
        static void handle(Activity message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender != null) TriggerTransitionBlock.heartbeat(sender, message.id);
            });
            context.setPacketHandled(true);
        }
    }
    private record Abort(java.util.UUID id) {
        static void encode(Abort message, FriendlyByteBuf buf) { buf.writeUUID(message.id); }
        static Abort decode(FriendlyByteBuf buf) { return new Abort(buf.readUUID()); }
        static void handle(Abort message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender != null) TriggerTransitionBlock.finish(sender, message.id);
            });
            context.setPacketHandled(true);
        }
    }
    private record Result(java.util.UUID id) {
        static void encode(Result message, FriendlyByteBuf buf) { buf.writeUUID(message.id); }
        static Result decode(FriendlyByteBuf buf) { return new Result(buf.readUUID()); }
        static void handle(Result message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientTransitionEffects.complete(message.id)));
            context.setPacketHandled(true);
        }
    }
    private record Close() {
        static void encode(Close message, FriendlyByteBuf buf) {}
        static Close decode(FriendlyByteBuf buf) { return new Close(); }
        static void handle(Close message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> ClientFinaleEffects::startClose));
            context.setPacketHandled(true);
        }
    }
    private record Transition(java.util.UUID id) {
        static void encode(Transition message, FriendlyByteBuf buf) { buf.writeUUID(message.id); }
        static Transition decode(FriendlyByteBuf buf) { return new Transition(buf.readUUID()); }
        static void handle(Transition message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientTransitionEffects.start(message.id)));
            context.setPacketHandled(true);
        }
    }
    private record ClientEvent(String name) {
        static void encode(ClientEvent message, FriendlyByteBuf buf) { buf.writeUtf(message.name, 32); }
        static ClientEvent decode(FriendlyByteBuf buf) { return new ClientEvent(buf.readUtf(32)); }
        static void handle(ClientEvent message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientEventEffects.start(message.name)));
            context.setPacketHandled(true);
        }
    }
    private record Broken() {
        static void encode(Broken message, FriendlyByteBuf buf) {}
        static Broken decode(FriendlyByteBuf buf) { return new Broken(); }
        static void handle(Broken message, Supplier<NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> ClientStoryEffects::startBroken));
            context.setPacketHandled(true);
        }
    }
}

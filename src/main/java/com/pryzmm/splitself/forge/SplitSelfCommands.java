package com.pryzmm.splitself.forge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/** Source commands whose backing data exists in the Forge port. */
final class SplitSelfCommands {
    private SplitSelfCommands() {}

    static void register(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    static void registerCommands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("splitself")
                .executes(context -> {
                    context.getSource().sendSuccess(
                            () -> Component.translatable("command.splitself.empty_command"), false);
                    return 1;
                })
                .then(Commands.literal("information")
                        .executes(context -> {
                            var player = context.getSource().getPlayer();
                            if (player != null) MemoryNetwork.openWarning(player);
                            return 1;
                        }))
                .then(Commands.literal("runEvent")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("event", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                        .suggest(java.util.stream.Stream.concat(PortedEvents.supported().stream(),java.util.stream.Stream.of("random")), builder))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    String name = StringArgumentType.getString(context, "event");
                                    if (player == null || !EventRuntime.force(player, name)) {
                                        context.getSource().sendFailure(
                                                Component.translatable("command.splitself.invalid_value"));
                                        return 0;
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("runTests")
                        .requires(source -> source.hasPermission(2))
                .then(Commands.literal("testTnt")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> WorldManifestations.testTnt(context.getSource().getPlayerOrException()) ? 1 : 0))
                .then(Commands.literal("testEvent")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("event", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider
                                        .suggest(java.util.stream.Stream.concat(SimpleEventRules.CLIENT_EVENTS.stream(),
                                                java.util.stream.Stream.concat(BatchEventRules.all().stream(),java.util.stream.Stream.concat(RemainingEventRules.all().stream(),java.util.stream.Stream.of("KICK")))), builder))
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "event").toUpperCase(java.util.Locale.ROOT);
                                    if (!SimpleEventRules.CLIENT_EVENTS.contains(name) && !BatchEventRules.all().contains(name) && !RemainingEventRules.all().contains(name) && !name.equals("KICK")) {
                                        context.getSource().sendFailure(Component.translatable("command.splitself.invalid_value"));
                                        return 0;
                                    }
                                    if (RemainingEventRules.WORLD.contains(name)) return RemainingWorldEvents.run(context.getSource().getPlayerOrException(),name,true) ? 1 : 0;
                                    if (name.equals("MEMORY")) { RemainingWorldEvents.memory(context.getSource().getPlayerOrException()); return 1; }
                                    if (name.equals("KICK")) KickEvent.start(context.getSource().getPlayerOrException());
                                    else if (BatchEventRules.WORLD.contains(name)) return BatchWorldEvents.run(context.getSource().getPlayerOrException(),name) ? 1 : 0;
                                    else StoryNetwork.clientEvent(context.getSource().getPlayerOrException(), name);
                                    return 1;
                                })))
                .then(Commands.literal("testForgotten")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> EncounterSpawns.forgottenRandom(context.getSource().getPlayerOrException()) ? 1 : 0))
                .then(Commands.literal("testBroken")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> StoryScenes.startBroken(context.getSource().getServer()) ? 1 : 0))
                .then(Commands.literal("testTransition")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> TriggerTransitionBlock.start(
                                context.getSource().getPlayerOrException()) ? 1 : 0))
                .then(Commands.literal("testSleep")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("stage", IntegerArgumentType.integer(0, 4))
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrException();
                                    return SleepSystem.debugRun(player, IntegerArgumentType.getInteger(context, "stage")) ? 1 : 0;
                                })))
                .then(Commands.literal("testFinale").requires(source->source.hasPermission(2)).then(Commands.argument("mode",StringArgumentType.word()).suggests((c,bb)->net.minecraft.commands.SharedSuggestionProvider.suggest(java.util.List.of("INTRO","CANCEL","VISUAL","STOP"),bb)).executes(c->{String mode=StringArgumentType.getString(c,"mode").toUpperCase(java.util.Locale.ROOT);if(!java.util.Set.of("INTRO","CANCEL","VISUAL","STOP").contains(mode))return 0;StoryNetwork.clientEvent(c.getSource().getPlayerOrException(),"FINALE_"+mode);return 1;})))
                .then(Commands.literal("testSpeech").requires(source->source.hasPermission(2)).executes(c->{c.getSource().sendSuccess(()->Component.literal("/splitself runTests testSpeech <text> — can you hear me / ты слышишь меня / привет; без микрофона"),false);return 1;}).then(Commands.argument("text",StringArgumentType.greedyString()).executes(c->{return ServerSpeech.test(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"text"));})))
                )
                .then(Commands.literal("debug")
                        .then(Commands.literal("debugFullscreen").requires(source->source.hasPermission(2)).executes(c->{StoryNetwork.clientEvent(c.getSource().getPlayerOrException(),"DEBUG_FULLSCREEN");return 1;}))
                        .then(Commands.literal("debugSendVec3d").requires(source->source.hasPermission(2)).executes(c->{var p=c.getSource().getPlayerOrException();var pos=WorldManifestationRules.moveVectorFromBase(new net.minecraft.world.phys.Vec3(p.getX(),319,p.getZ()),p.getRespawnDimension().equals(p.level().dimension())?p.getRespawnPosition():null,EventCatalog.baseSafeRadius());c.getSource().sendSuccess(()->Component.literal(pos.toString()),false);return 1;}))
                        .then(Commands.literal("debugInvert").requires(source->source.hasPermission(2)).executes(c->{StoryNetwork.clientEvent(c.getSource().getPlayerOrException(),"DEBUG_INVERT");return 1;}))
                        .then(Commands.literal("debugVideo").requires(source->source.hasPermission(2)).then(Commands.argument("video",StringArgumentType.word()).executes(c->{if(!java.util.Set.of("transition","transition.mp4").contains(StringArgumentType.getString(c,"video")))return 0;StoryNetwork.clientEvent(c.getSource().getPlayerOrException(),"DEBUG_VIDEO");return 1;})))
                        .then(Commands.literal("debugStripMine").requires(source->source.hasPermission(2)).executes(c->StripMineEncounters.spawn(c.getSource().getPlayerOrException())?1:0))
                        .then(Commands.literal("debugEmpty")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    var target = context.getSource().getServer().getLevel(SplitSelfWorldEvents.EMPTY);
                                    if (target == null) {
                                        context.getSource().sendFailure(Component.literal("Empty dimension is unavailable"));
                                        return 0;
                                    }
                                    var pos = DeadCoralChunkGenerator.findGroundPos(0, 0);
                                    target.getChunkAt(pos);
                                    player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                                    return 1;
                                }))
                        .then(Commands.literal("debugLimbo")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    var target = context.getSource().getServer().getLevel(SplitSelfWorldEvents.LIMBO);
                                    if (target == null) {
                                        context.getSource().sendFailure(Component.literal("Limbo dimension is unavailable"));
                                        return 0;
                                    }
                                    BlockPos pos = new BlockPos(2, 3, 2);
                                    target.getChunkAt(pos);
                                    player.teleportTo(target, 2.5, 3.0, 2.5, -135.0f, 40.0f);
                                    return 1;
                                }))
                        .then(Commands.literal("debugLimboFinale")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    var target = context.getSource().getServer().getLevel(SplitSelfWorldEvents.LIMBO);
                                    if (target == null) {
                                        context.getSource().sendFailure(Component.literal("Limbo dimension is unavailable"));
                                        return 0;
                                    }
                                    BlockPos pos = new BlockPos(4007, 3, 3);
                                    target.getChunkAt(pos);
                                    player.teleportTo(target, 4007.5, 3.0, 3.5, -90.0f, 0.0f);
                                    return 1;
                                }))
                        .then(Commands.literal("debugHallway")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    var target = context.getSource().getServer().getLevel(SplitSelfWorldEvents.HALLWAY);
                                    if (target == null) {
                                        context.getSource().sendFailure(Component.literal("Hallway dimension is unavailable"));
                                        return 0;
                                    }
                                    BlockPos pos = new BlockPos(0, 101, 1);
                                    target.getChunkAt(pos);
                                    player.teleportTo(target, 0.5, 101, 1.5, 0.0f, 0.0f);
                                    return 1;
                                }))
                        .then(Commands.literal("debugLiftChunk")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    if (player.serverLevel() != context.getSource().getServer().overworld()) {
                                        context.getSource().sendFailure(Component.literal(
                                                "Run this destructive test from the Overworld, in a disposable test world"));
                                        return 0;
                                    }
                                    FinaleChunkEffect.liftOneChunk(context.getSource().getServer());
                                    context.getSource().sendSuccess(() -> Component.literal(
                                            "Moved one terrain section; this change is permanent in this world"), false);
                                    return 1;
                                }))
                        .then(Commands.literal("debugMemories")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    MemoryData data = MemoryData.get(context.getSource().getServer().overworld());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal(data.unlockedMemories().toString()), false);
                                    return 1;
                                })
                                .then(Commands.argument("memory", StringArgumentType.string())
                                        .executes(context -> {
                                            String id = StringArgumentType.getString(context, "memory");
                                            MemoryData.get(context.getSource().getServer().overworld()).unlock(id);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Updated Memory List"), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("debugSleepStage")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    int stage = MemoryData.get(context.getSource().getServer().overworld()).sleepStage();
                                    context.getSource().sendSuccess(() -> Component.literal(Integer.toString(stage)), false);
                                    return 1;
                                })
                                .then(Commands.argument("stage", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            int stage = IntegerArgumentType.getInteger(context, "stage");
                                            MemoryData.get(context.getSource().getServer().overworld()).setSleepStage(stage);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(Integer.toString(stage)), false);
                                            return 1;
                                        })))));
    }
}

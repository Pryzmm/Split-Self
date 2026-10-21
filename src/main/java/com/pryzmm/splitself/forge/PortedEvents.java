package com.pryzmm.splitself.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Audited handlers; external requests remain guarded independently on each client. */
final class PortedEvents {
    private PortedEvents() {}

    static java.util.Set<String> supported() {
        var names = new java.util.HashSet<>(java.util.Set.of("FRAME", "BRAIN", "BOOK", "HOUSE", "PILLAR", "IRONTRAP", "STATUE", "HALLWAY", "MINE", "FORGOTTEN", "BILLY", "BLU", "WHISPER", "FOV", "BRIGHTNESS", "INVERT", "STATIC", "RECURSIVE", "INVENTORYOVERLAY", "DISCONNECT", "SPAWNTHEOTHER"));
        names.addAll(ExternalConsent.EVENTS);
        names.addAll(java.util.Set.of("SIGN", "ITEM", "DOOR", "CORAL", "LAVA", "TNT", "KICK"));
        names.addAll(SimpleEventRules.CLIENT_EVENTS);
        names.addAll(BatchEventRules.all());
        names.addAll(RemainingEventRules.all());
        return java.util.Set.copyOf(names);
    }

    static boolean run(ServerPlayer player, String name) {
        name = name.toUpperCase(java.util.Locale.ROOT);
        if (!supported().contains(name)) return false;
        EncounterSpawns.remember(player);
        if (RemainingEventRules.WORLD.contains(name)) return RemainingWorldEvents.run(player,name,false);
        if (name.equals("MEMORY")) { for(var listener:player.server.getPlayerList().getPlayers()) RemainingWorldEvents.memory(listener); return true; }
        if (BatchEventRules.WORLD.contains(name)) return BatchWorldEvents.run(player,name);
        if (name.equals("KICK")) {
            var players = player.server.getPlayerList().getPlayers();
            if (players.isEmpty()) return false;
            KickEvent.start(players.get((int) (Math.random() * players.size())));
            return true;
        }
        if (java.util.Set.of("SIGN", "ITEM", "DOOR", "CORAL", "LAVA", "TNT").contains(name)) return WorldManifestations.run(player, name);
        if (ExternalConsent.EVENTS.contains(name)) {
            for (var listener : player.getServer().getPlayerList().getPlayers()) StoryNetwork.clientEvent(listener, name);
            return true;
        }
        if (BatchEventRules.CLIENT.contains(name) || SimpleEventRules.CLIENT_EVENTS.contains(name) || java.util.Set.of("FOV", "BRIGHTNESS", "INVERT", "STATIC", "RECURSIVE", "INVENTORYOVERLAY", "DISCONNECT").contains(name)) {
            for (var listener : player.getServer().getPlayerList().getPlayers()) StoryNetwork.clientEvent(listener, name);
            return true;
        }
        if (name.equals("SPAWNTHEOTHER")) return EncounterSpawns.theOther(player);
        if (java.util.Set.of("HOUSE", "PILLAR", "IRONTRAP", "STATUE", "HALLWAY", "MINE").contains(name)) {
            return WorldEventStructures.run(player, name);
        }
        if (name.equals("FORGOTTEN")) return EncounterSpawns.forgottenNearHouse(player);
        if (name.equals("WHISPER")) {
            for (var listener : player.getServer().getPlayerList().getPlayers()) {
                listener.playNotifySound(net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                        new net.minecraft.resources.ResourceLocation("splitself", "whisper")), SoundSource.MASTER, 40, 1);
            }
            return true;
        }
        if (name.equals("BILLY")) {
            var server = player.getServer();
            server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "events.splitself.billy.joined").withStyle(net.minecraft.ChatFormatting.YELLOW), false);
            TickScheduler.schedule(60, () -> server.getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable("events.splitself.billy.message"), false));
            TickScheduler.schedule(90, () -> server.getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable("events.splitself.billy.left")
                            .withStyle(net.minecraft.ChatFormatting.YELLOW), false));
            return true;
        }
        if (name.equals("BLU")) {
            var wolf = net.minecraft.world.entity.EntityType.WOLF.create(player.serverLevel());
            if (wolf == null) return false;
            wolf.tame(player);
            wolf.setCollarColor(net.minecraft.world.item.DyeColor.CYAN);
            wolf.setCustomName(net.minecraft.network.chat.Component.literal("Blu"));
            wolf.moveTo(player.getX(), player.getY(), player.getZ(), 0, 0);
            if (!player.serverLevel().addFreshEntity(wolf)) return false;
            player.drop(new ItemStack(SplitSelfForge.MEMORY_BLU.get()), false);
            return true;
        }
        Item item = switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "FRAME" -> SplitSelfForge.IMAGE_FRAME.get().asItem();
            case "BRAIN" -> SplitSelfForge.BRAIN.get().asItem();
            case "BOOK" -> SplitSelfForge.MEMORY_BOOK.get();
            default -> null;
        };
        if (item == null) return false;
        player.drop(new ItemStack(item), false);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP,
                SoundSource.MASTER, 1.0f, 1.0f);
        return true;
    }
}

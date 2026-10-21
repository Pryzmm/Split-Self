package com.pryzmm.splitself.forge;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Server-authoritative world manifestations. No external-system actions. */
final class WorldManifestations {
    private WorldManifestations() {}

    static boolean run(ServerPlayer initiator, String event) {
        if (event.equals("SIGN")) return sign(initiator);
        var players = initiator.server.getPlayerList().getPlayers();
        if (event.equals("TNT")) return WorldManifestationRules.tnt(players, WorldManifestations::tntTarget,
                EventCatalog.baseSafeRadius(), Math::random);
        if (event.equals("LAVA")) return WorldManifestationRules.lava(players, WorldManifestations::lavaTarget,
                EventCatalog.baseSafeRadius(), Math::random);
        if (event.equals("CORAL")) return WorldManifestationRules.coral(players, WorldManifestations::coralTarget,
                EventCatalog.baseSafeRadius(), Math::random);
        if (players.isEmpty()) return false;
        var selected = players.get(initiator.getRandom().nextInt(players.size()));
        return switch (event) {
            case "ITEM" -> removeItem(selected);
            case "DOOR" -> doors(selected);
            default -> false;
        };
    }

    static boolean testTnt(ServerPlayer player) {
        return WorldManifestationRules.tnt(java.util.List.of(player), WorldManifestations::tntTarget,
                EventCatalog.baseSafeRadius(), Math::random);
    }

    private static WorldManifestationRules.TntTarget tntTarget(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        return new WorldManifestationRules.TntTarget() {
            public net.minecraft.world.phys.Vec3 position() { return player.position(); }
            public BlockPos respawnInThisDimension() {
                return player.getRespawnDimension().equals(world.dimension()) ? player.getRespawnPosition() : null;
            }
            public void primedSound() {
                // Adaptation: sound follows the selected player's dimension, like the actual TNT.
                world.playSound(null, player.blockPosition(), SoundEvents.TNT_PRIMED, SoundSource.MASTER, 1, 1);
            }
            public boolean available(net.minecraft.world.phys.Vec3 pos) {
                BlockPos block = BlockPos.containing(pos);
                return !world.isOutsideBuildHeight(block) && world.hasChunkAt(block);
            }
            public void spawn(net.minecraft.world.phys.Vec3 pos, int fuse) {
                var tnt = net.minecraft.world.entity.EntityType.TNT.create(world);
                if (tnt == null) return;
                // EntityType.create, not the throwing constructor: preserve zero initial velocity/no owner.
                tnt.setPos(pos.x, pos.y, pos.z);
                tnt.setFuse(fuse);
                world.addFreshEntity(tnt);
            }
        };
    }

    private static WorldManifestationRules.LavaTarget lavaTarget(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        return new WorldManifestationRules.LavaTarget() {
            public net.minecraft.world.phys.Vec3 position() { return player.position(); }
            public BlockPos respawnInThisDimension() {
                return player.getRespawnDimension().equals(world.dimension()) ? player.getRespawnPosition() : null;
            }
            public boolean available(BlockPos pos) {
                return !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos);
            }
            public void placeLava(BlockPos pos) {
                world.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
            }
        };
    }

    private static WorldManifestationRules.CoralTarget coralTarget(ServerPlayer player) {
        // As with DOOR, use the selected player's dimension rather than the initiator's.
        ServerLevel world = player.serverLevel();
        return new WorldManifestationRules.CoralTarget() {
            public net.minecraft.world.phys.Vec3 position() { return player.position(); }
            public BlockPos blockPosition() { return player.blockPosition(); }
            public BlockPos respawnInThisDimension() {
                return player.getRespawnDimension().equals(world.dimension()) ? player.getRespawnPosition() : null;
            }
            public boolean available(BlockPos pos) {
                return !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos);
            }
            public boolean isAir(BlockPos pos) { return world.getBlockState(pos).isAir(); }
            public void replace(BlockPos pos) {
                // Deliberately destructive: containers, block entities and unbreakable blocks are eligible.
                world.setBlock(pos, Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            }
        };
    }

    private static boolean sign(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos pos = player.blockPosition();
        if (!world.setBlock(pos, Blocks.OAK_SIGN.defaultBlockState(), Block.UPDATE_ALL)) return false;
        if (!(world.getBlockEntity(pos) instanceof SignBlockEntity sign)) return false;
        var text = sign.getFrontText();
        String[] lines = WorldManifestationRules.signLines(world.random::nextInt);
        for (int i = 0; i < lines.length; i++) text = text.setMessage(i, Component.translatable(lines[i]));
        sign.setText(text, true);
        sign.setChanged();
        world.sendBlockUpdated(pos, sign.getBlockState(), sign.getBlockState(), Block.UPDATE_ALL);
        return true;
    }

    private static boolean removeItem(ServerPlayer player) {
        var inventory = player.getInventory();
        int slot = WorldManifestationRules.removalSlot(inventory.getContainerSize(),
                i -> !inventory.getItem(i).isEmpty(), player.getRandom()::nextInt);
        if (slot >= 0) {
            inventory.setItem(slot, ItemStack.EMPTY);
            inventory.setChanged();
            player.containerMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) player.inventoryMenu.broadcastChanges();
        }
        // A no-hit roll is still a completed event, as in the original.
        return true;
    }

    private static boolean doors(ServerPlayer player) {
        // Deviation: use the selected player's dimension, not the initiator's world at these coordinates.
        ServerLevel world = player.serverLevel();
        BlockPos center = player.blockPosition();
        var positions = new ArrayList<BlockPos>();
        int radius = WorldManifestationRules.DOOR_RADIUS;
        // Loaded chunks only: never generate/load terrain merely for a transient door effect.
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (world.isOutsideBuildHeight(pos) || !world.hasChunkAt(pos)) continue;
                    var state = world.getBlockState(pos);
                    if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        positions.add(pos);
                    }
                }
            }
        }
        if (positions.isEmpty()) return true;
        // Original: 50 cycles with a 100ms sleep, 1/2 chance per extant pair each cycle.
        // Forge adaptation: immediate first cycle and then two server ticks, never a world-writing thread.
        WorldManifestationRules.doorCycles(() -> {
            for (BlockPos bottom : positions) {
                if (!world.hasChunkAt(bottom)) continue;
                BlockPos top = bottom.above();
                var lower = world.getBlockState(bottom);
                var upper = world.getBlockState(top);
                if (!(lower.getBlock() instanceof DoorBlock) || !(upper.getBlock() instanceof DoorBlock)
                        || !world.random.nextBoolean()) continue;
                boolean open = lower.getValue(DoorBlock.OPEN);
                world.setBlock(bottom, lower.setValue(DoorBlock.OPEN, !open), Block.UPDATE_ALL);
                world.setBlock(top, upper.setValue(DoorBlock.OPEN, !open), Block.UPDATE_ALL);
                world.playSound(null, bottom, open ? SoundEvents.WOODEN_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_OPEN,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }, TickScheduler::schedule);
        return true;
    }
}

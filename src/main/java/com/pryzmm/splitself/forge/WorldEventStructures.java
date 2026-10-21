package com.pryzmm.splitself.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** Source ranges, integer truncation and rotation rules for the authored event builds. */
final class WorldEventStructures {
    private WorldEventStructures() {}

    static boolean run(ServerPlayer player, String name) {
        ServerLevel world = player.serverLevel();
        return switch (name) {
            case "HOUSE" -> place(world, player.blockPosition(), "house", 50, 80, -5, false) != null;
            case "IRONTRAP" -> place(world, player.blockPosition(), "irontrap", 50, 80, -2, false) != null;
            case "STATUE" -> place(world, player.blockPosition(), "statue", 110, 170, -7, false) != null;
            case "HALLWAY" -> place(world, player.blockPosition(), "broken_flower", 20, 40, 10, false) != null;
            case "PILLAR" -> {
                for (int i = 0; i <= 30; i++) place(world, player.blockPosition(), "pillar", 50, 80, 0, false);
                yield place(world, player.blockPosition(), "pillarmemory", 48, 48, 0, false) != null;
            }
            case "MINE" -> {
                BlockPos pos = place(world, player.blockPosition(), "stripmine", 50, 80, -49, true);
                if (pos == null) yield false;
                place(world, pos.offset(2, 0, 0), "stripmine_top", 0, 0, -43, true);
                BlockPos signPos = pos.offset(5, 5, 7);
                if (world.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
                    String date = java.time.LocalDate.now().minusMonths(6).minusDays(17)
                            .format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yy"));
                    sign.setText(sign.getFrontText().setMessage(2, Component.literal("- ")
                            .append(Component.translatable("events.splitself.redacted_name")))
                            .setMessage(3, Component.literal(date)), true);
                    sign.setChanged();
                    world.sendBlockUpdated(signPos, sign.getBlockState(), sign.getBlockState(), 3);
                }
                yield true;
            }
            default -> false;
        };
    }

    private static BlockPos place(ServerLevel world, BlockPos center, String name,
                                  int min, int max, int offsetY, boolean disableRotation) {
        var template = world.getStructureManager().get(new ResourceLocation("splitself", name)).orElse(null);
        if (template == null) return null;
        var random = world.getRandom();
        var positionRandom = new java.util.Random();
        double distance = min + positionRandom.nextDouble() * (max - min);
        double angle = positionRandom.nextDouble() * 2 * Math.PI;
        int x = (int) (center.getX() + 0.5 + Math.cos(angle) * distance);
        int z = (int) (center.getZ() + 0.5 + Math.sin(angle) * distance);
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + offsetY;
        BlockPos pos = new BlockPos(x, y, z);
        var rotation = disableRotation ? Rotation.NONE : Rotation.values()[random.nextInt(4)];
        var settings = new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        if (!template.placeInWorld(world, pos, pos, settings, random, 2)) return null;
        if (name.equals("house")) MemoryData.get(world).setForgottenLocation(
                new MemoryData.ForgottenLocation(world.dimension(), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos), 0, 0));
        return pos;
    }
}

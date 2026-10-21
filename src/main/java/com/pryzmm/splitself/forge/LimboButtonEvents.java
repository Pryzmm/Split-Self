package com.pryzmm.splitself.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Server-side actions for the two authored finale buttons. */
final class LimboButtonEvents {
    private LimboButtonEvents() {}

    static void onPressed(ServerPlayer player, UIButtonEntity button) {
        if (!player.level().dimension().equals(SplitSelfWorldEvents.LIMBO)) return;
        ServerLevel limbo = player.serverLevel();
        MemoryData data = MemoryData.get(limbo);
        if (data.clickedButton()) return;

        data.setClickedButton(true);
        data.setDeleted(button.deletesWorld());
        for(var listener:player.server.getPlayerList().getPlayers())StoryNetwork.clientEvent(listener,button.deletesWorld()?"FINALE_DELETE":"FINALE_CANCEL");
        AABB finaleArea = new AABB(3999, 0, 0, 4019, 13, 13);
        limbo.getEntitiesOfClass(UIButtonEntity.class, finaleArea).forEach(UIButtonEntity::discard);
        limbo.getEntitiesOfClass(TextDisplay.class, finaleArea).forEach(TextDisplay::discard);

        if (!button.deletesWorld()) {
            // The "Cancel" choice opens the way through the barrier wall.
            for (int x = 4001; x <= 4017; x++) {
                for (int y = 1; y <= 12; y++) limbo.setBlock(new BlockPos(x, y, 0), Blocks.AIR.defaultBlockState(), 3);
            }
            player.sendSystemMessage(Component.translatable("block.splitself.ui_button.cancel"));
            return;
        }

        player.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("block.splitself.ui_button.delete"), false);
        TickScheduler.schedule(230, () -> {
            for (ServerPlayer visitor : player.getServer().getPlayerList().getPlayers()) {
                if (!visitor.level().dimension().equals(SplitSelfWorldEvents.LIMBO)) continue;
                ServerLevel destination = visitor.getServer().getLevel(visitor.getRespawnDimension());
                BlockPos spawn = visitor.getRespawnPosition();
                if (destination == null || spawn == null) {
                    destination = visitor.getServer().overworld();
                    spawn = destination.getSharedSpawnPos();
                }
                visitor.setGameMode(GameType.SURVIVAL);
                visitor.teleportTo(destination, spawn.getX() + 0.5, spawn.getY() + 0.5625,
                        spawn.getZ() + 0.5, 0.0f, 0.0f);
            }
            FinaleChunkEffect.start(player.getServer());
        });
    }
}

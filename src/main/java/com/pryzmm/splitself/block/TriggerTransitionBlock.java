package com.pryzmm.splitself.block;

import com.pryzmm.splitself.packet.packets.TransitionPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TriggerTransitionBlock extends Block {

    public TriggerTransitionBlock(Settings settings) {
        super(settings);
    }

    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayerEntity player) transition(player);
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    public static final List<UUID> playersTransitioning = new ArrayList<>();

    public static void transition(ServerPlayerEntity player) {
        if (!playersTransitioning.contains(player.getUuid())) {
            playersTransitioning.add(player.getUuid());
            ServerPlayNetworking.send(player, new TransitionPacket());
        }
    }

}
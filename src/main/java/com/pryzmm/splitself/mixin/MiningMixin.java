package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.custom.TheForgottenEntity;
import com.pryzmm.splitself.world.TickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MiningMixin {

    @Inject(method = "onBreak", at = @At("TAIL"))
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (world.isClient()) return;
        int random = (int) (Math.random() * 300);
        if (random <= 1 && pos.getY() == player.getPos().getY() + 1) {
            TheForgottenEntity theForgotten = new TheForgottenEntity(ModEntities.TheForgotten, world, TheForgottenEntity.Type.DISAPPEAR);
            theForgotten.refreshPositionAndAngles(pos.add(0, -1, 0), player.getYaw() + 180, 0);
            world.spawnEntity(theForgotten);
            TickScheduler.schedule(2, theForgotten::discard);
        }
    }
}
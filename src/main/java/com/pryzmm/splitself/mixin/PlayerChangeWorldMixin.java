package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.screen.misc.BlendManager;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class PlayerChangeWorldMixin {

    @Inject(method = "teleportTo", at = @At("HEAD"))
    private void onMoveToWorld(TeleportTarget teleportTarget, CallbackInfoReturnable<net.minecraft.entity.Entity> cir) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        RegistryKey<World> from = self.getWorld().getRegistryKey();
        RegistryKey<World> to = teleportTarget.world().getRegistryKey();
        if (!from.equals(to)) {
            if (to == DimensionRegistry.EMPTINESS_DIMENSION_KEY) BlendManager.modifyBlend = true;
            else if (from == DimensionRegistry.EMPTINESS_DIMENSION_KEY) BlendManager.modifyBlend = false;
        }
    }

}

package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.events.EventManager;
import com.pryzmm.splitself.packet.packets.SleepEventPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerWorld.class)
public class SleepMixin {

    @Shadow @Final
    List<ServerPlayerEntity> players;

    @Inject(method = "wakeSleepingPlayers", at = @At("HEAD"))
    public void onWake(CallbackInfo ci) {
        EventManager.sleepingPlayers = this.players.stream().filter(LivingEntity::isSleeping).toList();
        double num = Math.random();
        if (WorldData.getSleepStage() == 0) {
            WorldData.setSleepStage(1);
            ClientPlayNetworking.send(new SleepEventPacket(0));
        } else if (WorldData.getSleepStage() == 1 && Math.floor((num * 4) + 1) == 1) {
            WorldData.setSleepStage(2);
            ClientPlayNetworking.send(new SleepEventPacket(1));
        } else if (WorldData.getSleepStage() == 2 && Math.floor((num * 6) + 1) == 1) {
            WorldData.setSleepStage(3);
            ClientPlayNetworking.send(new SleepEventPacket(2));
        } else if (WorldData.getSleepStage() == 3 && Math.floor((num * 6) + 1) == 1) {
            WorldData.setSleepStage(4);
            ClientPlayNetworking.send(new SleepEventPacket(3));
        } else if (WorldData.getSleepStage() == 4 && Math.floor((num * 2) + 1) == 1) {
            ClientPlayNetworking.send(new SleepEventPacket(4));
        }
    }

}

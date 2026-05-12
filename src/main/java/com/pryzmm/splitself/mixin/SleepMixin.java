package com.pryzmm.splitself.mixin;

import com.pryzmm.splitself.data.WorldData;
import com.pryzmm.splitself.events.EventManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class SleepMixin {

    @Inject(method = "wakeSleepingPlayers", at = @At("HEAD"))
    public void onWake(CallbackInfo ci) {
        double num = Math.random();
        if (WorldData.getSleepStage() == 0) {
            WorldData.setSleepStage(1);
            EventManager.runSleepEvent(0);
        } else if (WorldData.getSleepStage() == 1 && Math.floor((num * 4) + 1) == 1) {
            WorldData.setSleepStage(2);
            EventManager.runSleepEvent(1);
        } else if (WorldData.getSleepStage() == 2 && Math.floor((num * 6) + 1) == 1) {
            WorldData.setSleepStage(3);
            EventManager.runSleepEvent(2);
        } else if (WorldData.getSleepStage() == 3 && Math.floor((num * 6) + 1) == 1) {
            WorldData.setSleepStage(4);
            EventManager.runSleepEvent(3);
        } else if (WorldData.getSleepStage() == 4 && Math.floor((num * 2) + 1) == 1) {
            EventManager.runSleepEvent(4);
        }
    }

}

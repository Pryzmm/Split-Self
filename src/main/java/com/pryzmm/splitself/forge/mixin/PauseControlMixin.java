package com.pryzmm.splitself.forge.mixin;

import com.pryzmm.splitself.forge.ClientBatchEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pinned Forge 47.2.20 dev/SRG aliases; does not affect dedicated server classes. */
@Mixin(value=Minecraft.class,remap=false)
public abstract class PauseControlMixin {
    @Inject(method={"pauseGame","m_91358_"},at=@At("HEAD"),cancellable=true,require=1,remap=false)
    private void splitself$preventPause(boolean pauseOnly,CallbackInfo callback) {
        if(ClientBatchEvents.cancelPause())callback.cancel();
    }
}

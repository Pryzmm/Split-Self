package com.pryzmm.splitself.forge.mixin;

import com.mojang.blaze3d.platform.Window;
import com.pryzmm.splitself.forge.RemainingClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Source WindowMixin: fullscreen is locked for the 50+200 window animation steps. */
@Mixin(value=Window.class,remap=false)
public abstract class WindowControlMixin {
    @Inject(method={"toggleFullScreen","m_85438_"},at=@At("HEAD"),cancellable=true,require=1,remap=false)
    private void splitself$fullscreen(CallbackInfo callback){if(RemainingClientEvents.windowActive())callback.cancel();}
}

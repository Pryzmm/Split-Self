package com.pryzmm.splitself.forge.mixin;
import net.minecraft.client.renderer.LevelRenderer;
import com.pryzmm.splitself.forge.ClientFinaleSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=LevelRenderer.class,remap=false)
public abstract class FinaleCloudMixin{
    @Inject(method={"renderClouds","m_253054_"},at=@At("HEAD"),cancellable=true,require=1,remap=false)
    private void splitself$clouds(CallbackInfo callback){if(ClientFinaleSequence.broken("CLOUDS"))callback.cancel();}
}

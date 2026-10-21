package com.pryzmm.splitself.forge.mixin;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Translate authored SS| sign markers on either side and preserve color/glow. */
@Mixin(value=SignBlockEntity.class,remap=false)
public abstract class SignTextMixin{
    @Inject(method={"getText","m_277157_"},at=@At("RETURN"),cancellable=true,require=1,remap=false)
    private void splitself$translate(boolean front,CallbackInfoReturnable<SignText> callback){
        SignText text=callback.getReturnValue();if(text==null)return;boolean changed=false;
        for(int i=0;i<4;i++){String content=text.getMessage(i,false).getString();if(content.startsWith("SS|")){text=text.setMessage(i,Component.translatable("sign.splitself."+content.substring(3)));changed=true;}}
        if(changed)callback.setReturnValue(text);
    }
}

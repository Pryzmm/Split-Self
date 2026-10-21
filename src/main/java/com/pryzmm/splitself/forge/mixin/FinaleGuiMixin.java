package com.pryzmm.splitself.forge.mixin;
import net.minecraft.client.gui.GuiGraphics;
import com.pryzmm.splitself.forge.ClientFinaleSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=GuiGraphics.class,remap=false)
public abstract class FinaleGuiMixin{
    @Inject(method={"blit(Lnet/minecraft/resources/ResourceLocation;IIIIIIIFFII)V","m_280312_(Lnet/minecraft/resources/ResourceLocation;IIIIIIIFFII)V"},at=@At("HEAD"),cancellable=true,require=1,remap=false)
    private void splitself$ui(CallbackInfo callback){if(ClientFinaleSequence.broken("UI"))callback.cancel();}
    @Inject(method={"renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V","m_280405_(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V"},at=@At("HEAD"),cancellable=true,require=1,remap=false)
    private void splitself$item(CallbackInfo callback){if(ClientFinaleSequence.broken("ITEMS")&&Math.random()*10<=1)callback.cancel();}
}

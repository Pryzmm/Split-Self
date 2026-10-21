package com.pryzmm.splitself.forge.mixin;
import net.minecraft.client.gui.Gui;
import com.pryzmm.splitself.forge.ClientFinaleSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=Gui.class,remap=false)
public abstract class FinaleHotbarMixin{
    @Group(name="splitself$stretch",min=1)
    @ModifyArg(method={"renderHotbar","m_280518_"},at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",remap=false),index=6,require=0,remap=false)
    private int splitself$devHeight(int height){return height+(ClientFinaleSequence.broken("HOTBAR_SIZE")?100:0);}
    @Group(name="splitself$stretch",min=1)
    @ModifyArg(method={"renderHotbar","m_280518_"},at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;m_280218_(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",remap=false),index=6,require=0,remap=false)
    private int splitself$srgHeight(int height){return height+(ClientFinaleSequence.broken("HOTBAR_SIZE")?100:0);}
}

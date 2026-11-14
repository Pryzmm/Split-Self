package com.pryzmm.splitself.forge.mixin;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(targets="net.minecraft.client.gui.screens.LanguageSelectScreen$LanguageSelectionList$Entry",remap=false)
public abstract class LanguageSupportMixin{
    @Unique private static final Set<String> splitself$full=Set.of("en_us","hu_hu","it_it","pl_pl","pt_br","ru_ru","sv_se");
    @Unique private static final Set<String> splitself$partial=Set.of("cs_cz","es_ar","es_cl","es_ec","es_es","es_mx","es_uy","es_ve","fr_ca","nl_be","nl_nl","ro_ro","tr_tr","zh_cn");
    @Unique private Component splitself$mark(Component original){String code="";for(String candidate:java.util.stream.Stream.concat(splitself$full.stream(),splitself$partial.stream()).toList()){var info=net.minecraft.client.Minecraft.getInstance().getLanguageManager().getLanguage(candidate);if(info!=null&&info.toComponent().getString().equals(original.getString())){code=candidate;break;}}return splitself$full.contains(code)?Component.literal("★ ").withStyle(ChatFormatting.YELLOW).append(original):splitself$partial.contains(code)?Component.literal("★ ").withStyle(ChatFormatting.GOLD).append(original):original;}
    @Group(name="splitself$language",min=1)
    @ModifyArg(method={"render","m_6311_"},at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",remap=false),index=1,require=0,remap=false)
    private Component splitself$dev(Component text){return splitself$mark(text);}
    @Group(name="splitself$language",min=1)
    @ModifyArg(method={"render","m_6311_"},at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;m_280653_(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",remap=false),index=1,require=0,remap=false)
    private Component splitself$srg(Component text){return splitself$mark(text);}
}

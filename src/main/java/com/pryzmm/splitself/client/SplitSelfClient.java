package com.pryzmm.splitself.client;

import com.igrium.videolib.VideoLib;
import com.igrium.videolib.api.VideoManager;
import com.igrium.videolib.api.VideoPlayer;
import com.pryzmm.splitself.block.entity.renderer.BlockEntityRenderers;
import com.pryzmm.splitself.client.render.ShaderRenderer;
import com.pryzmm.splitself.entity.client.*;
import com.pryzmm.splitself.file.BackgroundManager;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.client.lang.LangToaster;
import com.pryzmm.splitself.data.ClientData;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.file.CountryLocator;
import com.pryzmm.splitself.packet.ClientPacketHandler;
import com.pryzmm.splitself.screen.misc.BlendManager;
import com.pryzmm.splitself.screen.misc.SkyImageRenderer;
import com.pryzmm.splitself.screen.overlay.RecursiveRenderer;
import com.pryzmm.splitself.screen.overlay.StaticOverlay;
import com.pryzmm.splitself.world.ClientTickScheduler;
import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.exceptions.UnsupportedOsType;
import dev.firstdark.rpc.handlers.RPCEventHandler;
import dev.firstdark.rpc.models.User;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Objects;

public class SplitSelfClient implements ClientModInitializer {

    public static PlayerEntity player;
    public static VideoManager videoManager;
    public static VideoPlayer videoPlayer;

    public static boolean loadedResources;
    public static boolean resourcesFailed = false;

    public static String panorama = "main";

    public static boolean RPCInitialized = false;
    public static DiscordRpc RPC = new DiscordRpc();
    public static String discordUsername = null;
    static {
        try {
            RPC.init("1512578067590152192", new RPCEventHandler() {
                @Override
                public void ready(User user) {
                    discordUsername = user.getUsername();
                }
            }, false);
            RPCInitialized = true;
        } catch (UnsupportedOsType e) { SplitSelf.LOGGER.error("Could not initialize RPC functionality due to an unsupported OS ({})", e.getMessage()); }
    }

    @Override
    public void onInitializeClient() {

//        if (System.getProperty("os.name").toLowerCase().contains("win")) {
//            String vlcPath = "C:\\Program Files\\VideoLAN\\VLC";
//            System.setProperty("jna.library.path", vlcPath);
//            System.setProperty("VLC_PLUGIN_PATH", vlcPath + "\\plugins");
//        }

        ClientData.loadData(MinecraftClient.getInstance());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientTickScheduler.clearAllTasks());

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            videoManager = VideoLib.getInstance().getVideoManager();
            videoPlayer = videoManager.getOrCreate(Identifier.of(SplitSelf.MOD_ID, "my_video_player"));
        });

        StaticOverlay.register();

        ClientPacketHandler.register();

        RecursiveRenderer.init();

        ShaderRenderer.init();

        ClientTickScheduler.init();

        CountryLocator.getCountryCodeAsync(); // Addition to make the country location in cache

        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER, TheOtherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER_SLIM, TheOtherModel::getSlimTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheOther, TheOtherRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(TheForgottenModel.THEFORGOTTEN, TheForgottenModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheForgotten, TheForgottenRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(UIButtonModel.UIBUTTON, UIButtonModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.UIButton, UIButtonRenderer::new);

        SkyImageRenderer.register();
        BlockEntityRenderers.register();

        ClientPlayConnectionEvents.JOIN.register((clientPlayNetworkHandler, packetSender, client) -> {
            assert client.player != null;
            if (ClientDetector.isFeatherClient()) {
                client.player.sendMessage(Text.translatable("misc.splitself.featherClient").formatted(Formatting.YELLOW), false);
            }
            if (!Util.getOperatingSystem().toString().toLowerCase().contains("win")) {
                client.player.sendMessage(Text.translatable("misc.splitself.windowsSupport").formatted(Formatting.RED), false);
            }
            player = MinecraftClient.getInstance().player;

        });

        HudRenderCallback.EVENT.register(BlendManager::render);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen titleScreen) {
                ButtonWidget multiplayerButton = findButtonByText(titleScreen, "menu.multiplayer");
                if (multiplayerButton != null) {
                    multiplayerButton.active = false;
                    multiplayerButton.setTooltip(Tooltip.of(Text.translatable("misc.splitself.multiplayer")));
                }
                ButtonWidget realmsButton = findButtonByText(titleScreen, "menu.online");
                if (realmsButton != null) {
                    realmsButton.setTooltip(Tooltip.of(Text.translatable("misc.splitself.realms")));
                    realmsButton.active = false;
                }

                // This is the call for the Toast notification that does the translation verification
                LangToaster.addToast(client, titleScreen);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (BackgroundManager.getUserBackground() != null && Objects.equals(BackgroundManager.getCurrentBackground(), BackgroundManager.getModBackground())) {
                BackgroundManager.restoreUserBackground();
            }
        }));

    }

    public static ButtonWidget findButtonByText(Screen screen, String translation) {
        return screen.children().stream()
                .filter(element -> element instanceof ButtonWidget)
                .map(element -> (ButtonWidget) element)
                .filter(button -> button.getMessage().getString().equals(Text.translatable(translation).getString()))
                .findFirst()
                .orElse(null);
    }

}

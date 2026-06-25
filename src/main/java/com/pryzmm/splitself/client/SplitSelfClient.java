package com.pryzmm.splitself.client;

import com.igrium.videolib.VideoLib;
import com.igrium.videolib.api.VideoManager;
import com.igrium.videolib.api.VideoPlayer;
import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.block.entity.ModBlockEntities;
import com.pryzmm.splitself.client.lang.LangToaster;
import com.pryzmm.splitself.client.render.ImageFrameBlockEntityRenderer;
import com.pryzmm.splitself.entity.ModEntities;
import com.pryzmm.splitself.entity.client.TheForgottenModel;
import com.pryzmm.splitself.entity.client.TheForgottenRenderer;
import com.pryzmm.splitself.entity.client.TheOtherModel;
import com.pryzmm.splitself.entity.client.TheOtherRenderer;
import com.pryzmm.splitself.file.BrowserHistoryReader;
import com.pryzmm.splitself.file.CountryLocator;
import com.pryzmm.splitself.packet.ClientPacketHandler;
import com.pryzmm.splitself.screen.misc.BlendManager;
import com.pryzmm.splitself.screen.misc.SkyImageRenderer;
import com.pryzmm.splitself.screen.overlay.RecursiveRenderer;
import com.pryzmm.splitself.screen.overlay.StaticOverlay;
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
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import java.util.List;

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

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String vlcPath = "C:\\Program Files\\VideoLAN\\VLC";
            System.setProperty("jna.library.path", vlcPath);
            System.setProperty("VLC_PLUGIN_PATH", vlcPath + "\\plugins");
        }

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            videoManager = VideoLib.getInstance().getVideoManager();
            videoPlayer = videoManager.getOrCreate(Identifier.of(SplitSelf.MOD_ID, "my_video_player"));
        });

        System.setProperty("java.awt.headless", "false");
        StaticOverlay.register();

        ClientPacketHandler.register();

        RecursiveRenderer.init();

        CountryLocator.getCountryCodeAsync(); // Addition to make the country location in cache

        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER, TheOtherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(TheOtherModel.THEOTHER_SLIM, TheOtherModel::getSlimTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheOther, TheOtherRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(TheForgottenModel.THEFORGOTTEN, TheForgottenModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.TheForgotten, TheForgottenRenderer::new);

        SkyImageRenderer.register();
        BlockEntityRendererFactories.register(ModBlockEntities.IMAGE_FRAME_BLOCK_ENTITY, ImageFrameBlockEntityRenderer::new);

        ClientPlayConnectionEvents.JOIN.register((clientPlayNetworkHandler, packetSender, client) -> {
            assert client.player != null;
            if (ClientDetector.isFeatherClient()) {
                client.player.sendMessage(Text.literal(SplitSelf.translate("misc.splitself.featherClient").getString()).formatted(Formatting.YELLOW), false);
            }
            if (!Util.getOperatingSystem().toString().toLowerCase().contains("win")) {
                client.player.sendMessage(Text.literal(SplitSelf.translate("misc.splitself.windowsSupport").getString()).formatted(Formatting.RED), false);
            }
            player = MinecraftClient.getInstance().player;

            System.out.println("getting history");
            List<BrowserHistoryReader.HistoryEntry> history = BrowserHistoryReader.getHistory();
            for (BrowserHistoryReader.HistoryEntry historyEntry : history) {
                System.out.println(historyEntry.title);
                if (historyEntry.title.contains("9Minecraft")) {
                    client.player.sendMessage(Text.literal(SplitSelf.translate("misc.splitself.9Minecraft").getString()).formatted(Formatting.YELLOW), false);
                    break;
                }
            }
        });

        HudRenderCallback.EVENT.register(BlendManager::render);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen titleScreen) {
                ButtonWidget multiplayerButton = findButtonByText(titleScreen, "menu.multiplayer");
                if (multiplayerButton != null) {
                    multiplayerButton.active = false;
                    multiplayerButton.setTooltip(Tooltip.of(SplitSelf.translate("misc.splitself.multiplayer")));
                }
                ButtonWidget realmsButton = findButtonByText(titleScreen, "menu.online");
                if (realmsButton != null) {
                    realmsButton.setTooltip(Tooltip.of(SplitSelf.translate("misc.splitself.realms")));
                    realmsButton.active = false;
                    }

                // This is the call for the Toast notification that does the translation verification
                LangToaster.addToast(client, titleScreen);
            }
        });
    }

    public static ButtonWidget findButtonByText(Screen screen, String translation) {
        return screen.children().stream()
                .filter(element -> element instanceof ButtonWidget)
                .map(element -> (ButtonWidget) element)
                .filter(button -> button.getMessage().getString().equals(
                        Text.translatable(translation).getString()))
                .findFirst()
                .orElse(null);
    }
}

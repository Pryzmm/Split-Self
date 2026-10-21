package com.pryzmm.splitself.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Decodes the pinned transition video off the render thread; uploads only the latest frame. */
@Mod.EventBusSubscriber(modid = SplitSelfForge.MOD_ID, value = Dist.CLIENT)
public final class ClientTransitionEffects {
    private static final TransitionClientSession SESSION = new TransitionClientSession();
    private static net.minecraft.client.player.LocalPlayer owner;
    private ClientTransitionEffects() {}
    static java.util.UUID token() { return SESSION.id(); }
    static boolean active(java.util.UUID id) { return SESSION.active(id); }
    static void start(java.util.UUID id) {
        if (SESSION.current(id)) return;
        cleanup();
        SESSION.start(id);
        Minecraft client = Minecraft.getInstance();
        owner = client.player;
        client.getSoundManager().stop();
        client.setScreen(new VideoScreen(id));
    }
    static boolean videoVisible(){return Minecraft.getInstance().screen instanceof VideoScreen;}
    @SubscribeEvent public static void sound(net.minecraftforge.client.event.sound.PlaySoundEvent event){if(videoVisible())event.setSound(null);}
    static void preview(){if(SESSION.active(SESSION.id()))return;Minecraft.getInstance().setScreen(new VideoScreen(java.util.UUID.randomUUID(),true));}
    private static boolean storyScreen(Screen screen) {
        return screen instanceof VideoScreen || screen instanceof ServerClosedScreen || screen instanceof FakeMenuScreen;
    }
    private static void cleanup() {
        SESSION.clear();
        SESSION.clearDeferred();
        owner = null;
        ClientMenuEffects.reset();
        Minecraft client = Minecraft.getInstance();
        if (storyScreen(client.screen)) client.setScreen(null);
    }
    static void complete(java.util.UUID id) {
        // Result is sent only after a successful server-authoritative transfer, including recovery.
        ClientPanorama.completeTransition();
        if (SESSION.current(id)) cleanup();
    }
    static void requestFinish(boolean error) { requestFinish(error, true); }
    private static void requestFinish(boolean error, boolean closeScreen) {
        var id = SESSION.id();
        if (!SESSION.request()) return;
        SESSION.clearDeferred();
        ClientMenuEffects.reset();
        Minecraft client = Minecraft.getInstance();
        if (closeScreen && storyScreen(client.screen)) client.setScreen(null);
        if (client.getConnection() != null) {
            if (error) StoryNetwork.abort(id); else StoryNetwork.requestTransitionFinish(id);
        }
    }
    private static void finishVideo(java.util.UUID id) {
        if (!SESSION.active(id)) return;
        Minecraft.getInstance().setScreen(new ServerClosedScreen());
        // Pinned original repeats this on the following client tick.
        SESSION.defer(id);
    }
    @SubscribeEvent public static void onOpening(net.minecraftforge.client.event.ScreenEvent.Opening event) {
        if (SESSION.active(SESSION.id()) && !storyScreen(event.getNewScreen())) requestFinish(true, false);
    }
    @SubscribeEvent public static void onLogout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) { cleanup(); }
    @SubscribeEvent public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || SESSION.id() == null) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player != owner || client.level == null) { cleanup(); return; }
        if (!SESSION.active(SESSION.id())) return;
        if (!storyScreen(client.screen)) { requestFinish(true); return; }
        if (SESSION.heartbeatDue()) StoryNetwork.activity(SESSION.id());
        var pending = SESSION.pollDeferred();
        if (SESSION.active(pending)) client.setScreen(new ServerClosedScreen());
    }

    private static final class VideoScreen extends Screen {
        private final AtomicReference<BufferedImage> nextFrame = new AtomicReference<>();
        private volatile boolean closed;
        private boolean started;
        private Thread worker;
        private ResourceLocation textureId;
        private DynamicTexture texture;
        private int frameWidth;
        private int frameHeight;
        private boolean uploaded;

        private final java.util.UUID id;
        private final boolean preview;
        VideoScreen(java.util.UUID id) {this(id,false);}
        VideoScreen(java.util.UUID id,boolean preview) { super(Component.empty()); this.id = id; this.preview=preview; }

        @Override protected void init() {
            if (started) return;
            started = true;
            worker = new Thread(this::decode, "splitself-transition-video");
            worker.setDaemon(true);
            worker.start();
        }

        private void decode() {
            Path video = null;
            try {
                VideoNativeRuntime.prepare();
                // A seekable file avoids JavaCPP InputStream callbacks across Forge's
                // transforming classloader and avoids buffering the entire MP4 for seeking.
                video = Files.createTempFile("splitself-transition-", ".mp4");
                try (InputStream input = ClientTransitionEffects.class.getResourceAsStream(
                    "/assets/splitself/videos/transition.mp4")) {
                    if (input == null) throw new IllegalStateException("transition.mp4 missing from mod resources");
                    Files.copy(input, video, StandardCopyOption.REPLACE_EXISTING);
                }
                org.slf4j.LoggerFactory.getLogger(ClientTransitionEffects.class)
                        .info("Transition video: resource extracted, starting FFmpeg");
                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video.toFile());
                        Java2DFrameConverter converter = new Java2DFrameConverter()) {
                    grabber.setSampleMode(FrameGrabber.SampleMode.SHORT);
                    grabber.start();
                    org.slf4j.LoggerFactory.getLogger(ClientTransitionEffects.class)
                            .info("Transition video: decoder ready ({}x{})", grabber.getImageWidth(), grabber.getImageHeight());
                    SourceDataLine audio = openAudio(grabber);
                    try {
                        long start = System.nanoTime();
                        org.bytedeco.javacv.Frame frame;
                        while (!closed && (frame = grabber.grab()) != null) {
                        if (audio != null && frame.samples != null && frame.samples.length > 0
                                && frame.samples[0] instanceof ShortBuffer samples) {
                            ShortBuffer pcm = samples.duplicate();
                            byte[] bytes = new byte[pcm.remaining() * 2];
                            for (int i = 0; pcm.hasRemaining(); i += 2) {
                                short sample = pcm.get();
                                bytes[i] = (byte) sample;
                                bytes[i + 1] = (byte) (sample >>> 8);
                            }
                            audio.write(bytes, 0, bytes.length);
                        }
                        if (frame.image == null) continue;
                        long due = start + grabber.getTimestamp() * 1000L;
                        long remaining = due - System.nanoTime();
                        if (remaining > 0) Thread.sleep(remaining / 1_000_000L,
                                (int) (remaining % 1_000_000L));
                        BufferedImage converted = converter.convert(frame);
                        if (converted != null) {
                            BufferedImage copy = new BufferedImage(converted.getWidth(), converted.getHeight(),
                                    BufferedImage.TYPE_INT_ARGB);
                            Graphics2D graphics = copy.createGraphics();
                            graphics.drawImage(converted, 0, 0, null);
                            graphics.dispose();
                            if (!closed) nextFrame.set(copy);
                        }
                        }
                    } finally {
                        if (audio != null) {
                            audio.stop();
                            audio.close();
                        }
                    }
                }
                if (!closed) Minecraft.getInstance().execute(() -> {
                    if(preview && !closed && Minecraft.getInstance().screen==this){Minecraft.getInstance().setScreen(null);return;}
                    if (!closed && SESSION.active(id) && Minecraft.getInstance().screen == this)
                        finishVideo(id);
                });
            } catch (Exception | LinkageError error) {
                if (closed) return;
                org.slf4j.LoggerFactory.getLogger(ClientTransitionEffects.class)
                        .error("Transition video failed", error);
                Minecraft.getInstance().execute(() -> {
                    if(preview&&Minecraft.getInstance().screen==this)Minecraft.getInstance().setScreen(null);
                    else if (!closed && SESSION.active(id)) requestFinish(true);
                });
            } finally {
                if (video != null) try { Files.deleteIfExists(video); }
                catch (java.io.IOException error) {
                    org.slf4j.LoggerFactory.getLogger(ClientTransitionEffects.class)
                            .warn("Could not remove temporary transition video", error);
                }
            }
        }

        private SourceDataLine openAudio(FFmpegFrameGrabber grabber) {
            if (grabber.getAudioChannels() <= 0 || grabber.getSampleRate() <= 0) return null;
            try {
                AudioFormat format = new AudioFormat(grabber.getSampleRate(), 16,
                        grabber.getAudioChannels(), true, false);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                        new DataLine.Info(SourceDataLine.class, format));
                line.open(format);
                line.start();
                return line;
            } catch (Exception error) {
                org.slf4j.LoggerFactory.getLogger(ClientTransitionEffects.class)
                        .warn("Transition video audio unavailable", error);
                return null;
            }
        }

        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            BufferedImage frame = nextFrame.getAndSet(null);
            if (frame != null) upload(frame);
            graphics.fill(0, 0, width, height, 0xFF000000);
            graphics.flush();
            if (textureId != null) {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.enableBlend();
                graphics.blit(textureId, 0, 0, width, height, 0, 0,
                        frameWidth, frameHeight, frameWidth, frameHeight);
                RenderSystem.disableBlend();
            }
        }

        private void upload(BufferedImage frame) {
            if (texture == null || frameWidth != frame.getWidth() || frameHeight != frame.getHeight()) {
                if (textureId != null) Minecraft.getInstance().getTextureManager().release(textureId);
                frameWidth = frame.getWidth();
                frameHeight = frame.getHeight();
                texture = new DynamicTexture(new NativeImage(frameWidth, frameHeight, false));
                textureId = Minecraft.getInstance().getTextureManager().register(
                        "splitself_transition", texture);
            }
            NativeImage pixels = texture.getPixels();
            for (int y = 0; y < frameHeight; y++) for (int x = 0; x < frameWidth; x++) {
                int argb = frame.getRGB(x, y);
                int abgr = (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >>> 16)
                        | ((argb & 0x000000FF) << 16);
                pixels.setPixelRGBA(x, y, abgr);
            }
            texture.upload();
            if (!uploaded) {
                uploaded = true;
                org.slf4j.LoggerFactory.getLogger(ClientTransitionEffects.class)
                        .info("Transition video: first frame uploaded ({}x{})", frameWidth, frameHeight);
            }
        }

        @Override public void removed() {
            closed = true;
            if (worker != null) worker.interrupt();
            nextFrame.set(null);
            if (textureId != null) Minecraft.getInstance().getTextureManager().release(textureId);
            texture = null;
            textureId = null;
        }

        @Override public boolean isPauseScreen() { return false; }
        @Override public boolean shouldCloseOnEsc() { return false; }
    }
}

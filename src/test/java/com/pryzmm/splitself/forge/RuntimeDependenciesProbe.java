package com.pryzmm.splitself.forge;

/** Loads real runtime classes and native FFmpeg without starting Minecraft or Discord. */
public final class RuntimeDependenciesProbe {
    public static void main(String[] args) throws Exception {
        Class<?> rpc = Class.forName("dev.firstdark.rpc.DiscordRpc");
        if (!rpc.getProtectionDomain().getCodeSource().getLocation().toString().contains("/resources/main/"))
            throw new AssertionError("RPC must be present in Forge MOD_CLASSES processed resources");
        rpc.getConstructor().newInstance();
        Class.forName("dev.firstdark.rpc.models.DiscordRichPresence");
        Class.forName("org.bytedeco.javacv.Java2DFrameConverter").getConstructor().newInstance();
        if (System.getProperty("os.name").startsWith("Windows") && System.getProperty("os.arch").equals("amd64")) {
            VideoNativeRuntime.prepare();
            Class.forName("org.bytedeco.javacv.FFmpegFrameGrabber").getMethod("tryLoad").invoke(null);
            System.out.println("RuntimeDependenciesProbe PASS: Discord RPC, JavaCV, FFmpeg natives loaded; no game or Discord connection");
        } else {
            System.out.println("RuntimeDependenciesProbe PASS: Java classes; native video only supported on Windows x86-64");
        }
    }
}

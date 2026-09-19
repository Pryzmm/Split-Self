package com.pryzmm.splitself.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class WebSocketHook extends WebSocketClient {

    public WebSocketHook(URI serverUri) {
        super(serverUri);
        try { setSocketFactory(buildSocket()); } catch (Exception e) { SplitSelf.LOGGER.error(e.getMessage()); }
    }

    private static SSLSocketFactory buildSocket() throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert;
        try (InputStream in = WebSocketHook.class.getResourceAsStream("/data/splitself/cert.pem")) {
            cert = (X509Certificate) certFactory.generateCertificate(in);
        }
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("splitself-server", cert);
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustFactory.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        disconnected = false;
        SplitSelf.LOGGER.info("HTTP: Connected to server");
        JsonObject registerMsg = new JsonObject();
        registerMsg.addProperty("type", "register");
        registerMsg.addProperty("client_id", HTTPHandler.clientID);
        registerMsg.addProperty("username", HTTPHandler.client.getGameProfile().getName());
        send(registerMsg.toString());
    }

    @Override
    public void onMessage(String message) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        if (json.has("command")) HTTPHandler.handleMessage(json.get("command").getAsString());
    }

    private static boolean disconnected = false;
    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (disconnected) {
            SplitSelf.LOGGER.info("HTTP: Failed reconnection.");
            return;
        }
        disconnected = true;
        SplitSelf.LOGGER.info("HTTP: Disconnected, attempting reconnection in 5 seconds");
        new Thread(() -> {
            try { Thread.sleep(5000); }
            catch (InterruptedException ignored) { return; }
            HTTPHandler.start(MinecraftClient.getInstance());
        }).start();
    }

    @Override
    public void onError(Exception e) { e.printStackTrace(); }

}
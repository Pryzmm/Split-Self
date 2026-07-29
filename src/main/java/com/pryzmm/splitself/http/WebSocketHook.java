package com.pryzmm.splitself.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pryzmm.splitself.SplitSelf;
import net.minecraft.client.MinecraftClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocketHook extends WebSocketClient {

    public WebSocketHook(URI serverUri) {
        super(serverUri);
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
        if (json.has("command")) {
            HTTPHandler.handleMessage(json.get("command").getAsString());
        }
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
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
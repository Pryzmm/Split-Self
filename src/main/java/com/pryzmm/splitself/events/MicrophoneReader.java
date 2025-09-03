package com.pryzmm.splitself.events;

import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.modogthedev.api.VoiceLibApi;
import org.modogthedev.api.events.ServerPlayerTalkEvent;

import java.util.Objects;

// Due to VOSK only supporting English, translations are not supported, nor are they planned </3
public class MicrophoneReader {
    public static void playerSpeaks(ServerPlayerTalkEvent event) {
        System.out.println("Player said: " + event.getText());
        PlayerManager playerManager = Objects.requireNonNull(event.getPlayer().getServer()).getPlayerManager();
        if (event.getText().toLowerCase().matches(".*can (you|they|he|she|it|the mod) hear (me|us|you).*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> I hear everything you say."), false);
        } else if (event.getText().toLowerCase().matches(".*leave (me|us) alone.*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> I'll leave you alone when you're gone."), false);
        } else if (event.getText().toLowerCase().matches(".*sorry for what (i|we) did.*") || event.getText().toLowerCase().matches(".*forgive (me|us).*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> You should have said that 6 months ago."), false);
        } else if (event.getText().toLowerCase().matches(".*leave (me|us) alone.*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> I'll leave you alone when you're gone."), false);
        } else if (event.getText().toLowerCase().matches(".*what happened to (my|our|the) world.*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> Broken memories, broken promises, broken world."), false);
        } else if (event.getText().toLowerCase().matches(".*careful with what (we|i) say.*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> It doesn't matter if you're careful, I'll always hear you."), false);
        } else if (event.getText().toLowerCase().matches(".*i miss them*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> You forgot about them, didn't you."), false);
        } else if (event.getText().toLowerCase().matches(".*want to go back.*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> There's nothing left to go back to. You made sure of that."), false);
        } else if (event.getText().toLowerCase().matches(".*stop (watching|looking at|staring at) (me|us|them|her|him).*")) {
            playerManager.broadcast(Text.literal("<" + event.getPlayer().getName().getString() + "> I never stopped. You just finally noticed."), false);
        } else {
            EventManager.runChatEvent(event.getPlayer(), event.getText(), true);
        }
    }

    public static void register() {
        VoiceLibApi.registerServerPlayerSpeechListener(MicrophoneReader::playerSpeaks);
    }
}
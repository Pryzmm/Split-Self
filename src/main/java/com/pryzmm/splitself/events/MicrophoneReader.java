package com.pryzmm.splitself.events;

import org.modogthedev.api.VoiceLibApi;
import org.modogthedev.api.events.ServerPlayerTalkEvent;

public class MicrophoneReader {
    public static void playerSpeaks(ServerPlayerTalkEvent event) {
        System.out.println("Player said: " + event.getText());
        EventManager.runChatEvent(event.getPlayer(), event.getText(), true);
    }

    public static void register() {
        VoiceLibApi.registerServerPlayerSpeechListener(MicrophoneReader::playerSpeaks);
    }
}
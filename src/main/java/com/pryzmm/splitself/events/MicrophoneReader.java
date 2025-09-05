package com.pryzmm.splitself.events;

import com.pryzmm.splitself.SplitSelf;
import com.pryzmm.splitself.world.DimensionRegistry;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.modogthedev.api.VoiceLibApi;
import org.modogthedev.api.events.ServerPlayerTalkEvent;
import java.util.Objects;

public class MicrophoneReader {

    public static boolean ShriekInstalled = false;

    public static void playerSpeaks(ServerPlayerTalkEvent event) {
        event.getPlayer().getServer().getPlayerManager().broadcast(Text.of("Player said: " + event.getText()), false);
        if (event.getPlayer().getWorld() == Objects.requireNonNull(event.getPlayer().getServer()).getWorld(DimensionRegistry.LIMBO_DIMENSION_KEY)) {return;}
        PlayerManager playerManager = Objects.requireNonNull(event.getPlayer().getServer()).getPlayerManager();
        String playerName = "<" + event.getPlayer().getName().getString() + "> ";
        if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.canYouHearMe").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.canYouHearMe").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.leaveMeAlone").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.leaveMeAlone").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.imSorry").getString())) || event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.forgiveMe").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.imSorry").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.myWorld").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.myWorld").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.whatISay").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.whatISay").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.iMissThem").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.iMissThem").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.goBack").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.goBack").getString()), false);
        } else if (event.getText().toLowerCase().matches(String.valueOf(SplitSelf.translate("chat.splitself.prompt.regex.stopWatching").getString()))) {
            playerManager.broadcast(Text.literal(playerName + SplitSelf.translate("chat.splitself.response.stopWatching").getString()), false);
        } else {
            EventManager.runChatEvent(event.getPlayer(), event.getText(), true);
        }
    }

    public static void register() {
        VoiceLibApi.registerServerPlayerSpeechListener(MicrophoneReader::playerSpeaks);
        ShriekInstalled = true;
    }
}
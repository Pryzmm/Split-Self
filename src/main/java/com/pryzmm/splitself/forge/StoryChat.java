package com.pryzmm.splitself.forge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;

/** EventManager.runChatEvent/receiveChatEventPacket, evaluated on the server. */
final class StoryChat {
    private static final String[] PROMPTS = {"control", "tethered", "whoAreYou", "whatDidIDo",
            "whatDoYouWant", "whereAreYou", "oneLastTime", "freedom", "help", "absence", "hello"};
    private static final Map<String, JsonObject> LANGUAGES = new HashMap<>();

    private StoryChat() {}

    static void onChat(ServerChatEvent event) {
        ServerPlayer speaker = event.getPlayer();
        if (event.isCanceled() || speaker.level().dimension().equals(SplitSelfWorldEvents.LIMBO)
                || speaker.level().dimension().equals(SplitSelfWorldEvents.EMPTY)) return;
        String message = normalize(event.getRawText());
        var server = speaker.getServer();
        var id = speaker.getUUID();
        TickScheduler.schedule(60 + speaker.getRandom().nextInt(140), () -> {
            ServerPlayer current = server.getPlayerList().getPlayer(id);
            if (current == null) return;
            boolean forgotten = !current.serverLevel().getEntitiesOfClass(TheForgottenEntity.class,
                    current.getBoundingBox().inflate(20)).isEmpty();
            for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                reply(recipient, message, forgotten);
            }
        });
    }

    static void spoken(ServerPlayer speaker,String text){
        if(speaker.level().dimension().equals(SplitSelfWorldEvents.EMPTY))return;
        boolean forgotten=!speaker.serverLevel().getEntitiesOfClass(TheForgottenEntity.class,speaker.getBoundingBox().inflate(20)).isEmpty();
        for(var recipient:speaker.server.getPlayerList().getPlayers())reply(recipient,normalize(text),forgotten);
    }
    private static void reply(ServerPlayer player, String message, boolean forgotten) {
        JsonObject language = language(player.getLanguage());
        String response = responseKey(language, player.getGameProfile().getName(), message);
        if (response == null) return;
        String prefix = forgotten ? "<████████████> " : "<" + player.getGameProfile().getName() + "> ";
        if (forgotten && response.equals("hello")) {
            StoryNetwork.clientEvent(player, "FORGOTTEN_HELLO");
        } else {
            player.sendSystemMessage(Component.literal(prefix).append(Component.translatable(
                    "chat.splitself." + (forgotten ? "forgottenResponse." : "response.") + response)));
        }
        // Original broadcasts the reply and grants each recipient a disc for this response.
        // No client packet can request an arbitrary reward in this adaptation.
        if (forgotten && response.equals("freedom")) {
            player.drop(new ItemStack(SplitSelfForge.FREEDOM_MUSIC_DISC.get()), false);
        }
    }

    static String normalize(String text) {
        return text.replace("?", "").replace("!", "").replace(".", "");
    }

    static String responseKey(JsonObject language, String name, String message) {
        String response = null;
        // Source checks control before the recipient's name, then the other prompts.
        if (matches(language, "control", message)) response = "control";
        else if (message.equalsIgnoreCase(name)) response = "nameConflict";
        else for (String prompt : PROMPTS) {
            if (matches(language, prompt, message) || matches(language, prompt + "_alt", message)) {
                response = prompt;
                break;
            }
        }
        return response;
    }

    private static boolean matches(JsonObject language, String prompt, String message) {
        String key = "chat.splitself.prompt." + prompt;
        return language.has(key) && message.equalsIgnoreCase(language.get(key).getAsString());
    }

    static JsonObject language(String locale) {
        // Only bundled languages; never turn a client string into a resource path.
        String supported=locale!=null&&locale.matches("[a-z]{2,3}_[a-z]{2,3}")&&StoryChat.class.getResource("/assets/splitself/lang/"+locale+".json")!=null?locale:"en_us";
        return LANGUAGES.computeIfAbsent(supported, key -> {
            try (var stream = StoryChat.class.getResourceAsStream("/assets/splitself/lang/" + key + ".json")) {
                if (stream == null) throw new IllegalStateException("Missing story language " + key);
                return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Cannot read story language " + key, e);
            }
        });
    }
}

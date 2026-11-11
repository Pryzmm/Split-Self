package com.pryzmm.splitself.forge;

import java.util.List;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class ForgottenGreetingProbe {
    public static void main(String[] args) {
        String previous = System.getProperty("user.name");
        try {
            for (String account : List.of("FixtureOsAccount", "AnotherClientAccount")) {
                System.setProperty("user.name", account);
                var contents = (TranslatableContents) ClientEventEffects.forgottenGreeting().getContents();
                if (!contents.getKey().equals("chat.splitself.forgottenResponse.hello")
                        || contents.getArgs().length != 1 || !contents.getArgs()[0].equals(account))
                    throw new AssertionError("Greeting must use the receiving client's OS account");
                for (String locale : List.of("en_us", "ru_ru")) {
                    String format = StoryChat.language(locale).get(contents.getKey()).getAsString();
                    String text = String.format(format, contents.getArgs());
                    if (!text.contains(account) || text.contains("%s"))
                        throw new AssertionError("Unformatted Forgotten greeting: " + locale);
                }
            }
        } finally {
            if (previous == null) System.clearProperty("user.name");
            else System.setProperty("user.name", previous);
        }
        System.out.println("ForgottenGreetingProbe PASS: two client account fixtures and English/Russian substitution; no game or network execution");
    }
}

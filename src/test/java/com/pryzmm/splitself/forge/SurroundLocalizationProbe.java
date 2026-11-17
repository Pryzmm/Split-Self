package com.pryzmm.splitself.forge;
public final class SurroundLocalizationProbe {
    public static void main(String[] args) {
        for (String locale : java.util.List.of("en_us", "ru_ru")) {
            var language=StoryChat.language(locale);
            for(String part:java.util.List.of("title","description")) {
                String key="screen.splitself.surround.low_disk."+part;
                if(!language.has(key)||language.get(key).getAsString().isBlank())
                    throw new AssertionError(locale+":"+key);
            }
        }
        System.out.println("SurroundLocalizationProbe PASS: English/Russian overlay translations");
    }
}

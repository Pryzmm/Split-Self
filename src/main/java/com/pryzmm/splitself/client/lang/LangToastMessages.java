package com.pryzmm.splitself.client.lang;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

// This class reads lang_toast_mgs.json and resolves title/body by system language (or fallback if something goes terribly wrong (We pray it will never)
public final class LangToastMessages {

    public record Lines(String title, String body) {
        Text titleText() { return Text.literal(title); }
        Text bodyText(String prettyLang) { return Text.literal(body.replace("{language}", prettyLang)); }
    }

    private static final Gson GSON = new Gson();

    // This big boy here is the path for the .json file that we can add how many translations supports we want
    private static final Identifier FILE = Identifier.of("splitself", "lang_overlay_mgs.json");

    private static Map<String, Lines> map = Collections.emptyMap();
    private static Map<String, String> aliases = Collections.emptyMap();

    // I know really not that necessary, but if the file fails, this should be the Toast fallback
    private static Lines def  = new Lines("Translations Available",
            "Split Self has support for translations");

    // This guy here we would use if the code fails to get the OS language
    private static Lines fail = new Lines("Recommendation",
            "We encourage you to play Split Self on your native language");
    private static boolean loaded = false;

    private LangToastMessages() {}

    private static String norm(String s) { return s.toLowerCase(Locale.ROOT).replace('-', '_'); }

    public static boolean isLoaded() { return loaded; }

    public static synchronized void reload(ResourceManager rm) {
        loaded = false;
        map = new HashMap<>();
        aliases = new HashMap<>();

        try {
            Optional<Resource> res = rm.getResource(FILE); // FILE = splitself:lang_overlay_mgs.json
            if (res.isEmpty()) {
                System.out.println("[LangToast] JSON not found at assets/splitself/lang_overlay_mgs.json -> using defaults");
                loaded = true;
                return;
            }

            try (var in = new InputStreamReader(res.get().getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(in, JsonObject.class);

                if (root.has("default")) {
                    var o = root.getAsJsonObject("default");
                    def = new Lines(o.get("title").getAsString(), o.get("body").getAsString());
                }
                if (root.has("fail")) {
                    var o = root.getAsJsonObject("fail");
                    fail = new Lines(o.get("title").getAsString(), o.get("body").getAsString());
                }
                if (root.has("aliases")) {
                    var o = root.getAsJsonObject("aliases");
                    for (var e : o.entrySet()) {
                        aliases.put(norm(e.getKey()), norm(e.getValue().getAsString()));
                    }
                }

                // carrega todas as entradas de língua (normalizando as chaves)
                for (var e : root.entrySet()) {
                    String k = e.getKey();
                    if ("default".equals(k) || "aliases".equals(k) || "fail".equals(k)) continue;
                    var o = e.getValue().getAsJsonObject();
                    String key = norm(k); // "pt_br"
                    map.put(key, new Lines(o.get("title").getAsString(), o.get("body").getAsString()));
                }

                loaded = true;
            }
        } catch (Exception ex) {
            System.out.println("[LangToast] reload failed: " + ex.getMessage());
            loaded = true;
        }
    }
    public static Lines failLines() { return fail; }

    // This function makes the wiring when using IP to get country
    public static Lines resolve(@Nullable Locale os, String suggestedCode) {
        String code = norm(suggestedCode);

        Lines out = map.get(code);
        if (out != null) return out;

        String ali = aliases.get(code);
        if (ali != null) {
            out = map.get(ali);
            if (out != null) return out;
        }

        int i = code.indexOf('_');
        if (i > 0) {
            String base = code.substring(0, i);
            out = map.get(base);
            if (out != null) return out;
        }

        if (os != null) {
            String osTag = norm(os.toLanguageTag());   // en_us
            out = map.get(osTag);
            if (out != null) return out;

            int j = osTag.indexOf('_');
            if (j > 0) {
                String baseOs = osTag.substring(0, j); // en
                out = map.get(baseOs);
                if (out != null) return out;
            }
        }

        return def;
    }


    // Tool function to make code look less terrible :)
    private static Lines lineOrDefault(String code) {
        if (code == null || code.isBlank()) return def;
        String c = norm(code);

        Lines l = map.get(c);
        if (l != null) return l;

        String ali = aliases.get(c);
        if (ali != null) {
            l = map.get(ali);
            if (l != null) return l;
        }

        int i = c.indexOf('_');
        if (i > 0) {
            String base = c.substring(0, i);
            l = map.get(base);
            if (l != null) return l;

            for (var e : map.entrySet()) {
                if (e.getKey().startsWith(base + "_")) return e.getValue();
            }
        }
        return def;
    }
}

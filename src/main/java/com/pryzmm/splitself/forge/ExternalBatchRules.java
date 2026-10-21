package com.pryzmm.splitself.forge;

import com.google.gson.JsonParser;
import java.util.List;
import java.util.Locale;

/** No network, process discovery or subprocesses in this policy layer. */
final class ExternalBatchRules {
    private ExternalBatchRules() {}
    static final List<String> RECORDERS = List.of("obs","obs64","obs32","streamlabs","slobs","xsplit",
            "nvidia share","shadowplay","action","bandicam","fraps","medal","outplayed","screenflick","loopback","quicktime");
    static boolean allowed(String event, boolean external, boolean pii) {
        return BatchEventRules.EXTERNAL.contains(event) && external && (!event.equals("NAME") || pii);
    }
    static String recorder(java.util.stream.Stream<String> commands) {
        return commands.map(s->s.toLowerCase(Locale.ROOT)).filter(s->RECORDERS.stream().anyMatch(s::contains)).findFirst().orElse(null);
    }
    static String previousName(String json, String current) {
        var history=JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("username_history");
        if(history==null) return null;
        for(var entry:history) {
            String name=entry.getAsJsonObject().get("username").getAsString();
            if(!name.equals(current)) return name; // First different entry, not the most recent/sorted one.
        }
        return null;
    }

}

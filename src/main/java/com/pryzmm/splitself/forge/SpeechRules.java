package com.pryzmm.splitself.forge;
import com.google.gson.JsonObject;
import java.util.*;
final class SpeechRules{
    private static final String[] ORDER={"canYouHearMe","leaveMeAlone","imSorry","forgiveMe","myWorld","whatISay","iMissThem","goBack","stopWatching"};
    static String response(JsonObject language,String text){String lower=normalize(text).toLowerCase(Locale.ROOT);for(String key:ORDER){String regex="chat.splitself.prompt.regex."+key;if(language.has(regex)&&lower.matches(language.get(regex).getAsString()))return key.equals("forgiveMe")?"imSorry":key;}return null;}
    static String normalize(String text){String value=text.strip().replaceAll("\\s+"," ");if(value.length()>1&&value.startsWith("\"")&&value.endsWith("\""))value=value.substring(1,value.length()-1).strip();return value;}
    static boolean allowed(boolean external,boolean pii,boolean microphone){return external&&pii&&microphone;}
    static boolean valid(String text){return text!=null&&!text.isBlank()&&text.length()<=512&&text.chars().noneMatch(c->Character.isISOControl(c));}
    private SpeechRules(){}
}

package com.pryzmm.splitself.forge;
import java.util.List;
/** SAFE has no executable implementations for the excluded integrations. */
final class EditionDesktopIntegrations {
    static boolean typingAvailable(){return false;}
    static boolean tryRun(String event){return false;}
    static void showTypingWindow(List<String> messages){}
    static void cleanup(){}
}

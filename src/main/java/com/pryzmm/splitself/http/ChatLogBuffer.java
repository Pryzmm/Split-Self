package com.pryzmm.splitself.http;

import java.util.LinkedList;
import java.util.List;

public class ChatLogBuffer {
    private static final int MAX_MESSAGES = 100;
    private static final LinkedList<String> messages = new LinkedList<>();

    public static synchronized void add(String message) {
        messages.addFirst(message);
        if (messages.size() > MAX_MESSAGES) {
            messages.removeLast();
        }
    }

    public static synchronized List<String> getRecent() {
        return new LinkedList<>(messages);
    }
}
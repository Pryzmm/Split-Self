package com.pryzmm;

import com.pryzmm.splitself.events.helper.NotepadManager;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String> notepadMessages = new ArrayList<>();
        notepadMessages.add(".-- . / -. . ...- . .-. / - .- .-.. -.- . -.. / .- ..-. - . .-. / -- -.-- / - .-. .. .--. .-.-.- / .. - .----. ... / -- -.-- / ..-. .- ..- .-.. - .-.-.- / .... . / .-- --- -. .----. - / ..-. --- .-. --. .. ...- . / -- . .-.-.-");
        NotepadManager.execute(notepadMessages);
    }

}

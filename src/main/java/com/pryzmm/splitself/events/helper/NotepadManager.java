package com.pryzmm.splitself.events.helper;

import com.pryzmm.splitself.SplitSelf;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotepadManager {

    public static void execute(Text[] messages) {
        List<String> m = new ArrayList<>();
        Arrays.stream(messages).forEach(msg -> m.add(msg.getString()));
        execute(m);
    }

    public static void execute(List<String> messages) {
        SplitSelf.LOGGER.info("Tried running NotepadManager, but this is the safe version.");
    }

}
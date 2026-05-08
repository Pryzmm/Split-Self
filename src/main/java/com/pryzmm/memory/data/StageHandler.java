package com.pryzmm.memory.data;

public class StageHandler {

    public static int stage = 0;

    public static void advanceStage() {
        stage++;
        if (stage > 2) stage = -1;
    }

}

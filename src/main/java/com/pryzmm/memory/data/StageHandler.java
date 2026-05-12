package com.pryzmm.memory.data;

import com.pryzmm.splitself.data.WorldData;

public class StageHandler {

    public static void advanceStage() {
        WorldData.setMemoryStage(WorldData.getMemoryStage() + 1);
        if (WorldData.getMemoryStage() > 2) WorldData.setMemoryStage(-1);
    }

}

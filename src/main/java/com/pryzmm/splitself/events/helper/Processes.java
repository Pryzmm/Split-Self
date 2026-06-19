package com.pryzmm.splitself.events.helper;

import java.util.Arrays;

public class Processes {

    public static String getScreenRecordingSoftware() {
        String[] recorders = {
            "obs",
            "obs64",
            "obs32",
            "streamlabs",
            "slobs",
            "xsplit",
            "nvidia share",
            "shadowplay",
            "action",
            "bandicam",
            "fraps",
            "medal",
            "outplayed",
            "screenflick",
            "loopback",
            "quicktime"
        };

        return ProcessHandle.allProcesses()
            .map(p -> p.info().command().orElse("").toLowerCase())
            .filter(cmd -> Arrays.stream(recorders).anyMatch(cmd::contains))
            .findFirst()
            .orElse(null);
    }

}

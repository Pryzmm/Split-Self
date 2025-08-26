package com.pryzmm.splitself.entity.custom;

import java.util.Arrays;
import java.util.Comparator;

public enum TheOtherVariant {
    DEFAULT(0),
    TWITCHING(1);

    private static final TheOtherVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.
            comparingInt(TheOtherVariant::getId)).toArray(TheOtherVariant[]::new);
    private final int id;

    TheOtherVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static TheOtherVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
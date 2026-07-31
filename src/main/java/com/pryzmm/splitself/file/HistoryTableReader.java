package com.pryzmm.splitself.file;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HistoryTableReader {

    private static final int CHROMIUM_COL_URL = 1;
    private static final int CHROMIUM_COL_TITLE = 2;
    private static final int CHROMIUM_COL_VISIT_COUNT = 3;
    private static final int CHROMIUM_COL_LAST_VISIT_TIME = 5;

    private static final int FIREFOX_COL_URL = 1;
    private static final int FIREFOX_COL_TITLE = 2;
    private static final int FIREFOX_COL_VISIT_COUNT = 10;
    private static final int FIREFOX_COL_LAST_VISIT_DATE = 15;

    public record Row(String url, String title, long visitTime, int visitCount) {}

    public static List<Row> readChromiumUrls(File dbFile) throws IOException {
        try (MiniSQLiteReader reader = MiniSQLiteReader.open(dbFile)) {
            List<Object[]> rawRows = reader.readTable("urls");
            List<Row> rows = new ArrayList<>(rawRows.size());
            for (Object[] r : rawRows) {
                if (r.length <= CHROMIUM_COL_LAST_VISIT_TIME) continue;
                long lastVisitTime = asLong(r[CHROMIUM_COL_LAST_VISIT_TIME]);
                int visitCount = (int) asLong(r[CHROMIUM_COL_VISIT_COUNT]);
                if (lastVisitTime <= 0 || visitCount <= 0) continue; // matches existing WHERE clause
                rows.add(new Row(
                    asString(r[CHROMIUM_COL_URL]),
                    asString(r[CHROMIUM_COL_TITLE]),
                    lastVisitTime,
                    visitCount
                ));
            }
            return rows;
        }
    }

    public static List<Row> readFirefoxPlaces(File dbFile) throws IOException {
        try (MiniSQLiteReader reader = MiniSQLiteReader.open(dbFile)) {
            List<Object[]> rawRows = reader.readTable("moz_places");
            List<Row> rows = new ArrayList<>(rawRows.size());
            for (Object[] r : rawRows) {
                if (r.length <= FIREFOX_COL_LAST_VISIT_DATE) continue;
                Object lastVisitObj = r[FIREFOX_COL_LAST_VISIT_DATE];
                if (lastVisitObj == null) continue;
                int visitCount = (int) asLong(r[FIREFOX_COL_VISIT_COUNT]);
                if (visitCount <= 0) continue;
                rows.add(new Row(
                    asString(r[FIREFOX_COL_URL]),
                    asString(r[FIREFOX_COL_TITLE]),
                    asLong(lastVisitObj),
                    visitCount
                ));
            }
            return rows;
        }
    }

    private static long asLong(Object o) {
        return switch (o) {
            case Long l -> l;
            case Double d -> d.longValue();
            case null, default -> 0L;
        };
    }

    private static String asString(Object o) {
        return o == null ? "" : o.toString();
    }
}
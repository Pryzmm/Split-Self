package com.pryzmm.splitself.file;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrowserHistoryReader {

    public static class HistoryEntry {
        public String url;
        public String title;
        public long visitTime;
        public int visitCount;
        public String browser;

        public HistoryEntry(String url, String title, long visitTime, int visitCount, String browser) {
            this.url = url;
            this.title = title;
            this.visitTime = visitTime;
            this.visitCount = visitCount;
            this.browser = browser;
        }

        @Override
        public String toString() {
            return String.format("%s/split/%s/split/%s/split/%d", title, url, browser, visitCount);
        }
    }

    public List<HistoryEntry> getRecentHistory(int limit) {
        List<HistoryEntry> history = new ArrayList<>();

        history.addAll(readChromeHistory(limit));
        history.addAll(readFirefoxHistory(limit));
        history.addAll(readOperaGXHistory(limit));

        history.sort((a, b) -> Long.compare(b.visitTime, a.visitTime));

        return history.subList(0, Math.min(limit, history.size()));
    }

    public List<HistoryEntry> getMostVisited(int limit) {
        List<HistoryEntry> history = new ArrayList<>();

        // Debug: Let's see what we're getting from each browser
        List<HistoryEntry> chromeEntries = readChromeHistory(1000); // Get more entries for debugging
        List<HistoryEntry> firefoxEntries = readFirefoxHistory(1000);
        List<HistoryEntry> operaEntries = readOperaGXHistory(1000);

        System.out.println("Debug - Chrome entries: " + chromeEntries.size());
        System.out.println("Debug - Firefox entries: " + firefoxEntries.size());
        System.out.println("Debug - Opera entries: " + operaEntries.size());

        history.addAll(chromeEntries);
        history.addAll(firefoxEntries);
        history.addAll(operaEntries);

        // Debug: Print some visit counts before sorting
        System.out.println("Debug - Total entries before sorting: " + history.size());
        if (!history.isEmpty()) {
            System.out.println("Debug - Sample visit counts:");
            for (int i = 0; i < Math.min(5, history.size()); i++) {
                HistoryEntry entry = history.get(i);
                System.out.println("  " + entry.browser + ": " + entry.title + " (visits: " + entry.visitCount + ")");
            }
        }

        // Fixed sorting: most visited first
        history.sort((a, b) -> Integer.compare(b.visitCount, a.visitCount));

        // Debug: Print top entries after sorting
        System.out.println("Debug - Top entries after sorting:");
        for (int i = 0; i < Math.min(5, history.size()); i++) {
            HistoryEntry entry = history.get(i);
            System.out.println("  " + entry.browser + ": " + entry.title + " (visits: " + entry.visitCount + ")");
        }

        return history.subList(0, Math.min(limit, history.size()));
    }

    private List<HistoryEntry> readChromeHistory(int limit) {
        String historyPath = getChromeHistoryPath();
        System.out.println("Debug - Chrome history path: " + historyPath);
        return new ArrayList<>(readChromiumBasedHistory(historyPath, limit, "Chrome"));
    }

    private List<HistoryEntry> readOperaGXHistory(int limit) {
        String historyPath = getOperaGXHistoryPath();
        System.out.println("Debug - Opera GX history path: " + historyPath);
        return new ArrayList<>(readChromiumBasedHistory(historyPath, limit, "OperaGX"));
    }

    private List<HistoryEntry> readFirefoxHistory(int limit) {
        List<HistoryEntry> entries = new ArrayList<>();
        String historyPath = getFirefoxHistoryPath();
        System.out.println("Debug - Firefox history path: " + historyPath);

        if (historyPath == null) {
            System.out.println("Debug - Firefox history path is null");
            return entries;
        }

        File historyFile = new File(historyPath);
        if (!historyFile.exists()) {
            System.out.println("Firefox history file not found at: " + historyPath);
            return entries;
        }

        String tempPath = copyToTempFile(historyPath, "Firefox");
        String connectionUrl = "jdbc:sqlite:" + (tempPath != null ? tempPath : historyPath);

        try {
            Connection conn = DriverManager.getConnection(connectionUrl);
            // Modified query to get higher visit counts and better data
            String query = "SELECT url, title, visit_count, last_visit_date FROM moz_places " +
                    "WHERE last_visit_date IS NOT NULL AND visit_count > 0 " +
                    "ORDER BY visit_count DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                long firefoxTime = rs.getLong("last_visit_date");
                long compatibleTime = firefoxTime + 11644473600000000L;

                if (title == null || title.trim().isEmpty()) {
                    continue;
                }

                entries.add(new HistoryEntry(
                        rs.getString("url"),
                        rs.getString("title"),
                        compatibleTime,
                        rs.getInt("visit_count"),
                        "Firefox"
                ));
            }

            conn.close();

            if (tempPath != null) {
                new File(tempPath).delete();
            }

        } catch (SQLException e) {
            System.err.println("Error reading Firefox history: " + e.getMessage());
        }

        return entries;
    }

    private List<HistoryEntry> readChromiumBasedHistory(String historyPath, int limit, String browserName) {
        List<HistoryEntry> entries = new ArrayList<>();

        File historyFile = new File(historyPath);
        if (!historyFile.exists()) {
            System.out.println(browserName + " history file not found at: " + historyPath);
            return entries;
        }

        String tempPath = copyToTempFile(historyPath, browserName);
        String connectionUrl = "jdbc:sqlite:" + (tempPath != null ? tempPath : historyPath);

        try {
            Connection conn = DriverManager.getConnection(connectionUrl);
            // Modified query to prioritize by visit count instead of time for most visited
            String query = "SELECT url, title, visit_count, last_visit_time FROM urls " +
                    "WHERE last_visit_time > 0 AND visit_count > 0 " +
                    "ORDER BY visit_count DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(new HistoryEntry(
                        rs.getString("url"),
                        rs.getString("title"),
                        rs.getLong("last_visit_time"),
                        rs.getInt("visit_count"),
                        browserName
                ));
            }

            conn.close();

            if (tempPath != null) {
                new File(tempPath).delete();
            }

        } catch (SQLException e) {
            System.err.println("Error reading " + browserName + " history: " + e.getMessage());
        }

        return entries;
    }

    private String copyToTempFile(String originalPath, String browserName) {
        try {
            File originalFile = new File(originalPath);
            if (!originalFile.exists()) {
                return null;
            }

            File tempFile = File.createTempFile(browserName.toLowerCase() + "_history_", ".sqlite");
            tempFile.deleteOnExit();

            java.nio.file.Files.copy(originalFile.toPath(), tempFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Created temporary copy of " + browserName + " history for safe reading.");
            return tempFile.getAbsolutePath();

        } catch (java.io.IOException e) {
            System.err.println("Could not copy " + browserName + " history file: " + e.getMessage());
            System.err.println("Will attempt to read original file directly.");
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error copying " + browserName + " history: " + e.getMessage());
            return null;
        }
    }

    private String getChromeHistoryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return userHome + "/AppData/Local/Google/Chrome/User Data/Default/History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/Google/Chrome/Default/History";
        } else {
            return userHome + "/.config/google-chrome/Default/History";
        }
    }

    private String getOperaGXHistoryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return userHome + "/AppData/Roaming/Opera Software/Opera GX Stable/History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/com.operasoftware.OperaGX/History";
        } else {
            return userHome + "/.config/opera-gx/History";
        }
    }

    private String getFirefoxHistoryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String profilesPath = "";

        if (os.contains("win")) {
            profilesPath = userHome + "/AppData/Roaming/Mozilla/Firefox/Profiles";
        } else if (os.contains("mac")) {
            profilesPath = userHome + "/Library/Application Support/Firefox/Profiles";
        } else {
            profilesPath = userHome + "/.mozilla/firefox";
        }

        File profilesDir = new File(profilesPath);
        if (profilesDir.exists() && profilesDir.isDirectory()) {
            File[] profiles = profilesDir.listFiles();
            if (profiles != null) {
                for (File profile : profiles) {
                    if (profile.isDirectory() &&
                            (profile.getName().contains("default") || profile.getName().contains("release"))) {
                        File placesFile = new File(profile, "places.sqlite");
                        if (placesFile.exists()) {
                            return placesFile.getAbsolutePath();
                        }
                    }
                }
            }
        }

        return null;
    }

    public static List<HistoryEntry> getHistory() {
        BrowserHistoryReader reader = new BrowserHistoryReader();
        return reader.getRecentHistory(10);
    }

    public static List<HistoryEntry> getMostVisited() {
        BrowserHistoryReader reader = new BrowserHistoryReader();
        return reader.getMostVisited(10);
    }
}
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

        history.addAll(readChromeHistory(limit, "last_visit_time"));
        history.addAll(readBraveHistory(limit, "last_visit_time"));
        history.addAll(readFirefoxHistory(limit, "last_visit_date"));
        history.addAll(readOperaGXHistory(limit, "last_visit_time"));

        history.sort((a, b) -> Long.compare(b.visitTime, a.visitTime));

        return history.subList(0, Math.min(limit, history.size()));
    }

    public List<HistoryEntry> getMostVisited(int limit) {
        List<HistoryEntry> history = new ArrayList<>();

        List<HistoryEntry> chromeEntries = readChromeHistory(100, "visit_count");
        List<HistoryEntry> braveEntries = readBraveHistory(100, "visit_count");
        List<HistoryEntry> firefoxEntries = readFirefoxHistory(100, "visit_count");
        List<HistoryEntry> operaEntries = readOperaGXHistory(100, "visit_count");

        history.addAll(chromeEntries);
        history.addAll(braveEntries);
        history.addAll(firefoxEntries);
        history.addAll(operaEntries);

        if (!history.isEmpty()) {
            for (int i = 0; i < Math.min(5, history.size()); i++) {
                HistoryEntry entry = history.get(i);
            }
        }

        history.sort((a, b) -> Integer.compare(b.visitCount, a.visitCount));

        System.out.println("Debug - Top entries after sorting:");
        for (int i = 0; i < Math.min(5, history.size()); i++) {
            HistoryEntry entry = history.get(i);
            System.out.println("  " + entry.browser + ": " + entry.title + " (visits: " + entry.visitCount + ")");
        }

        return history.subList(0, Math.min(limit, history.size()));
    }

    private List<HistoryEntry> readChromeHistory(int limit, String sort) {
        String historyPath = getChromeHistoryPath();
        System.out.println("Debug - Chrome history path: " + historyPath);
        return new ArrayList<>(readChromiumBasedHistory(historyPath, limit, "Chrome", sort));
    }

    private List<HistoryEntry> readBraveHistory(int limit, String sort) {
        String historyPath = getBraveHistoryPath();
        System.out.println("Debug - Brave history path: " + historyPath);
        return new ArrayList<>(readChromiumBasedHistory(historyPath, limit, "Brave", sort));
    }

    private List<HistoryEntry> readOperaGXHistory(int limit, String sort) {
        String historyPath = getOperaGXHistoryPath();
        System.out.println("Debug - Opera GX history path: " + historyPath);
        return new ArrayList<>(readChromiumBasedHistory(historyPath, limit, "OperaGX", sort));
    }

    private List<HistoryEntry> readFirefoxHistory(int limit, String sort) {
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
            String query = "SELECT url, title, visit_count, last_visit_date FROM moz_places " +
                    "WHERE last_visit_date IS NOT NULL AND visit_count > 0 " +
                    "ORDER BY " + sort + " DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                title = replaceTitle(title);
                long firefoxTime = rs.getLong("last_visit_date");
                long compatibleTime = firefoxTime + 11644473600000000L;

                if (title == null || title.trim().isEmpty()) {
                    continue;
                } else if (title.contains("GX Corner") || title.contains("New Tab") || title.equals("Home")) { // Removing startup pages from the list
                    continue;
                }

                entries.add(new HistoryEntry(
                        rs.getString("url"),
                        title,
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

    private List<HistoryEntry> readChromiumBasedHistory(String historyPath, int limit, String browserName, String sort) {
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
                    "ORDER BY " + sort + " DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                title = replaceTitle(title);
                entries.add(new HistoryEntry(
                        rs.getString("url"),
                        title,
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

    private String getBraveHistoryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return userHome + "/AppData/Local/BraveSoftware/Brave-Browser/User Data/Default/History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/BraveSoftware/Brave-Browser/Default/History";
        } else {
            return userHome + "/.config/BraveSoftware/Brave-Browser/Default/History";
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

    private String replaceTitle(String title) {
        title = title.toLowerCase().contains("inbox (") ? "Email" : title;
        title = title.toLowerCase().contains("amazon") ? "Amazon" : title;
        title = title.toLowerCase().contains("youtube") ? "YouTube" : title;
        title = title.toLowerCase().contains("split self") ? "this Minecraft mod on Modrinth" : title;
        title = title.toLowerCase().contains("split-self") ? "this Minecraft mod on Github" : title;
        return title;
    }
}
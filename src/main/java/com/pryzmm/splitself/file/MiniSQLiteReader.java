package com.pryzmm.splitself.file;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class MiniSQLiteReader implements Closeable {
    private final RandomAccessFile db;
    private final Map<Integer, byte[]> walPages;
    private final int pageSize, reservedSpace;

    private MiniSQLiteReader(RandomAccessFile db, Map<Integer, byte[]> walPages, int pageSize, int reservedSpace) {
        this.db = db; this.walPages = walPages; this.pageSize = pageSize; this.reservedSpace = reservedSpace;
    }

    public static MiniSQLiteReader open(File dbFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(dbFile, "r");
        byte[] header = new byte[100];
        raf.seek(0); raf.readFully(header);
        if (!new String(header, 0, 16, StandardCharsets.US_ASCII).equals("SQLite format 3\u0000")) {
            raf.close(); throw new IOException("Not a SQLite v3 database: " + dbFile);
        }
        int pageSize = ((header[16] & 0xFF) << 8) | (header[17] & 0xFF);
        if (pageSize == 1) pageSize = 65536;
        int reserved = header[20] & 0xFF;
        File walFile = new File(dbFile.getParentFile(), dbFile.getName() + "-wal");
        Map<Integer, byte[]> walPages = walFile.exists() ? readWal(walFile, pageSize) : Collections.emptyMap();
        return new MiniSQLiteReader(raf, walPages, pageSize, reserved);
    }

    public List<Object[]> readTable(String tableName) throws IOException {
        int rootPage = findTableRootPage(tableName);
        if (rootPage == -1) throw new IOException("Table not found in sqlite_master: " + tableName);
        List<Object[]> rows = new ArrayList<>();
        walkTablePage(rootPage, rows);
        return rows;
    }

    @Override public void close() throws IOException { db.close(); }

    private static Map<Integer, byte[]> readWal(File walFile, int expectedPageSize) throws IOException {
        byte[] all = Files.readAllBytes(walFile.toPath());
        if (all.length < 32) return Collections.emptyMap();
        int magic = readInt32BE(all, 0);
        if (magic != 0x377f0682 && magic != 0x377f0683) return Collections.emptyMap();
        int walPageSize = readInt32BE(all, 8);
        if (walPageSize != expectedPageSize) return Collections.emptyMap();

        final int FRAME_HEADER = 24, WAL_HEADER = 32;
        Map<Integer, byte[]> committed = new LinkedHashMap<>(), pending = new LinkedHashMap<>();
        int offset = WAL_HEADER;
        while (offset + FRAME_HEADER + walPageSize <= all.length) {
            int pageNumber = readInt32BE(all, offset);
            int commitSize = readInt32BE(all, offset + 4);
            pending.put(pageNumber, Arrays.copyOfRange(all, offset + FRAME_HEADER, offset + FRAME_HEADER + walPageSize));
            if (commitSize != 0) { committed.putAll(pending); pending.clear(); }
            offset += FRAME_HEADER + walPageSize;
        }
        return committed;
    }

    private static int readInt32BE(byte[] b, int off) {
        return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16) | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
    }

    private byte[] readPage(int pageNumber) throws IOException {
        byte[] fromWal = walPages.get(pageNumber);
        if (fromWal != null) return fromWal;
        byte[] page = new byte[pageSize];
        db.seek((long) (pageNumber - 1) * pageSize);
        db.readFully(page);
        return page;
    }

    private int findTableRootPage(String tableName) throws IOException {
        List<Object[]> masterRows = new ArrayList<>();
        walkTablePage(1, masterRows);
        for (Object[] row : masterRows)
            if (row.length >= 4 && "table".equals(row[0]) && tableName.equals(row[1]) && row[3] instanceof Long)
                return ((Long) row[3]).intValue();
        return -1;
    }

    private void walkTablePage(int pageNumber, List<Object[]> out) throws IOException {
        byte[] page = readPage(pageNumber);
        int base = (pageNumber == 1) ? 100 : 0;
        int pageType = page[base] & 0xFF;
        int cellCount = ((page[base + 3] & 0xFF) << 8) | (page[base + 4] & 0xFF);
        int cellPointerArrayStart = base + ((pageType == 0x05 || pageType == 0x02) ? 12 : 8);

        switch (pageType) {
            case 0x0D -> {
                for (int i = 0; i < cellCount; i++)
                    out.add(readLeafCell(page, readUint16(page, cellPointerArrayStart + i * 2)));
            }
            case 0x05 -> {
                for (int i = 0; i < cellCount; i++)
                    walkTablePage(readInt32BEUnsigned(page, readUint16(page, cellPointerArrayStart + i * 2)), out);
                walkTablePage(readInt32BEUnsigned(page, base + 8), out);
            }
            default -> throw new IOException("Unsupported page type 0x" + Integer.toHexString(pageType) + " at page " + pageNumber + " (only rowid table b-tree pages are supported)");
        }
    }

    private Object[] readLeafCell(byte[] page, int cellPtr) throws IOException {
        int pos = cellPtr;
        long[] payloadLenResult = readVarint(page, pos);
        long payloadLength = payloadLenResult[0];
        pos += (int) payloadLenResult[1];
        pos += (int) readVarint(page, pos)[1];

        int usable = pageSize - reservedSpace;
        int maxLocal = usable - 35;
        int minLocal = (usable - 12) * 32 / 255 - 23;

        byte[] payload;
        if (payloadLength <= maxLocal) {
            payload = Arrays.copyOfRange(page, pos, pos + (int) payloadLength);
        } else {
            long k = minLocal + (payloadLength - minLocal) % (usable - 4);
            int local = (k <= maxLocal) ? (int) k : minLocal;
            payload = new byte[(int) payloadLength];
            System.arraycopy(page, pos, payload, 0, local);
            int overflowPageNum = readInt32BEUnsigned(page, pos + local);

            int written = local, remaining = (int) payloadLength - local;
            Set<Integer> visited = new HashSet<>();
            while (remaining > 0) {
                if (overflowPageNum == 0) throw new IOException("Overflow chain ended early: " + remaining + " bytes still expected");
                if (!visited.add(overflowPageNum)) throw new IOException("Overflow page chain loop detected at page " + overflowPageNum);
                byte[] overflowPage = readPage(overflowPageNum);
                int nextPage = readInt32BEUnsigned(overflowPage, 0);
                int chunk = Math.min(remaining, usable - 4);
                System.arraycopy(overflowPage, 4, payload, written, chunk);
                written += chunk; remaining -= chunk; overflowPageNum = nextPage;
            }
        }
        return decodeRecord(payload);
    }

    private Object[] decodeRecord(byte[] payload) {
        long[] headerLenResult = readVarint(payload, 0);
        int headerLength = (int) headerLenResult[0];
        int headerVarintSize = (int) headerLenResult[1];

        List<Long> serialTypes = new ArrayList<>();
        int hp = headerVarintSize;
        while (hp < headerLength) {
            long[] st = readVarint(payload, hp);
            serialTypes.add(st[0]);
            hp += (int) st[1];
        }

        Object[] values = new Object[serialTypes.size()];
        int bodyPos = headerLength;
        for (int i = 0; i < serialTypes.size(); i++) {
            long serialType = serialTypes.get(i);
            values[i] = decodeValue(payload, bodyPos, serialType);
            bodyPos += serialTypeSize(serialType);
        }
        return values;
    }

    private static int serialTypeSize(long serialType) {
        if (serialType == 0 || serialType == 8 || serialType == 9) return 0; // NULL, 0, 1
        if (serialType >= 1 && serialType <= 6)
            return switch ((int) serialType) { case 1 -> 1; case 2 -> 2; case 3 -> 3; case 4 -> 4; case 5 -> 6; case 6 -> 8; default -> 0; };
        if (serialType == 7) return 8;
        if (serialType >= 12 && serialType % 2 == 0) return (int) ((serialType - 12) / 2);
        if (serialType >= 13) return (int) ((serialType - 13) / 2);
        return 0;
    }

    private Object decodeValue(byte[] payload, int pos, long serialType) {
        if (serialType == 0) return null;
        if (serialType >= 1 && serialType <= 6) return readSignedBigEndian(payload, pos, serialTypeSize(serialType));
        if (serialType == 7) return Double.longBitsToDouble(readSignedBigEndian(payload, pos, 8));
        if (serialType == 8) return 0L;
        if (serialType == 9) return 1L;
        if (serialType >= 12 && serialType % 2 == 0) return Arrays.copyOfRange(payload, pos, pos + serialTypeSize(serialType));
        if (serialType >= 13) return new String(payload, pos, serialTypeSize(serialType), StandardCharsets.UTF_8);
        return null;
    }

    private static int readUint16(byte[] b, int off) { return ((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF); }

    private static int readInt32BEUnsigned(byte[] b, int off) {
        return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16) | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
    }

    private static long readSignedBigEndian(byte[] b, int off, int len) {
        long v = 0;
        for (int i = 0; i < len; i++) v = (v << 8) | (b[off + i] & 0xFF);
        int bits = len * 8;
        if (bits < 64 && (v & (1L << (bits - 1))) != 0) v -= (1L << bits);
        return v;
    }

    private static long[] readVarint(byte[] b, int off) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            byte cur = b[off + i];
            result = (result << 7) | (cur & 0x7F);
            if ((cur & 0x80) == 0) return new long[]{result, i + 1};
        }
        return new long[]{(result << 8) | (b[off + 8] & 0xFF), 9};
    }
}
package com.MagicalStay.shared.data;

import java.io.*;
import java.util.*;

public class IndexFile {
    private RandomAccessFile indexFile;
    private static final int KEY_SIZE = 50;
    private static final int POSITION_SIZE = 8;
    private static final int RECORD_SIZE = KEY_SIZE + POSITION_SIZE;

    public IndexFile(String filename) throws IOException {
        this.indexFile = new RandomAccessFile(filename, "rw");
    }

    public void addIndex(String key, long position) throws IOException {
        indexFile.seek(indexFile.length());
        writeKey(key);
        indexFile.writeLong(position);
    }

    public long findPosition(String key) throws IOException {
        for (long pos = 0; pos < indexFile.length(); pos += RECORD_SIZE) {
            indexFile.seek(pos);
            String currentKey = readKey();
            if (currentKey.trim().equals(key)) {
                return indexFile.readLong();
            }
        }
        return -1;
    }

    private void writeKey(String key) throws IOException {
        StringBuilder paddedKey = new StringBuilder(key);
        while (paddedKey.length() < KEY_SIZE) {
            paddedKey.append(' ');
        }
        indexFile.writeChars(paddedKey.substring(0, KEY_SIZE));
    }

    private String readKey() throws IOException {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < KEY_SIZE; i++) {
            key.append(indexFile.readChar());
        }
        return key.toString().trim();
    }

    public void close() throws IOException {
        if (indexFile != null) {
            indexFile.close();
        }
    }
}
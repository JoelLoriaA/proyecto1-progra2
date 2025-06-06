package com.MagicalStay.server;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class FileTransferService {

    public static byte[] compressFile(File file) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            zos.putNextEntry(new ZipEntry(file.getName()));
            Files.copy(file.toPath(), zos);
            zos.closeEntry();

            return baos.toByteArray();
        }
    }

    public static void decompressFile(byte[] data, Path destPath) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ZipInputStream zis = new ZipInputStream(bais)) {

            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                Files.copy(zis, destPath.resolve(entry.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}

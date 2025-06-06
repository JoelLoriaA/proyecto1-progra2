
package com.MagicalStay.server;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileService {
    private static final String FILES_DIR = "C:\\Users\\Admin\\Documents\\ULTIMA FASE\\server\\";

    static {
        try {
            Files.createDirectories(Paths.get(FILES_DIR));
        } catch (IOException e) {
            System.err.println("No se pudo crear el directorio de archivos: " + e.getMessage());
        }
    }

    public static void guardarArchivo(String nombre, byte[] datos) throws IOException {
        Files.write(Paths.get(FILES_DIR, nombre), datos);
    }

    public static byte[] leerArchivo(String nombre) throws IOException {
        return Files.readAllBytes(Paths.get(FILES_DIR, nombre));
    }

    public static List<String> listarArchivos() {
        try {
            File dir = new File(FILES_DIR);
            String[] archivos = dir.list();
            return archivos != null ? Arrays.asList(archivos) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
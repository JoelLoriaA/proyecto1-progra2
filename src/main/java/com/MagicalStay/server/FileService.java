
package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class FileService {
    private static final String FILES_DIR = "D:\\JAVA_DEV\\progra2-2025\\ULTIMA FASE\\server\\";

    static {
        try {
            Files.createDirectories(Paths.get(FILES_DIR));
            Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR));
        } catch (IOException e) {
            System.err.println("No se pudieron crear los directorios: " + e.getMessage());
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

    public static void sincronizarArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
        Path ruta = Paths.get(esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : FILES_DIR, nombre);
        Files.write(ruta, datos, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
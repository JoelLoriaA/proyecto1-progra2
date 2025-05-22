package com.MagicalStay.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataConfig {

    private static final String DATA_DIRECTORY = "data";

    public static final String HOTELS_FILE = "hotels.dat";
    public static final String ROOMS_FILE = "rooms.dat";
    public static final String GUESTS_FILE = "guests.dat";
    public static final String BOOKINGS_FILE = "bookings.dat";
    public static final String FRONT_DESK_FILE = "frontdesk.dat";

    static {

        createDataDirectory();
    }


    private static void createDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIRECTORY);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                System.out.println("Directorio de datos creado: " + dataPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static String getDataFilePath(String filename) {
        return DATA_DIRECTORY + File.separator + filename;
    }


    public static String getDataDirectory() {
        return DATA_DIRECTORY;
    }

    public static boolean dataDirectoryExists() {
        File dir = new File(DATA_DIRECTORY);
        return dir.exists() && dir.isDirectory();
    }

    public static void clearAllDataFiles() {
        try {
            deleteFileIfExists(getDataFilePath(HOTELS_FILE));
            deleteFileIfExists(getDataFilePath(ROOMS_FILE));
            deleteFileIfExists(getDataFilePath(GUESTS_FILE));
            deleteFileIfExists(getDataFilePath(BOOKINGS_FILE));
            deleteFileIfExists(getDataFilePath(FRONT_DESK_FILE));
            System.out.println("Archivos de datos limpiados");
        } catch (Exception e) {
            System.err.println("Error al limpiar archivos de datos: " + e.getMessage());
        }
    }

    private static void deleteFileIfExists(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void listDataFiles() {
        File dataDir = new File(DATA_DIRECTORY);
        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles();
            if (files != null) {
                System.out.println("Archivos en el directorio de datos:");
                for (File file : files) {
                    if (file.isFile()) {
                        System.out.println("- " + file.getName() + " (" + file.length() + " bytes)");
                    }
                }
            }
        }
    }
}
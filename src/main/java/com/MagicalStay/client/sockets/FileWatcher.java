package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileWatcher implements Runnable {
        private final FileClient fileClient;
        private final String[] directorios = {
            ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
            ConfiguracionApp.RUTA_IMAGENES_SERVIDOR,
            ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR
        };

    private final Map<String, Long> archivosIgnorados = new ConcurrentHashMap<>();
    private static final long IGNORAR_MS = 1500;

        public FileWatcher(FileClient fileClient) {
            this.fileClient = fileClient;
        }

    public void ignorarArchivo(String ruta) {
        archivosIgnorados.put(ruta, System.currentTimeMillis() + IGNORAR_MS);
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            for (String dir : directorios) {
                Paths.get(dir).register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            }
            while (true) {
                WatchKey key = watcher.take();
                Path dir = (Path) key.watchable();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = dir.resolve((Path) event.context());
                    String rutaAbsoluta = changed.toAbsolutePath().toString();

                    // Ignorar si está en el mapa y no ha expirado
                    Long hasta = archivosIgnorados.get(rutaAbsoluta);
                    if (hasta != null) {
                        if (System.currentTimeMillis() < hasta) {
                            continue;
                        } else {
                            archivosIgnorados.remove(rutaAbsoluta);
                        }
                    }

                    File archivo = changed.toFile();
                    if (archivo.isFile()) {
                        boolean esImagen = dir.toString().contains("images");
                        byte[] datos = Files.readAllBytes(archivo.toPath());
                        fileClient.subirArchivo(archivo.getName(), datos, esImagen);
                        System.out.println("Archivo sincronizado: " + archivo.getName());
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            System.err.println("Error en FileWatcher: " + e.getMessage());
        }
    }
    }
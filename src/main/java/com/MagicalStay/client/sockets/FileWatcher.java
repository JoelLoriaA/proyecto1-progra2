
package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileWatcher implements Runnable {
    private final FileClient fileClient;
    private final String[] directorios = {
        ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
        ConfiguracionApp.RUTA_IMAGENES_SERVIDOR,
        ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR
    };

    public FileWatcher(FileClient fileClient) {
        this.fileClient = fileClient;
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
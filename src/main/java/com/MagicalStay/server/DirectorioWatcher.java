
package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;
import java.nio.file.*;
import java.util.List;

public class DirectorioWatcher implements Runnable {
    private final List<ClientHandler> clientes;

    public DirectorioWatcher(List<ClientHandler> clientes) {
        this.clientes = clientes;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR).register(
                watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY
            );
            Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR).register(
                watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY
            );
            Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR).register(
                watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY
            );

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    String fileName = event.context().toString();
                    String tipoEvento = event.kind().name();
                    synchronized (clientes) {
                        // Suponiendo que tienes una lista de ClientHandler llamada clientes
                        for (ClientHandler cliente : clientes) {
                            if (!cliente.estaSincronizando()) {
                                cliente.enviarMensaje("ARCHIVO_CAMBIADO|...");
                            }
                        }
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            System.err.println("Error en DirectorioWatcher: " + e.getMessage());
        }
    }
}

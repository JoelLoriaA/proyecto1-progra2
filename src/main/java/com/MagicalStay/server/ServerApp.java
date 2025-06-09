package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final int PUERTO_POR_DEFECTO = 5000;
    private final ServerSocket serverSocket;
    private final ExecutorService poolDeHilos;
    private volatile boolean ejecutando;

    public ServerApp(int puerto) throws IOException {
        Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR));
        Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR));
        Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR));

        serverSocket = new ServerSocket(puerto);
        poolDeHilos = Executors.newCachedThreadPool();
        System.out.println("Servidor iniciado en puerto " + puerto);

        System.out.println("Directorios inicializados:");
        System.out.println("- Archivos: " + ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR);
        System.out.println("- Imágenes: " + ConfiguracionApp.RUTA_IMAGENES_SERVIDOR);
        System.out.println("- Copia de Imágenes: " + ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR);
    }

    public void iniciarWatcher() {
        new Thread(() -> {
            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                Path archivosPath = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR);
                Path imagenesPath = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR);
                Path copiaImagenesPath = Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR);

                archivosPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                imagenesPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                copiaImagenesPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                while (ejecutando) {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path context = (Path) event.context();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        // Determinar qué directorio fue afectado
                        Path dir = (Path) key.watchable();
                        String directorioAfectado = "";
                        if (dir.equals(archivosPath)) {
                            directorioAfectado = ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;
                        } else if (dir.equals(imagenesPath)) {
                            directorioAfectado = ConfiguracionApp.RUTA_IMAGENES_SERVIDOR;
                        } else if (dir.equals(copiaImagenesPath)) {
                            directorioAfectado = ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR;
                        }

                        // Construir el mensaje de notificación
                        String nombreArchivo = context.toString();
                        String mensaje = "NOTIFICACION|archivo_modificado|" + nombreArchivo;

                        // Notificar a todos los clientes
                        ClientHandler.notificarTodos(mensaje);
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en watcher: " + e.getMessage());
            }
        }).start();
    }

    public void iniciar() {
        ejecutando = true;
        iniciarWatcher();

        while (ejecutando) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                poolDeHilos.execute(handler);
            } catch (IOException e) {
                if (ejecutando) {
                    System.err.println("Error aceptando conexión: " + e.getMessage());
                }
            }
        }
    }

    public void detener() {
        ejecutando = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            poolDeHilos.shutdown();
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int puerto = PUERTO_POR_DEFECTO;
        try {
            ServerApp servidor = new ServerApp(puerto);
            Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
            servidor.iniciar();
        } catch (IOException e) {
            System.err.println("Error fatal: " + e.getMessage());
            System.exit(1);
        }
    }
}
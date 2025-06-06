package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private volatile boolean ejecutando = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

   @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());

            enviarMensaje("WELCOME|Conectado al servidor MagicalStay");

            while (ejecutando && !socket.isClosed()) {
                try {
                    Object mensaje = entrada.readObject();
                    if (mensaje instanceof String) {
                        String comandoStr = (String) mensaje;
                        handleMessage(comandoStr);
                    } else {
                        System.out.println("Mensaje no reconocido: " + mensaje.getClass());
                    }
                } catch (EOFException e) {
                    break; // Conexión cerrada normalmente
                } catch (IOException | ClassNotFoundException e) {
                    if (ejecutando) {
                        System.err.println("Error procesando mensaje: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error en ClientHandler: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private void handleMessage(String mensaje) throws IOException {
        try {
            if (mensaje.equals("listar_archivos")) {
                enviarListaArchivos();
            } else if (mensaje.startsWith("subir_archivo|") || mensaje.startsWith("subir_imagen|")) {
                recibirArchivo(mensaje);
            } else {
                procesarComandoEstandar(mensaje);
            }
        } catch (Exception e) {
            enviarMensaje("Error: " + e.getMessage());
        }
    }

    private void enviarListaArchivos() throws IOException {
        List<Path> archivos = new ArrayList<>();

        // Recolectar archivos de ambos directorios
        try (Stream<Path> archivosNormales = Files.walk(Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR))) {
            archivosNormales
                .filter(Files::isRegularFile)
                .forEach(archivos::add);
        }

        try (Stream<Path> imagenes = Files.walk(Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR))) {
            imagenes
                .filter(Files::isRegularFile)
                .forEach(archivos::add);
        }

        // Enviar conteo total
        enviarMensaje("FILE_COUNT|" + archivos.size());

        // Enviar cada archivo
        for (Path archivo : archivos) {
            String tipo = archivo.toString().contains("images") ? "imagen" : "archivo";
            String nombre = archivo.getFileName().toString();

            // Enviar metadata
            enviarMensaje(tipo + "|" + nombre);

            // Enviar contenido
            byte[] datos = Files.readAllBytes(archivo);
            salida.writeObject(datos);
            salida.flush();
        }

        enviarMensaje("Lista de archivos enviada");
    }

  private void recibirArchivo(String comando) throws IOException {
        try {
            String[] partes = comando.split("\\|");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Formato de comando inválido");
            }

            String tipo = partes[0];
            String nombre = partes[1];

            int tamanoTotal = entrada.readInt();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(tamanoTotal);
            byte[] buffer = new byte[8192];

            while (true) {
                int tamanoChunk = entrada.readInt();
                if (tamanoChunk == -1) break;

                int bytesLeidos = 0;
                while (bytesLeidos < tamanoChunk) {
                    int leidos = entrada.read(buffer, 0, Math.min(buffer.length, tamanoChunk - bytesLeidos));
                    if (leidos == -1) break;
                    baos.write(buffer, 0, leidos);
                    bytesLeidos += leidos;
                }
            }

            String rutaBase = tipo.equals("subir_imagen") ?
                ConfiguracionApp.RUTA_IMAGENES_SERVIDOR :
                ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;

            Path rutaDestino = Paths.get(rutaBase, nombre);
            Files.createDirectories(rutaDestino.getParent());
            Files.write(rutaDestino, baos.toByteArray());

            enviarMensaje("Archivo recibido: " + nombre);
        } catch (Exception e) {
            String error = "Error al recibir archivo: " + e.getMessage();
            System.err.println(error);
            enviarMensaje("Error: " + error);
        }
    }

    private void procesarComandoEstandar(String comando) throws IOException {
        // Procesar otros comandos estándar aquí
        enviarMensaje("Comando procesado: " + comando);
    }

    private void enviarMensaje(String mensaje) throws IOException {
        if (salida != null && !socket.isClosed()) {
            salida.writeObject(mensaje);
            salida.flush();
        }
    }

    private void cerrarConexion() {
        ejecutando = false;
        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
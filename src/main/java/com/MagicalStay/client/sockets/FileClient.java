package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileClient {
    private static final int BUFFER_SIZE = 8192;
    private final SocketCliente socketCliente;
    private List<String> archivosEnviados = new ArrayList<>();
    private volatile boolean sincronizando = false;

    public FileClient(SocketCliente socketCliente) {
        this.socketCliente = socketCliente;
        crearDirectorios();
    }

    private void crearDirectorios() {
        try {
            Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR));
            Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR));
            Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR));
        } catch (IOException e) {
            System.err.println("Error al crear directorios: " + e.getMessage());
        }
    }

    public void sincronizarBidireccional() throws IOException, ClassNotFoundException {
        if (sincronizando) return;
        sincronizando = true;

        try {
            // 1. Solicitar lista de archivos al servidor
            socketCliente.enviarMensaje("listar_archivos");

            // 2. Recibir mensaje de bienvenida y respuesta FILE_COUNT
            String respuesta = (String) socketCliente.recibirObjeto();
            if (!respuesta.startsWith("FILE_COUNT|")) {
                // Si es mensaje de bienvenida, leer siguiente mensaje
                respuesta = (String) socketCliente.recibirObjeto();
                if (!respuesta.startsWith("FILE_COUNT|")) {
                    throw new IOException("Respuesta inesperada del servidor: " + respuesta);
                }
            }

            // 3. Procesar cantidad de archivos
            String[] partes = respuesta.split("\\|");
            int cantidadArchivos = Integer.parseInt(partes[1]);
            System.out.println("Se recibirán " + cantidadArchivos + " archivos");

            // 4. Recibir archivos del servidor
            for (int i = 0; i < cantidadArchivos; i++) {
                String infoArchivo = (String) socketCliente.recibirObjeto();
                partes = infoArchivo.split("\\|");
                String tipo = partes[0];
                String nombre = partes[1];

                byte[] datos = (byte[]) socketCliente.recibirObjeto();
                Path rutaDestino;

                if (tipo.equals("imagen")) {
                    rutaDestino = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, nombre);
                    if (Files.exists(rutaDestino)) {
                        System.out.println("Imagen ya existe en el directorio principal: " + rutaDestino);
                        continue;
                    }
                } else {
                    rutaDestino = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombre);
                }

                Files.createDirectories(rutaDestino.getParent());
                Files.write(rutaDestino, datos);
            }
            System.out.println("Recibidos " + cantidadArchivos + " archivos del servidor");

            // 5. Enviar archivos locales al servidor
            System.out.println("Enviando archivos locales al servidor...");
            enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, false);
            enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, true);

        } finally {
            sincronizando = false;
        }
    }

    public void subirArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
        String comando = esImagen ? "subir_imagen" : "subir_archivo";
        socketCliente.enviarMensaje(comando + "|" + nombre);
        socketCliente.enviarObjeto(datos); // Enviar datos completos sin dividir en chunks
    }

    private void enviarArchivosDesdeDirectorio(String directorio, boolean sonImagenes) throws IOException {
        File dir = new File(directorio);
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] archivos = dir.listFiles();
        if (archivos == null) return;

        for (File archivo : archivos) {
            if (!archivo.isFile()) continue;

            String nombre = archivo.getName();
            if (archivoYaEnviado(nombre, archivo)) continue;

            try {
                byte[] datos = Files.readAllBytes(archivo.toPath());
                subirArchivo(nombre, datos, sonImagenes);
                if (sonImagenes) {
                    System.out.println("Enviada imagen: " + nombre);
                } else {
                    System.out.println("Enviado archivo: " + nombre);
                }
                archivosEnviados.add(obtenerIdentificadorArchivo(nombre, archivo));
            } catch (IOException e) {
                System.err.println("Error al enviar archivo " + nombre + ": " + e.getMessage());
            }
        }
    }

    private boolean archivoYaEnviado(String nombre, File archivo) {
        return archivosEnviados.contains(obtenerIdentificadorArchivo(nombre, archivo));
    }

    private String obtenerIdentificadorArchivo(String nombre, File archivo) {
        return nombre + "_" + archivo.length() + "_" + archivo.lastModified();
    }
}
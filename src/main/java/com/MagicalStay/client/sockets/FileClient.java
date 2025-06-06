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
            // Primero recibir archivos del servidor
            socketCliente.enviarMensaje("listar_archivos");
            String respuesta = (String) socketCliente.recibirObjeto();

            if (respuesta.startsWith("FILE_COUNT|")) {
                int cantidadArchivos = Integer.parseInt(respuesta.split("\\|")[1]);
                System.out.println("Se recibirán " + cantidadArchivos + " archivos");

                // Recibir archivos
                for (int i = 0; i < cantidadArchivos; i++) {
                    String fileInfo = (String) socketCliente.recibirObjeto();
                    String[] partes = fileInfo.split("\\|");
                    String tipo = partes[0];
                    String nombre = partes[1];

                    byte[] datos = (byte[]) socketCliente.recibirObjeto();
                    Path rutaDestino = Paths.get(
                            tipo.equals("imagen") ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                            nombre
                    );

                    // Solo guardar si el archivo no existe
                    if (!Files.exists(rutaDestino)) {
                        Files.createDirectories(rutaDestino.getParent());
                        Files.write(rutaDestino, datos);
                        System.out.println(tipo + " recibido: " + nombre);
                    } else {
                        System.out.println(tipo + " ya existe en el directorio principal: " + rutaDestino);
                    }
                }
                System.out.println("Recibidos " + cantidadArchivos + " archivos del servidor");
            }

            // Enviar solo archivos .dat
            System.out.println("Enviando archivos locales al servidor...");
            enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, false);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error durante la sincronización: " + e.getMessage());
            throw e;
        } finally {
            sincronizando = false;
        }
    }

    public void subirArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
        String comando = esImagen ? "subir_imagen" : "subir_archivo";
        socketCliente.enviarMensaje(comando + "|" + nombre);
        socketCliente.enviarObjeto(datos);
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
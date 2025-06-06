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

            if (!respuesta.startsWith("FILE_COUNT|")) {
                throw new IOException("Respuesta inesperada del servidor: " + respuesta);
            }

            String[] partes = respuesta.split("\\|");
            if (partes.length != 2) {
                throw new IOException("Formato de respuesta inválido");
            }

            try {
                int cantidadArchivos = Integer.parseInt(partes[1].trim());
                System.out.println("Se recibirán " + cantidadArchivos + " archivos");

                for (int i = 0; i < cantidadArchivos; i++) {
                    String fileInfo = (String) socketCliente.recibirObjeto();
                    partes = fileInfo.split("\\|");
                    if (partes.length != 2) {
                        throw new IOException("Formato de archivo inválido: " + fileInfo);
                    }

                    String tipo = partes[0];
                    String nombre = partes[1];

                    Object datosObj = socketCliente.recibirObjeto();
                    if (!(datosObj instanceof byte[])) {
                        throw new IOException("Tipo de datos inválido para " + nombre);
                    }

                    byte[] datos = (byte[]) datosObj;
                    String rutaBase = tipo.equals("imagen") ?
                        ConfiguracionApp.RUTA_IMAGENES_SERVIDOR :
                        ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;

                    Path rutaDestino = Paths.get(rutaBase, nombre);
                    Files.createDirectories(rutaDestino.getParent());
                    Files.write(rutaDestino, datos);
                    System.out.println("Recibido " + tipo + ": " + nombre);
                }

                // Esperar mensaje de confirmación
                String confirmacion = (String) socketCliente.recibirObjeto();
                System.out.println(confirmacion);

                // Enviar archivos locales
                Thread.sleep(1000); // Pausa antes de enviar
                enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, false);
                enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, true);

            } catch (NumberFormatException e) {
                throw new IOException("Cantidad de archivos inválida: " + partes[1]);
            }

        } catch (Exception e) {
            System.err.println("Error durante la sincronización: " + e.getMessage());
            e.printStackTrace();
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
            if (!archivo.isFile() || !esArchivoValido(archivo, sonImagenes)) continue;

            String nombre = archivo.getName();
            if (archivoYaEnviado(nombre, archivo)) continue;

            try {
                byte[] datos = Files.readAllBytes(archivo.toPath());
                System.out.println((sonImagenes ? "Enviando imagen: " : "Enviando archivo: ") + nombre);
                subirArchivo(nombre, datos, sonImagenes);
                archivosEnviados.add(obtenerIdentificadorArchivo(nombre, archivo));

                // Esperar confirmación del servidor
                Thread.sleep(500); // Pequeña pausa entre archivos

            } catch (Exception e) {
                System.err.println("Error al enviar " + nombre + ": " + e.getMessage());
            }
        }
    }

    private boolean archivoYaEnviado(String nombre, File archivo) {
        return archivosEnviados.contains(obtenerIdentificadorArchivo(nombre, archivo));
    }

    private String obtenerIdentificadorArchivo(String nombre, File archivo) {
        return nombre + "_" + archivo.length() + "_" + archivo.lastModified();
    }

    private boolean esArchivoValido(File archivo, boolean sonImagenes) {
        String nombre = archivo.getName().toLowerCase();
        if (sonImagenes) {
            return nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") ||
                   nombre.endsWith(".png") || nombre.endsWith(".gif");
        } else {
            return nombre.endsWith(".dat");
        }
    }
}
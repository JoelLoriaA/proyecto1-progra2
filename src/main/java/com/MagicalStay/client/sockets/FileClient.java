package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileClient {
    private final SocketCliente socketCliente;
    private List<String> archivosEnviados = new ArrayList<>();

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
            System.err.println("Error creando directorios: " + e.getMessage());
        }
    }

    public void subirArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
        String comando = esImagen ? "subir_imagen" : "subir_archivo";
        socketCliente.enviarMensaje(comando + "|" + nombre);
        socketCliente.enviarObjeto(datos);

        if (esImagen) {
            // Solo guardar en el directorio de copias si es una imagen seleccionada por el usuario
            Path rutaCopia = Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, nombre);
            Files.createDirectories(rutaCopia.getParent());
            Files.write(rutaCopia, datos);
        } else {
            // Archivos normales se manejan igual
            Path rutaArchivo = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombre);
            Files.createDirectories(rutaArchivo.getParent());
            Files.write(rutaArchivo, datos);
        }
    }

    public List<String> listarArchivos() throws IOException {
        socketCliente.enviarMensaje("listar_archivos");
        List<String> archivos = new ArrayList<>();

        try {
            String respuesta = (String) socketCliente.recibirObjeto();
            if (respuesta == null || !respuesta.startsWith("FILE_COUNT|")) {
                throw new IOException("Protocolo de transferencia incorrecto");
            }

            int numArchivos = Integer.parseInt(respuesta.split("\\|")[1]);
            System.out.println("Se recibirán " + numArchivos + " archivos");

            for (int i = 0; i < numArchivos; i++) {
                String metadata = (String) socketCliente.recibirObjeto();
                if (metadata == null) continue;

                String[] partes = metadata.split("\\|");
                if (partes.length != 2) continue;

                boolean esImagen = partes[0].equals("imagen");
                String nombreArchivo = partes[1];
                archivos.add(nombreArchivo);

                byte[] contenido = (byte[]) socketCliente.recibirObjeto();
                if (contenido == null) continue;

                if (esImagen) {
                    // Verificar si la imagen ya existe en el directorio principal
                    Path rutaImagen = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, nombreArchivo);
                    if (!Files.exists(rutaImagen)) {
                        Files.createDirectories(rutaImagen.getParent());
                        Files.write(rutaImagen, contenido);
                        System.out.println("Nueva imagen guardada: " + rutaImagen);
                    } else {
                        System.out.println("Imagen ya existe en el directorio principal: " + rutaImagen);
                    }
                } else {
                    // Los archivos normales van al directorio de archivos
                    Path rutaArchivo = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombreArchivo);
                    Files.createDirectories(rutaArchivo.getParent());
                    Files.write(rutaArchivo, contenido);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error listando archivos: " + e.getMessage());
        }

        return archivos;
    }

    public void sincronizarBidireccional() throws IOException {
        try {
            String bienvenida = (String) socketCliente.recibirObjeto();
            if (!bienvenida.startsWith("WELCOME|")) {
                throw new IOException("Protocolo de conexión incorrecto");
            }
            System.out.println(bienvenida.split("\\|")[1]);

            System.out.println("Iniciando sincronización bidireccional...");
            List<String> archivosServidor = listarArchivos();
            System.out.println("Recibidos " + archivosServidor.size() + " archivos del servidor");

            enviarArchivosLocales();

        } catch (ClassNotFoundException e) {
            throw new IOException("Error en el protocolo de conexión: " + e.getMessage());
        }
    }

    private void enviarArchivosLocales() throws IOException {
        System.out.println("Enviando archivos locales al servidor...");
        enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, false);
        enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, true);
        enviarArchivosDesdeDirectorio(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, true);
    }

    private void enviarArchivosDesdeDirectorio(String directorio, boolean esImagen) {
        File dir = new File(directorio);
        if (dir.exists()) {
            File[] archivos = dir.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isFile()) {
                        try {
                            final String nombreArchivo = archivo.getName();

                            // Verificar si el archivo ya fue enviado
                            if (archivoYaEnviado(nombreArchivo, archivo)) {
                                System.out.println("Omitiendo archivo duplicado: " + nombreArchivo);
                                continue;
                            }

                            byte[] datos = Files.readAllBytes(archivo.toPath());
                            Platform.runLater(() -> {
                                try {
                                    subirArchivo(nombreArchivo, datos, esImagen);
                                    archivosEnviados.add(obtenerIdentificadorArchivo(nombreArchivo, archivo));
                                    System.out.println("Enviado " + (esImagen ? "imagen: " : "archivo: ") + nombreArchivo);
                                } catch (IOException e) {
                                    System.err.println("Error enviando " + (esImagen ? "imagen " : "archivo ") +
                                            nombreArchivo + ": " + e.getMessage());
                                }
                            });
                        } catch (IOException e) {
                            System.err.println("Error leyendo " + (esImagen ? "imagen " : "archivo ") +
                                    archivo.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private boolean archivoYaEnviado(String nombreArchivo, File archivo) {
        String identificador = obtenerIdentificadorArchivo(nombreArchivo, archivo);
        return archivosEnviados.contains(identificador);
    }

    private String obtenerIdentificadorArchivo(String nombreArchivo, File archivo) {
        return nombreArchivo + "_" + archivo.length() + "_" + archivo.lastModified();
    }
}
package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileClient {
    private final SocketCliente socketCliente;

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

        // Guardar copia local
        String rutaBase = esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;
        Path rutaLocal = Paths.get(rutaBase, nombre);
        Files.createDirectories(rutaLocal.getParent());
        Files.write(rutaLocal, datos);

    }

    public List<String> listarArchivos() throws IOException {
        socketCliente.enviarMensaje("listar_archivos");
        List<String> archivos = new ArrayList<>();

        try {
            String respuesta = (String) socketCliente.recibirObjeto();
            if (respuesta == null) {
                throw new IOException("No se recibió respuesta del servidor");
            }

            if (!respuesta.startsWith("FILE_COUNT|")) {
                System.err.println("Respuesta recibida: " + respuesta);
                throw new IOException("Protocolo de transferencia incorrecto: respuesta inválida");
            }

            int numArchivos;
            try {
                numArchivos = Integer.parseInt(respuesta.split("\\|")[1]);
                System.out.println("Se recibirán " + numArchivos + " archivos");
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new IOException("Formato de contador de archivos inválido");
            }

            for (int i = 0; i < numArchivos; i++) {
                String metadata = (String) socketCliente.recibirObjeto();
                if (metadata == null) {
                    throw new IOException("Metadata nula para archivo " + (i + 1));
                }

                String[] partes = metadata.split("\\|");
                if (partes.length != 2) {
                    throw new IOException("Formato de metadata inválido para archivo " + (i + 1));
                }

                archivos.add(partes[1]);
                System.out.println("Procesando archivo: " + partes[1]);

                byte[] contenido = (byte[]) socketCliente.recibirObjeto();
                if (contenido == null) {
                    throw new IOException("Contenido nulo para archivo " + partes[1]);
                }

                boolean esImagen = partes[0].equals("imagen");
                String rutaBase = esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;
                Path rutaLocal = Paths.get(rutaBase, partes[1]);
                Files.createDirectories(rutaLocal.getParent());
                Files.write(rutaLocal, contenido);
                System.out.println("Guardado archivo: " + rutaLocal);

                // Si es copia enviar al directorio de copias
                if (esImagen) {
                    Path rutaCopia = Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, partes[1]);
                    Files.createDirectories(rutaCopia.getParent());
                    Files.write(rutaCopia,contenido);

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
                            byte[] datos = Files.readAllBytes(archivo.toPath());
                            Platform.runLater(() -> {
                                try {
                                    subirArchivo(nombreArchivo, datos, esImagen);
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
}
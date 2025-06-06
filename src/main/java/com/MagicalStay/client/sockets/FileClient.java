package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import jakarta.xml.bind.DatatypeConverter;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileClient {
    private final SocketCliente socketCliente;
    private Map<String, String> hashesLocales = new HashMap<>();
    private Map<String, String> hashesServidor = new HashMap<>();

    // Método para calcular el hash MD5 de un archivo
    private String calcularMD5(byte[] datos) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(datos);
            return DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error calculando MD5: " + e.getMessage());
            return null;
        }
    }


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
        String hash = calcularMD5(datos);

        // Verificar si el archivo ya existe en el servidor
        if (hashesServidor.containsValue(hash)) {
            System.out.println("El archivo " + nombre + " ya existe en el servidor. Omitiendo...");
            return;
        }

        String comando = esImagen ? "subir_imagen" : "subir_archivo";
        socketCliente.enviarMensaje(comando + "|" + nombre + "|" + hash);

        // Esperar confirmación del servidor
        try {
            String respuesta = (String) socketCliente.recibirObjeto();
            if (respuesta.equals("ARCHIVO_EXISTENTE")) {
                System.out.println("El archivo ya existe en el servidor");
                return;
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Error en protocolo de transferencia");
        }

        // Proceder con la subida
        socketCliente.enviarObjeto(datos);

        // Guardar copia local
        String rutaBase = esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;
        Path rutaLocal = Paths.get(rutaBase, nombre);
        Files.createDirectories(rutaLocal.getParent());
        Files.write(rutaLocal, datos);

        // Actualizar hash local
        hashesLocales.put(nombre, hash);

        if (esImagen) {
            Path rutaCopia = Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, nombre);
            Files.createDirectories(rutaCopia.getParent());
            Files.copy(rutaLocal, rutaCopia, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public List<String> listarArchivos() throws IOException {
        socketCliente.enviarMensaje("listar_archivos");
        List<String> archivos = new ArrayList<>();
        hashesServidor.clear();

        try {
            String respuesta = (String) socketCliente.recibirObjeto();
            if (respuesta == null || !respuesta.startsWith("FILE_COUNT|")) {
                throw new IOException("Protocolo de transferencia incorrecto");
            }

            int numArchivos = Integer.parseInt(respuesta.split("\\|")[1]);

            for (int i = 0; i < numArchivos; i++) {
                String metadata = (String) socketCliente.recibirObjeto();
                if (metadata == null) continue;

                String[] partes = metadata.split("\\|");
                if (partes.length != 3) continue; // nombre|tipo|hash

                String nombre = partes[1];
                String hashServidor = partes[2];
                boolean esImagen = partes[0].equals("imagen");

                // Verificar si necesitamos el archivo
                String hashLocal = hashesLocales.get(nombre);
                if (hashLocal != null && hashLocal.equals(hashServidor)) {
                    System.out.println("Archivo " + nombre + " sin cambios. Omitiendo...");
                    continue;
                }

                archivos.add(nombre);
                byte[] contenido = (byte[]) socketCliente.recibirObjeto();
                if (contenido == null) continue;

                String rutaBase = esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR;
                Path rutaLocal = Paths.get(rutaBase, nombre);
                Files.createDirectories(rutaLocal.getParent());
                Files.write(rutaLocal, contenido);

                // Actualizar hash local
                hashesLocales.put(nombre, hashServidor);
                hashesServidor.put(nombre, hashServidor);

                if (esImagen) {
                    Path rutaCopia = Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, nombre);
                    Files.createDirectories(rutaCopia.getParent());
                    Files.copy(rutaLocal, rutaCopia, StandardCopyOption.REPLACE_EXISTING);
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
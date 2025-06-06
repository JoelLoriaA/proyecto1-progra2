package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            handleConnect();

            while (!socket.isClosed()) {
                String comando = (String) entrada.readObject();
                handleMessage(comando);

                if (comando.equalsIgnoreCase("salir")) {
                    break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente desconectado: " + socket.getInetAddress());
        } catch (Exception e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos();
            handleDisconnect();
        }
    }

    private void cerrarRecursos() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar recursos: " + e.getMessage());
        }
    }

    private void handleConnect() throws IOException {
        System.out.println("Nuevo cliente conectado desde: " + socket.getInetAddress());
        salida.writeObject("Bienvenido al servidor de MagicalStay");
        salida.flush();
    }

    private void handleMessage(String comando) throws IOException {
        String respuesta = procesarComando(comando);
        salida.writeObject(respuesta);
        salida.flush();
    }

    private String procesarComando(String comando) {
        try {
            String[] partes = comando.split("\\|");
            String accion = partes[0].toLowerCase();

            return switch (accion) {
                case "subir_archivo" -> handleFileUpload(partes);
                case "subir_imagen" -> handleImageUpload(partes);
                case "obtener_archivo" -> handleFileDownload(partes);
                case "obtener_imagen" -> handleImageDownload(partes);
                case "listar_archivos" -> handleListFiles();
                case "consultar" -> handleQuery();
                case "reservar" -> handleBooking(partes);
                case "cancelar" -> handleCancellation(partes);
                case "salir" -> handleExit();
                default -> "Comando no reconocido";
            };
        } catch (Exception e) {
            return "Error procesando comando: " + e.getMessage();
        }
    }

    private String handleFileUpload(String[] partes) throws IOException {
        if (partes.length < 2) return "Error: Nombre de archivo requerido";
        String nombreArchivo = partes[1];

        byte[] datos = (byte[]) recibirObjeto();
        Path rutaDestino = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombreArchivo);
        Files.createDirectories(rutaDestino.getParent());
        Files.write(rutaDestino, datos);

        return "Archivo subido exitosamente";
    }

    private String handleImageUpload(String[] partes) throws IOException {
        if (partes.length < 2) return "Error: Nombre de imagen requerido";
        String nombreImagen = partes[1];

        byte[] datos = (byte[]) recibirObjeto();
        Path rutaDestino = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, nombreImagen);
        Path rutaCopia = Paths.get(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, nombreImagen);

        Files.createDirectories(rutaDestino.getParent());
        Files.createDirectories(rutaCopia.getParent());
        Files.write(rutaDestino, datos);
        Files.copy(rutaDestino, rutaCopia, StandardCopyOption.REPLACE_EXISTING);

        return "Imagen subida exitosamente";
    }

    private String handleFileDownload(String[] partes) throws IOException {
        if (partes.length < 2) return "Error: Nombre de archivo requerido";
        String nombreArchivo = partes[1];

        Path rutaArchivo = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombreArchivo);
        if (!Files.exists(rutaArchivo)) {
            return "ERROR|Archivo no encontrado";
        }

        byte[] datos = Files.readAllBytes(rutaArchivo);
        salida.writeObject(datos);
        return "OK";
    }

    private String handleImageDownload(String[] partes) throws IOException {
        if (partes.length < 2) return "Error: Nombre de imagen requerido";
        String nombreImagen = partes[1];

        Path rutaImagen = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, nombreImagen);
        if (!Files.exists(rutaImagen)) {
            return "ERROR|Imagen no encontrada";
        }

        byte[] datos = Files.readAllBytes(rutaImagen);
        salida.writeObject(datos);
        return "OK";
    }

    private String handleListFiles() throws IOException {
        List<String> archivos = new ArrayList<>();
        try (DirectoryStream<Path> streamArchivos = Files.newDirectoryStream(Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR));
             DirectoryStream<Path> streamImagenes = Files.newDirectoryStream(Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR))) {

            streamArchivos.forEach(path -> archivos.add("archivo|" + path.getFileName()));
            streamImagenes.forEach(path -> archivos.add("imagen|" + path.getFileName()));
        }

        salida.writeObject("FILE_COUNT|" + archivos.size());
        for (String archivo : archivos) {
            String[] partes = archivo.split("\\|");
            Path ruta = Paths.get(
                    partes[0].equals("archivo") ?
                            ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR :
                            ConfiguracionApp.RUTA_IMAGENES_SERVIDOR,
                    partes[1]
            );
            salida.writeObject(archivo);
            salida.writeObject(Files.readAllBytes(ruta));
        }
        return "OK";
    }

    private String handleQuery() {
        // Implementar lógica de consulta
        return "Consultando disponibilidad de habitaciones...";
    }

    private String handleBooking(String[] partes) {
        if (partes.length < 2) {
            return "Error: Faltan parámetros para la reserva";
        }
        // Implementar lógica de reserva
        return "Procesando reserva para: " + partes[1];
    }

    private String handleCancellation(String[] partes) {
        if (partes.length < 2) {
            return "Error: Faltan parámetros para la cancelación";
        }
        // Implementar lógica de cancelación
        return "Cancelando reserva: " + partes[1];
    }

    private String handleExit() {
        return "¡Hasta luego! Gracias por usar MagicalStay";
    }

    private void handleDisconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Conexión cerrada con: " + socket.getInetAddress());
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket: " + e.getMessage());
        }
    }

    private Object recibirObjeto() throws IOException {
        try {
            return entrada.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al recibir objeto: " + e.getMessage());
        }
    }
}
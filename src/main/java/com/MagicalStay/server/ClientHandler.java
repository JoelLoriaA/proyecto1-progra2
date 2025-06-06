package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

            switch (accion) {
                case "listar_archivos":
                    File[] archivos = new File(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR).listFiles();
                    salida.writeObject(archivos.length);
                    for (File archivo : archivos) {
                        salida.writeObject(archivo.getName());
                        byte[] contenido = Files.readAllBytes(archivo.toPath());
                        salida.writeObject(contenido);
                    }
                    return "Lista de archivos enviada";

                case "subir_archivo":
                    String nombreArchivo = partes[1];
                    byte[] datos = (byte[]) entrada.readObject();
                    Path rutaArchivo = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombreArchivo);
                    Files.createDirectories(rutaArchivo.getParent());
                    Files.write(rutaArchivo, datos);
                    return "Archivo guardado: " + nombreArchivo;

                case "subir_imagen":
                    String nombreImagen = partes[1];
                    byte[] datosImagen = (byte[]) entrada.readObject();
                    Path rutaImagen = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, nombreImagen);
                    Files.createDirectories(rutaImagen.getParent());
                    Files.write(rutaImagen, datosImagen);

                    return "Imagen guardada: " + nombreImagen;

                case "obtener_archivo":
                    String nombre = partes[1];
                    Path ruta = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombre);
                    if (Files.exists(ruta)) {
                        byte[] contenido = Files.readAllBytes(ruta);
                        salida.writeObject(contenido);
                        return "Archivo enviado";
                    }
                    return "Archivo no encontrado";
                default:
                    return "Comando no reconocido";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
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
}
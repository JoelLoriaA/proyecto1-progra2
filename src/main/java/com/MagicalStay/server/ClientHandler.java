package com.MagicalStay.server;

import com.MagicalStay.shared.config.ConfiguracionApp;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private final List<ClientHandler> clientes;

    // Flag para controlar la sincronización inicial
    private volatile boolean sincronizando = true;

    public void setSincronizando(boolean valor) {
        this.sincronizando = valor;
    }

    public boolean estaSincronizando() {
        return sincronizando;
    }

    public ClientHandler(Socket socket, List<ClientHandler> clientes) {
        this.socket = socket;
        this.clientes = clientes;
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
                case "ready":
                    return "OK";

                case "listar_archivos":
                    File[] archivosNormales = new File(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR).listFiles();
                    File[] imagenes = new File(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR).listFiles();
                    File[] copiasImagenes = new File(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR).listFiles();

                    int totalArchivos = 0;
                    if (archivosNormales != null)
                        totalArchivos += Arrays.stream(archivosNormales).filter(File::isFile).count();
                    if (imagenes != null)
                        totalArchivos += Arrays.stream(imagenes).filter(File::isFile).count();
                    if (copiasImagenes != null)
                        totalArchivos += Arrays.stream(copiasImagenes).filter(File::isFile).count();

                    salida.writeObject("FILE_COUNT|" + totalArchivos);

                    if (archivosNormales != null) {
                        for (File archivo : archivosNormales) {
                            if (archivo.isFile()) {
                                salida.writeObject("archivo|" + archivo.getName());
                                byte[] contenido = Files.readAllBytes(archivo.toPath());
                                salida.writeObject(contenido);
                            }
                        }
                    }
                    if (imagenes != null) {
                        for (File imagen : imagenes) {
                            if (imagen.isFile()) {
                                salida.writeObject("imagen|" + imagen.getName());
                                byte[] contenido = Files.readAllBytes(imagen.toPath());
                                salida.writeObject(contenido);
                            }
                        }
                    }
                    if (copiasImagenes != null) {
                        for (File copia : copiasImagenes) {
                            if (copia.isFile()) {
                                salida.writeObject("copia_imagen|" + copia.getName());
                                byte[] contenido = Files.readAllBytes(copia.toPath());
                                salida.writeObject(contenido);
                            }
                        }
                    }

                    // Marcar que ya terminó la sincronización inicial
                    this.setSincronizando(false);
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

    private void handleConnect() throws IOException {
        System.out.println("Cliente conectado desde: " + socket.getInetAddress());
        salida.writeObject("WELCOME|Conectado al servidor MagicalStay");
        salida.flush();
    }

    private void cerrarRecursos() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error cerrando recursos: " + e.getMessage());
        }
    }

    private void handleDisconnect() {
        System.out.println("Cliente desconectado: " + socket.getInetAddress());
        if (clientes != null) {
            clientes.remove(this);
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeObject(mensaje);
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error enviando mensaje al cliente: " + e.getMessage());
        }
    }
}
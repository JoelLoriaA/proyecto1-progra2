package com.MagicalStay.server;

    import com.MagicalStay.client.sockets.SocketCliente;
    import com.MagicalStay.shared.config.ConfiguracionApp;
    import java.io.*;
    import java.nio.file.*;
    import java.util.*;

    public class FileService {
        private final SocketCliente socketCliente;

        public FileService(SocketCliente socketCliente) {
            this.socketCliente = socketCliente;
            crearDirectorios();
        }

        private void crearDirectorios() {
            try {
                Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR));
                Files.createDirectories(Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR));
            } catch (IOException e) {
                System.err.println("Error creando directorios: " + e.getMessage());
            }
        }

        public void subirArchivo(String nombreArchivo, byte[] datos, boolean esImagen) throws IOException {
            String comando = esImagen ? "subir_imagen" : "subir_archivo";
            socketCliente.enviarMensaje(comando + "|" + nombreArchivo);
            socketCliente.enviarObjeto(datos);
        }

        public byte[] obtenerArchivo(String nombreArchivo) throws IOException {
            socketCliente.enviarMensaje("obtener_archivo|" + nombreArchivo);
            try {
                return (byte[]) socketCliente.recibirObjeto();
            } catch (ClassNotFoundException e) {
                throw new IOException("Error recibiendo archivo: " + e.getMessage());
            }
        }

        public void sincronizarArchivos() throws IOException {
            socketCliente.enviarMensaje("listar_archivos");
            try {
                int numArchivos = (Integer) socketCliente.recibirObjeto();
                System.out.println("Sincronizando " + numArchivos + " archivos...");

                for (int i = 0; i < numArchivos; i++) {
                    String metadata = (String) socketCliente.recibirObjeto();
                    String[] partes = metadata.split("\\|");
                    String tipo = partes[0];
                    String nombreArchivo = partes[1];

                    byte[] contenido = (byte[]) socketCliente.recibirObjeto();

                    Path rutaLocal;
                    if (tipo.equals("archivo")) {
                        rutaLocal = Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombreArchivo);
                    } else {
                        rutaLocal = Paths.get(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR, nombreArchivo);
                    }

                    Files.createDirectories(rutaLocal.getParent());
                    Files.write(rutaLocal, contenido, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Archivo sincronizado: " + rutaLocal);
                }
            } catch (ClassNotFoundException e) {
                throw new IOException("Error sincronizando archivos: " + e.getMessage());
            }
        }

        public void guardarArchivoLocal(String nombre, byte[] datos, boolean esImagen) throws IOException {
            Path ruta = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, datos);
        }

        public byte[] leerArchivoLocal(String nombre, boolean esImagen) throws IOException {
            Path ruta = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            return Files.readAllBytes(ruta);
        }
    }
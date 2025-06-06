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

                // 2. Recibir cantidad de archivos
                String respuesta = (String) socketCliente.recibirObjeto();
                String[] partes = respuesta.split("\\|");
                int cantidadArchivos = Integer.parseInt(partes[1]);
                System.out.println("Se recibirán " + cantidadArchivos + " archivos");

                // 3. Recibir archivos del servidor
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

                // 4. Enviar archivos locales al servidor
                System.out.println("Enviando archivos locales al servidor...");

                // Enviar archivos normales
                File dirArchivos = new File(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR);
                if (dirArchivos.exists()) {
                    for (File archivo : dirArchivos.listFiles()) {
                        if (archivo.isFile()) {
                            enviarArchivo(archivo.getName(), Files.readAllBytes(archivo.toPath()), false);
                            System.out.println("Enviado archivo: " + archivo.getName());
                        }
                    }
                }

                // Enviar imágenes originales
                File dirImagenes = new File(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR);
                if (dirImagenes.exists()) {
                    for (File imagen : dirImagenes.listFiles()) {
                        if (imagen.isFile()) {
                            enviarArchivo(imagen.getName(), Files.readAllBytes(imagen.toPath()), true);
                            System.out.println("Enviada imagen original: " + imagen.getName());
                        }
                    }
                }
            } finally {
                sincronizando = false;
            }
        }

        private void enviarArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
            String comando = esImagen ? "subir_imagen" : "subir_archivo";
            socketCliente.enviarMensaje(comando + "|" + nombre);

            // Enviar en chunks si el archivo es grande
            int offset = 0;
            while (offset < datos.length) {
                int chunkSize = Math.min(BUFFER_SIZE, datos.length - offset);
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(datos, offset, chunk, 0, chunkSize);
                socketCliente.enviarObjeto(chunk);
                offset += chunkSize;
            }
        }

        public void subirArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
            // Verificar si el archivo ya fue enviado
            Path rutaDestino = Paths.get(esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombre);
            File archivoExistente = rutaDestino.toFile();

            if (archivoYaEnviado(nombre, archivoExistente)) {
                System.out.println("El archivo " + nombre + " ya fue enviado anteriormente");
                return;
            }

            // Enviar archivo al servidor
            enviarArchivo(nombre, datos, esImagen);

            // Guardar localmente si es necesario
            if (!esImagen || !Files.exists(rutaDestino)) {
                Files.createDirectories(rutaDestino.getParent());
                Files.write(rutaDestino, datos);
            }

            // Marcar como enviado
            archivosEnviados.add(obtenerIdentificadorArchivo(nombre, archivoExistente));
        }

        private boolean archivoYaEnviado(String nombre, File archivo) {
            String identificador = obtenerIdentificadorArchivo(nombre, archivo);
            return archivosEnviados.contains(identificador);
        }

        private String obtenerIdentificadorArchivo(String nombre, File archivo) {
            return nombre + "_" + (archivo.exists() ? archivo.length() + "_" + archivo.lastModified() : "nuevo");
        }
    }
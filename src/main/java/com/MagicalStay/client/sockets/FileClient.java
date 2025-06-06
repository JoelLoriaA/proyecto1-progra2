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
            } catch (IOException e) {
                System.err.println("Error creando directorios: " + e.getMessage());
            }
        }

        public void subirArchivo(String nombre, byte[] datos, boolean esImagen) throws IOException {
            String comando = esImagen ? "subir_imagen" : "subir_archivo";
            socketCliente.enviarMensaje(comando + "|" + nombre);
            socketCliente.enviarObjeto(datos);

            // Guardar copia local
            Path rutaLocal = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            Files.createDirectories(rutaLocal.getParent());
            Files.write(rutaLocal, datos);
        }

        public byte[] descargarArchivo(String nombre, boolean esImagen) throws IOException {
            String comando = esImagen ? "obtener_imagen" : "obtener_archivo";
            socketCliente.enviarMensaje(comando + "|" + nombre);
            try {
                byte[] datos = (byte[]) socketCliente.recibirObjeto();

                // Guardar copia local
                Path rutaLocal = Paths.get(
                    esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                    nombre
                );
                Files.createDirectories(rutaLocal.getParent());
                Files.write(rutaLocal, datos);

                return datos;
            } catch (ClassNotFoundException e) {
                throw new IOException("Error recibiendo archivo: " + e.getMessage());
            }
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

                    // Recibir y guardar el contenido
                    byte[] contenido = (byte[]) socketCliente.recibirObjeto();
                    if (contenido == null) {
                        throw new IOException("Contenido nulo para archivo " + partes[1]);
                    }

                    Path rutaLocal = Paths.get(
                        partes[0].equals("archivo") ?
                            ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR :
                            ConfiguracionApp.RUTA_IMAGENES_SERVIDOR,
                        partes[1]
                    );
                    Files.createDirectories(rutaLocal.getParent());
                    Files.write(rutaLocal, contenido);
                    System.out.println("Guardado archivo: " + rutaLocal);
                }
            } catch (ClassNotFoundException e) {
                throw new IOException("Error listando archivos: " + e.getMessage());
            }

            return archivos;
        }

    public void sincronizarBidireccional() throws IOException {
            try {
                // Esperar mensaje de bienvenida
                String bienvenida = (String) socketCliente.recibirObjeto();
                if (!bienvenida.startsWith("WELCOME|")) {
                    throw new IOException("Protocolo de conexión incorrecto");
                }
                System.out.println(bienvenida.split("\\|")[1]);

                // Iniciar sincronización directamente sin enviar READY
                System.out.println("Iniciando sincronización bidireccional...");

                // Primero recibir archivos del servidor
                List<String> archivosServidor = listarArchivos();
                System.out.println("Recibidos " + archivosServidor.size() + " archivos del servidor");

                // Luego enviar archivos locales al servidor
                enviarArchivosLocales();

            } catch (ClassNotFoundException e) {
                throw new IOException("Error en el protocolo de conexión: " + e.getMessage());
            }
        }

        private void enviarArchivosLocales() throws IOException {
            System.out.println("Enviando archivos locales al servidor...");

            // Enviar archivos normales
            File dirArchivos = new File(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR);
            if (dirArchivos.exists()) {
                File[] archivos = dirArchivos.listFiles();
                if (archivos != null) {
                    for (File archivo : archivos) {
                        if (archivo.isFile()) {
                            try {
                                final String nombreArchivo = archivo.getName();
                                byte[] datos = Files.readAllBytes(archivo.toPath());
                                Platform.runLater(() -> {
                                    try {
                                        subirArchivo(nombreArchivo, datos, false);
                                        System.out.println("Enviado archivo: " + nombreArchivo);
                                    } catch (IOException e) {
                                        System.err.println("Error enviando archivo " + nombreArchivo + ": " + e.getMessage());
                                    }
                                });
                            } catch (IOException e) {
                                System.err.println("Error leyendo archivo " + archivo.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }

            // Enviar imágenes
            File dirImagenes = new File(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR);
            if (dirImagenes.exists()) {
                File[] imagenes = dirImagenes.listFiles();
                if (imagenes != null) {
                    for (File imagen : imagenes) {
                        if (imagen.isFile()) {
                            try {
                                final String nombreImagen = imagen.getName();
                                byte[] datos = Files.readAllBytes(imagen.toPath());
                                Platform.runLater(() -> {
                                    try {
                                        subirArchivo(nombreImagen, datos, true);
                                        System.out.println("Enviada imagen: " + nombreImagen);
                                    } catch (IOException e) {
                                        System.err.println("Error enviando imagen " + nombreImagen + ": " + e.getMessage());
                                    }
                                });
                            } catch (IOException e) {
                                System.err.println("Error leyendo imagen " + imagen.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        public byte[] leerArchivoLocal(String nombre, boolean esImagen) throws IOException {
            Path ruta = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            return Files.readAllBytes(ruta);
        }

        public void guardarArchivoLocal(String nombre, byte[] datos, boolean esImagen) throws IOException {
            Path ruta = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, datos);
        }

        public boolean existeArchivoLocal(String nombre, boolean esImagen) {
            Path ruta = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            return Files.exists(ruta);
        }

        public void eliminarArchivoLocal(String nombre, boolean esImagen) throws IOException {
            Path ruta = Paths.get(
                esImagen ? ConfiguracionApp.RUTA_IMAGENES_SERVIDOR : ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR,
                nombre
            );
            Files.deleteIfExists(ruta);
        }
    }
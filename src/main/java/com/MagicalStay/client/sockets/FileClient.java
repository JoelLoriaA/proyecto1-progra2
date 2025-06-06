package com.MagicalStay.client.sockets;

    import com.MagicalStay.shared.config.ConfiguracionApp;
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
                if (!respuesta.startsWith("FILE_COUNT|")) {
                    throw new IOException("Protocolo de transferencia incorrecto");
                }

                int numArchivos = Integer.parseInt(respuesta.split("\\|")[1]);
                for (int i = 0; i < numArchivos; i++) {
                    String metadata = (String) socketCliente.recibirObjeto();
                    String[] partes = metadata.split("\\|");
                    archivos.add(partes[1]); // Agregar solo el nombre del archivo

                    // Recibir y guardar el contenido
                    byte[] contenido = (byte[]) socketCliente.recibirObjeto();
                    Path rutaLocal = Paths.get(
                        partes[0].equals("archivo") ?
                            ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR :
                            ConfiguracionApp.RUTA_IMAGENES_SERVIDOR,
                        partes[1]
                    );
                    Files.createDirectories(rutaLocal.getParent());
                    Files.write(rutaLocal, contenido);
                }
            } catch (ClassNotFoundException e) {
                throw new IOException("Error listando archivos: " + e.getMessage());
            }

            return archivos;
        }

       public void sincronizarBidireccional() throws IOException {
            // Primero recibir archivos del servidor
            System.out.println("Iniciando sincronización bidireccional...");
            List<String> archivosServidor = listarArchivos();
            System.out.println("Recibidos " + archivosServidor.size() + " archivos del servidor");

            // Luego enviar archivos locales al servidor
            enviarArchivosLocales();
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
                                byte[] datos = Files.readAllBytes(archivo.toPath());
                                subirArchivo(archivo.getName(), datos, false);
                                System.out.println("Enviado archivo: " + archivo.getName());
                            } catch (IOException e) {
                                System.err.println("Error enviando archivo " + archivo.getName() + ": " + e.getMessage());
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
                                byte[] datos = Files.readAllBytes(imagen.toPath());
                                subirArchivo(imagen.getName(), datos, true);
                                System.out.println("Enviada imagen: " + imagen.getName());
                            } catch (IOException e) {
                                System.err.println("Error enviando imagen " + imagen.getName() + ": " + e.getMessage());
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
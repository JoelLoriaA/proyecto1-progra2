package com.MagicalStay.client.sockets;

    import javafx.application.Platform;
    import java.io.*;
    import java.net.Socket;
    import java.net.InetSocketAddress;
    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.LinkedBlockingQueue;

    public class SocketCliente {
        private static final int TIMEOUT_CONEXION = 200000;
        private Socket socket;
        private ObjectOutputStream salida;
        private ObjectInputStream entrada;
        private volatile boolean conectado;
        private final ClienteCallback callback;

        // Cola para respuestas síncronas
        private final BlockingQueue<Object> respuestas = new LinkedBlockingQueue<>();

        public interface ClienteCallback {
            void onMensajeRecibido(String mensaje);
            void onError(String error);
            void onConexionEstablecida();
            void onDesconexion();
        }

        public SocketCliente(ClienteCallback callback) {
            this.callback = callback;
        }

        public void conectar(String host, int puerto) {
            if (conectado) return;

            new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, puerto), TIMEOUT_CONEXION);
                    salida = new ObjectOutputStream(socket.getOutputStream());
                    entrada = new ObjectInputStream(socket.getInputStream());
                    conectado = true;

                    // Usar la sincronización bidireccional
                    FileClient fileClient = new FileClient(this);
                    fileClient.sincronizarBidireccional();

                    Platform.runLater(() -> callback.onConexionEstablecida());
                    escucharMensajes();
                } catch (IOException e) {
                    Platform.runLater(() -> callback.onError("Error de conexión: " + e.getMessage()));
                }
            }).start();
        }

        private void procesarMensaje(String mensaje) {
            if (mensaje.startsWith("ARCHIVO_CAMBIADO|")) {
                // Notificación asíncrona: manejarla aquí
                Platform.runLater(() -> {
                    try {
                        new FileClient(this).listarArchivos();
                    } catch (IOException e) {
                        callback.onError("Error sincronizando archivos: " + e.getMessage());
                    }
                });
            } else {
                // Mensaje de respuesta: ponerlo en la cola para el hilo que lo espera
                respuestas.offer(mensaje);
            }
        }

        private void escucharMensajes() {
            new Thread(() -> {
                while (conectado) {
                    try {
                        Object mensaje = entrada.readObject();
                        if (mensaje instanceof String) {
                            procesarMensaje((String) mensaje);
                        } else {
                            // Para transferencias de archivos, poner en la cola
                            respuestas.offer(mensaje);
                        }
                    } catch (Exception e) {
                        if (conectado) {
                            Platform.runLater(() -> callback.onError("Error: " + e.getMessage()));
                            desconectar();
                        }
                        break;
                    }
                }
            }).start();
        }

        public void enviarMensaje(String mensaje) {
            if (!conectado) {
                Platform.runLater(() -> callback.onError("No conectado al servidor"));
                return;
            }

            try {
                salida.writeObject(mensaje);
                salida.flush();
            } catch (IOException e) {
                Platform.runLater(() -> {
                    callback.onError("Error enviando mensaje: " + e.getMessage());
                    desconectar();
                });
            }
        }

        public void desconectar() {
            if (!conectado) return;

            conectado = false;
            try {
                if (salida != null) salida.close();
                if (entrada != null) entrada.close();
                if (socket != null) socket.close();
                Platform.runLater(() -> callback.onDesconexion());
            } catch (IOException e) {
                Platform.runLater(() -> callback.onError("Error al desconectar: " + e.getMessage()));
            }
        }

        public boolean estaConectado() {
            return conectado;
        }

        public void enviarObjeto(Object obj) {
            if (!conectado) {
                callback.onError("No conectado al servidor");
                return;
            }
            new Thread(() -> {
                try {
                    salida.writeObject(obj);
                    salida.flush();
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        callback.onError("Error enviando objeto: " + e.getMessage());
                        desconectar();
                    });
                }
            }).start();
        }

        // Cambiado: ahora espera la respuesta de la cola
        public Object recibirObjeto() throws IOException, ClassNotFoundException {
            if (!conectado) {
                throw new IOException("No conectado al servidor");
            }
            try {
                Object obj = respuestas.take();
                return obj;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrumpido esperando respuesta del servidor");
            }
        }
    }

package com.MagicalStay.client.sockets;

import com.MagicalStay.shared.config.ConfiguracionApp;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SocketCliente {
    private static final int TIMEOUT_CONEXION = 5000;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private volatile boolean conectado;
    private volatile boolean desconectando = false;
    private final ClienteCallback callback;

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


    private void escucharMensajes() {
        new Thread(() -> {
            while (conectado) {
                try {
                    Object mensaje = entrada.readObject();
                    if (mensaje instanceof String) {
                        String msg = (String) mensaje;
                        // Manejo de notificaciones de archivos
                        if (msg.startsWith("NOTIFICACION|archivo_modificado|")) {
                            String nombreArchivo = msg.split("\\|")[2];
                            enviarMensaje("obtener_archivo|" + nombreArchivo);
                            try {
                                byte[] datos = (byte[]) recibirObjeto();
                                // Guardar archivo actualizado localmente
                                Files.write(Paths.get(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR, nombreArchivo), datos);
                            } catch (Exception e) {
                                System.err.println("Error descargando archivo modificado: " + e.getMessage());
                            }
                        } else {
                            Platform.runLater(() -> callback.onMensajeRecibido(msg));
                        }
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
        if (!conectado || desconectando) return;
        desconectando = true;
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

    public Object recibirObjeto() throws IOException, ClassNotFoundException {
        if (!conectado) {
            throw new IOException("No conectado al servidor");
        }
        return entrada.readObject();
    }
}

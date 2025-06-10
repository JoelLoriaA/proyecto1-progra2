
package com.MagicalStay.client.sockets;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;

public class SocketCliente {
    private static final int TIMEOUT_CONEXION = 5000;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private volatile boolean conectado;
    private final ClienteCallback callback;
    private Thread hiloEscucha;

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

                // Esperar y consumir el mensaje de bienvenida
                Object bienvenida = entrada.readObject();
                if (bienvenida instanceof String && ((String) bienvenida).startsWith("WELCOME|")) {
                    Platform.runLater(() -> callback.onConexionEstablecida());
                }

            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    public void iniciarEscuchaMensajes() {
        if (hiloEscucha != null && hiloEscucha.isAlive()) return;
        hiloEscucha = new Thread(this::escucharMensajes);
        hiloEscucha.start();
    }

    public void detenerEscuchaMensajes() {
        if (hiloEscucha != null) {
            hiloEscucha.interrupt();
            hiloEscucha = null;
        }
    }

    public void iniciarSincronizacionBidireccional() {
        try{
        FileClient fileClient = new FileClient(this);

            fileClient.sincronizarBidireccional();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


   private void escucharMensajes() {
        while (conectado) {
            try {
                Object mensaje = entrada.readObject();
                if (mensaje instanceof String) {
                    Platform.runLater(() -> callback.onMensajeRecibido((String) mensaje));
                }
            } catch (Exception e) {
                if (conectado) {
                    Platform.runLater(() -> callback.onError("Error: " + e.getMessage()));
                    desconectar();
                }
                break;
            }
        }
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
        try {
            salida.writeObject(obj);
            salida.flush();
        } catch (IOException e) {
            Platform.runLater(() -> {
                callback.onError("Error enviando objeto: " + e.getMessage());
                desconectar();
            });
        }
    }

    public Object recibirObjeto() throws IOException, ClassNotFoundException {
        if (!conectado) {
            throw new IOException("No conectado al servidor");
        }
        return entrada.readObject();
    }
}

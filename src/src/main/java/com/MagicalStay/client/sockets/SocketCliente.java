package com.MagicalStay.client.sockets;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class SocketCliente {
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado;
    private final ClienteCallback callback;
    private Thread listenerThread;

    public interface ClienteCallback {
        void onMensajeRecibido(String mensaje);
        void onError(String error);
        void onConexionEstablecida();
        void onDesconexion();
    }

    public SocketCliente(ClienteCallback callback) {
        this.callback = callback;
        this.conectado = false;
    }

    public void conectar(String host, int puerto) {
        if (conectado) {
            return;
        }

        new Thread(() -> {
            try {
                socket = new Socket(host, puerto);
                salida = new ObjectOutputStream(socket.getOutputStream());
                entrada = new ObjectInputStream(socket.getInputStream());
                conectado = true;
                
                Platform.runLater(() -> callback.onConexionEstablecida());
                iniciarEscucha();
            } catch (IOException e) {
                Platform.runLater(() -> callback.onError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    private void iniciarEscucha() {
        listenerThread = new Thread(() -> {
            while (conectado) {
                try {
                    Object mensaje = entrada.readObject();
                    if (mensaje instanceof String) {
                        String mensajeStr = (String) mensaje;
                        Platform.runLater(() -> callback.onMensajeRecibido(mensajeStr));
                    }
                } catch (SocketException e) {
                    if (conectado) {
                        desconectar();
                    }
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (conectado) {
                        Platform.runLater(() -> callback.onError("Error de comunicación: " + e.getMessage()));
                        desconectar();
                    }
                    break;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void enviarMensaje(String mensaje) {
        if (!conectado) {
            Platform.runLater(() -> callback.onError("No está conectado al servidor"));
            return;
        }

        new Thread(() -> {
            try {
                salida.writeObject(mensaje);
                salida.flush();
            } catch (IOException e) {
                Platform.runLater(() -> callback.onError("Error enviando mensaje: " + e.getMessage()));
                desconectar();
            }
        }).start();
    }

    public void desconectar() {
        if (!conectado) {
            return;
        }
        
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
}
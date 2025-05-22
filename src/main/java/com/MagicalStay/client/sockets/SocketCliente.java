package com.MagicalStay.client.sockets;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketCliente {
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado = false;
    private ClienteCallback callback;

    public interface ClienteCallback {
        void onMensajeRecibido(String mensaje);
        void onError(String error);
        void onConexionEstablecida();
        void onDesconexion();
    }

    public void setCallback(ClienteCallback callback) {
        this.callback = callback;
    }

    public void conectar(String host, int puerto) {
        Task<Void> conexionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    socket = new Socket(host, puerto);
                    salida = new ObjectOutputStream(socket.getOutputStream());
                    entrada = new ObjectInputStream(socket.getInputStream());
                    conectado = true;
                    
                    Platform.runLater(() -> {
                        if (callback != null) {
                            callback.onConexionEstablecida();
                        }
                    });

                    // Iniciar hilo de escucha
                    iniciarEscucha();
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        if (callback != null) {
                            callback.onError("Error al conectar: " + e.getMessage());
                        }
                    });
                }
                return null;
            }
        };

        new Thread(conexionTask).start();
    }

    private void iniciarEscucha() {
        Thread escuchaThread = new Thread(() -> {
            while (conectado) {
                try {
                    String mensaje = (String) entrada.readObject();
                    Platform.runLater(() -> {
                        if (callback != null) {
                            callback.onMensajeRecibido(mensaje);
                        }
                    });
                } catch (IOException | ClassNotFoundException e) {
                    if (conectado) {
                        conectado = false;
                        Platform.runLater(() -> {
                            if (callback != null) {
                                callback.onError("Error de comunicación: " + e.getMessage());
                                callback.onDesconexion();
                            }
                        });
                    }
                    break;
                }
            }
        });
        escuchaThread.setDaemon(true);
        escuchaThread.start();
    }

    public void enviarMensaje(String mensaje) {
        if (!conectado) {
            if (callback != null) {
                callback.onError("No está conectado al servidor");
            }
            return;
        }

        Task<Void> envioTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    salida.writeObject(mensaje);
                    salida.flush();
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        if (callback != null) {
                            callback.onError("Error al enviar mensaje: " + e.getMessage());
                        }
                    });
                }
                return null;
            }
        };

        new Thread(envioTask).start();
    }

    public void desconectar() {
        if (conectado) {
            try {
                conectado = false;
                if (salida != null) salida.close();
                if (entrada != null) entrada.close();
                if (socket != null) socket.close();
                
                if (callback != null) {
                    Platform.runLater(() -> callback.onDesconexion());
                }
            } catch (IOException e) {
                if (callback != null) {
                    Platform.runLater(() -> callback.onError("Error al desconectar: " + e.getMessage()));
                }
            }
        }
    }

    public boolean estaConectado() {
        return conectado;
    }
}
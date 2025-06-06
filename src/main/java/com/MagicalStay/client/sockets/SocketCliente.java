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
                socket.setKeepAlive(true);
                socket.setSoTimeout(30000); // 30 segundos timeout
                socket.connect(new InetSocketAddress(host, puerto), TIMEOUT_CONEXION);

                salida = new ObjectOutputStream(socket.getOutputStream());
                salida.flush();
                entrada = new ObjectInputStream(socket.getInputStream());

                conectado = true;
                Platform.runLater(() -> callback.onConexionEstablecida());

                // Leer mensaje de bienvenida
                String mensajeBienvenida = (String) entrada.readObject();
                Platform.runLater(() -> callback.onMensajeRecibido(mensajeBienvenida));

                // Iniciar sincronización en un hilo separado
                new Thread(() -> {
                    try {
                        FileClient fileClient = new FileClient(this);
                        fileClient.sincronizarBidireccional();
                    } catch (Exception e) {
                        Platform.runLater(() -> callback.onError("Error en sincronización: " + e.getMessage()));
                    }
                }).start();

                // Bucle principal de recepción
                while (conectado && !socket.isClosed()) {
                    Object mensaje = entrada.readObject();
                    if (mensaje instanceof String) {
                        String mensajeStr = (String) mensaje;
                        Platform.runLater(() -> callback.onMensajeRecibido(mensajeStr));
                    }
                }
            } catch (Exception e) {
                conectado = false;
                Platform.runLater(() -> {
                    callback.onError("Error de conexión: " + e.getMessage());
                    callback.onDesconexion();
                });
            }
        }).start();
    }


    private void escucharMensajes() {
        new Thread(() -> {
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

  public synchronized void enviarObjeto(Object obj) {
        if (!conectado) {
            Platform.runLater(() -> callback.onError("No conectado al servidor"));
            return;
        }

        try {
            if (obj instanceof byte[]) {
                byte[] datos = (byte[]) obj;
                salida.writeInt(datos.length);
                salida.flush();

                int tamanoChunk = 8192;
                for (int offset = 0; offset < datos.length; offset += tamanoChunk) {
                    int tamanoActual = Math.min(tamanoChunk, datos.length - offset);
                    byte[] chunk = new byte[tamanoActual];
                    System.arraycopy(datos, offset, chunk, 0, tamanoActual);
                    salida.writeInt(tamanoActual);
                    salida.write(chunk, 0, tamanoActual);
                    salida.flush();
                }
                salida.writeInt(-1);
                salida.flush();
            } else {
                salida.writeObject(obj);
                salida.flush();
            }
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
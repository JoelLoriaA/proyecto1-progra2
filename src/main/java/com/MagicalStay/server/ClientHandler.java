package com.MagicalStay.server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            handleConnect();

            while (!socket.isClosed()) {
                try {
                    String comando = (String) entrada.readObject();
                    handleMessage(comando);

                    if ("salir".equalsIgnoreCase(comando)) {
                        break;
                    }
                } catch (EOFException e) {
                    LOGGER.log(Level.WARNING, "Cliente desconectado abruptamente: {0}", socket.getInetAddress());
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    LOGGER.log(Level.SEVERE, "Error procesando comando: {0}", e.getMessage());
                    enviarError("Error procesando comando: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error inicializando comunicación: {0}", e.getMessage());
        } finally {
            cerrarRecursos();
            handleDisconnect();
        }
    }

    private void handleMessage(String comando) {
        try {
            String respuesta = procesarComando(comando);
            salida.writeObject(respuesta);
            salida.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error enviando respuesta al cliente: {0}", e.getMessage());
            enviarError("Error enviando respuesta: " + e.getMessage());
        }
    }

    private String procesarComando(String comando) {
        // Aquí implementa la lógica real de tus comandos
        // Por ahora solo responde con un eco
        return "RESPUESTA|" + comando;
    }

    private void enviarError(String mensaje) {
        try {
            salida.writeObject("ERROR|" + mensaje);
            salida.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error enviando mensaje de error: {0}", e.getMessage());
        }
    }

    private void cerrarRecursos() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error cerrando recursos: {0}", e.getMessage());
        }
    }

    private void handleConnect() {
        try {
            LOGGER.log(Level.INFO, "Cliente conectado desde: {0}", socket.getInetAddress());
            salida.writeObject("WELCOME|Conectado al servidor MagicalStay");
            salida.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error enviando mensaje de bienvenida: {0}", e.getMessage());
        }
    }

    private void handleDisconnect() {
        LOGGER.log(Level.INFO, "Cliente desconectado: {0}", socket.getInetAddress());
    }
}
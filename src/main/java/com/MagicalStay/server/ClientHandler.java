package com.MagicalStay.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
        ) {
            this.salida = salida;
            this.entrada = entrada;
            
            handleConnect();

            while (!socket.isClosed()) {
                String comando = (String) entrada.readObject();
                handleMessage(comando);

                if (comando.equalsIgnoreCase("salir")) {
                    break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente desconectado: " + socket.getInetAddress());
        } catch (Exception e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            handleDisconnect();
        }
    }

    private void handleConnect() throws IOException {
        System.out.println("Nuevo cliente conectado desde: " + socket.getInetAddress());
        salida.writeObject("Bienvenido al servidor de MagicalStay");
        salida.flush();
    }

    private void handleMessage(String comando) throws IOException {
        String respuesta = procesarComando(comando);
        salida.writeObject(respuesta);
        salida.flush();
    }

    private String procesarComando(String comando) {
        try {
            String[] partes = comando.split("\\|");
            String accion = partes[0].toLowerCase();

            return switch (accion) {
                case "consultar" -> handleQuery();
                case "reservar" -> handleBooking(partes);
                case "cancelar" -> handleCancellation(partes);
                case "salir" -> handleExit();
                default -> "Comando no reconocido";
            };
        } catch (Exception e) {
            return "Error procesando comando: " + e.getMessage();
        }
    }

    private String handleQuery() {
        // Implementar lógica de consulta
        return "Consultando disponibilidad de habitaciones...";
    }

    private String handleBooking(String[] partes) {
        if (partes.length < 2) {
            return "Error: Faltan parámetros para la reserva";
        }
        // Implementar lógica de reserva
        return "Procesando reserva para: " + partes[1];
    }

    private String handleCancellation(String[] partes) {
        if (partes.length < 2) {
            return "Error: Faltan parámetros para la cancelación";
        }
        // Implementar lógica de cancelación
        return "Cancelando reserva: " + partes[1];
    }

    private String handleExit() {
        return "¡Hasta luego! Gracias por usar MagicalStay";
    }

    private void handleDisconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Conexión cerrada con: " + socket.getInetAddress());
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket: " + e.getMessage());
        }
    }
}
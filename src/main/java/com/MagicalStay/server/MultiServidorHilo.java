package com.MagicalStay.server;

import java.net.Socket;

public class MultiServidorHilo {
    private Socket socket;

    public MultiServidorHilo(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {
        try (
            java.io.ObjectInputStream entrada = new java.io.ObjectInputStream(socket.getInputStream());
            java.io.ObjectOutputStream salida = new java.io.ObjectOutputStream(socket.getOutputStream())
        ) {
            while (true) {

                String comando = (String) entrada.readObject();

                String respuesta = procesarComando(comando);

                salida.writeObject(respuesta);
                salida.flush();

                if (comando.equalsIgnoreCase("salir")) {
                    break;
                }
            }
        } catch (java.io.EOFException e) {
            // Cliente cerró la conexión
            System.out.println("Cliente desconectado: " + socket.getInetAddress());
        } catch (Exception e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                System.out.println("Conexión cerrada con: " + socket.getInetAddress());
            } catch (java.io.IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    private String procesarComando(String comando) {
        try {

            String[] partes = comando.split("\\|");
            String accion = partes[0].toLowerCase();
            
            switch (accion) {
                case "consultar":

                    return "Consultando datos...";
                    
                case "reservar":
                    if (partes.length < 2) {
                        return "Error: Faltan parámetros para la reserva";
                    }
                    // Implementar lógica de reserva
                    return "Procesando reserva para: " + partes[1];
                    
                case "cancelar":
                    if (partes.length < 2) {
                        return "Error: Faltan parámetros para la cancelación";
                    }

                    return "Cancelando reserva: " + partes[1];
                    
                case "salir":
                    return "¡Hasta luego!";
                    
                default:
                    return "Comando no reconocido";
            }
        } catch (Exception e) {
            return "Error procesando comando: " + e.getMessage();
        }
    }
    
}
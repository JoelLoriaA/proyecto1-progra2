package com.MagicalStay.server;

import javafx.concurrent.Task;

import java.io.IOException;
import java.net.ServerSocket;

public class HotelServerManager {

    private static ServerSocket serverSocket;

    public static void startServer(){

        // Prevent starting multiple server instances
        if (serverSocket != null && !serverSocket.isClosed()) {
            System.out.println("Server is already running.");
            return;
        }
        Task<Void> backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boolean escuchando = true;

                try {
                    serverSocket = new ServerSocket(9999);
                    System.out.println("Servidor activo");
                    // Keep accepting connections as long as the socket is open
                    while (!serverSocket.isClosed()){
                        // Use try-with-resources for MultiServidorHilo if it implements Closeable
                        System.out.println(Thread.currentThread().toString());
                        ClientHandler hilo = new ClientHandler(serverSocket.accept());
                        new Thread(hilo).start(); // Start the client handler in a new thread
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                return null;
            }
        };

        new Thread(backgroundTask).start();
    }

    public static void stopServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server stopped.");
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}

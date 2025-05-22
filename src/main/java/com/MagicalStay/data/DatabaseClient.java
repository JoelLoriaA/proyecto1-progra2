package com.MagicalStay.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9999;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ObjectMapper objectMapper;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final AtomicBoolean isConnected;
    private final ExecutorService executorService;
    private ConnectionStatusListener connectionStatusListener;

    public DatabaseClient() {
        this.objectMapper = new ObjectMapper();
        this.isConnected = new AtomicBoolean(false);
        this.executorService = Executors.newCachedThreadPool();
    }

    public void setConnectionStatusListener(ConnectionStatusListener listener) {
        this.connectionStatusListener = listener;
    }

    public <T> CompletableFuture<T> executeRequest(RequestDTO request, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            int attempts = 0;
            while (attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    if (!isConnected.get()) {
                        connect();
                    }

                    String jsonRequest = objectMapper.writeValueAsString(request);
                    writer.println(jsonRequest);
                    writer.flush();

                    String response = reader.readLine();
                    if (response == null) {
                        throw new IOException("No se recibió respuesta del servidor");
                    }

                    return objectMapper.readValue(response, responseType);

                } catch (IOException e) {
                    attempts++;
                    if (attempts >= MAX_RETRY_ATTEMPTS) {
                        disconnect();
                        throw new RuntimeException("Error en la comunicación después de " + 
                            MAX_RETRY_ATTEMPTS + " intentos: " + e.getMessage(), e);
                    }
                    try {
                        Thread.sleep(1000 * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operación interrumpida", ie);
                    }
                }
            }
            throw new RuntimeException("No se pudo completar la solicitud");
        }, executorService);
    }

    private synchronized void connect() throws IOException {
        if (!isConnected.get()) {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                socket.setSoTimeout(CONNECTION_TIMEOUT);
                writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                isConnected.set(true);
                notifyConnectionStatus(true, "Conectado al servidor");
                startHeartbeat();
            } catch (IOException e) {
                disconnect();
                throw new IOException("No se pudo conectar al servidor: " + e.getMessage(), e);
            }
        }
    }

    private void notifyConnectionStatus(boolean connected, String message) {
        if (connectionStatusListener != null) {
            Platform.runLater(() -> 
                connectionStatusListener.onConnectionStatusChanged(connected, message));
        }
    }

    // Interface para el listener de estado de conexión
    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean connected, String message);
    }

    private void startHeartbeat() {
        executorService.execute(() -> {
            while (isConnected.get()) {
                try {
                    RequestDTO heartbeat = new RequestDTO("HEARTBEAT", null, null);
                    executeRequest(heartbeat, ConnectionStatus.class)
                        .thenAccept(status -> {
                            if (!status.isConnected()) {
                                handleConnectionLoss();
                            }
                        })
                        .exceptionally(throwable -> {
                            handleConnectionLoss();
                            return null;
                        });
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void handleConnectionLoss() {
        Platform.runLater(() -> {
            disconnect();
            notifyConnectionStatus(false, "Se perdió la conexión con el servidor");
        });
    }

    public synchronized void disconnect() {
        isConnected.set(false);
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            notifyConnectionStatus(false, "Desconectado del servidor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public void shutdown() {
        disconnect();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
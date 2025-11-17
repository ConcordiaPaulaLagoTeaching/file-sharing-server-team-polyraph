package ca.concordia.server;

import ca.concordia.filesystem.FileSystemManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private final FileSystemManager fsManager;
    private final int port;

    public FileServer(int port, String fileSystemName, int totalSize){
        try {
            this.fsManager = FileSystemManager.getInstance(fileSystemName, totalSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize FileSystemManager", e);
        }
        this.port = port;
    }

    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Listening on port " + port + "...");

            while (true) {
                // Accept a new client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Create ClientHandler and launch in new thread
                ClientHandler handler = new ClientHandler(clientSocket, fsManager);
                new Thread(handler).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not start server on port " + port);
        }
    }
}
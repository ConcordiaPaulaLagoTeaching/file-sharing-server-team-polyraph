package ca.concordia.server;

import ca.concordia.filesystem.FileSystemManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final FileSystemManager fsManager;

    public ClientHandler(Socket clientSocket, FileSystemManager fsManager) {
        this.clientSocket = clientSocket;
        this.fsManager = fsManager;
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received from client [" + clientSocket + "]: " + line);
                String[] parts = line.trim().split(" ", 3);
                String command = parts[0].toUpperCase();

                try {
                    switch (command) {
                        case "CREATE":
                            if (parts.length < 2) {
                                writer.println("ERROR: Missing filename.");
                                break;
                            }
                            fsManager.createFile(parts[1]);
                            writer.println("SUCCESS: File '" + parts[1] + "' created.");
                            break;

                        case "WRITE":
                            if (parts.length < 3) {
                                writer.println("ERROR: Missing filename or content.");
                                break;
                            }
                            fsManager.writeFile(parts[1], parts[2].getBytes());
                            writer.println("SUCCESS: Wrote to file '" + parts[1] + "'.");
                            break;

                        case "READ":
                            if (parts.length < 2) {
                                writer.println("ERROR: Missing filename.");
                                break;
                            }
                            byte[] data = fsManager.readFile(parts[1]);
                            writer.println(new String(data));
                            break;

                        case "DELETE":
                            if (parts.length < 2) {
                                writer.println("ERROR: Missing filename.");
                                break;
                            }
                            fsManager.deleteFile(parts[1]);
                            writer.println("SUCCESS: File '" + parts[1] + "' deleted.");
                            break;

                        case "LIST":
                            String[] files = fsManager.listFiles();
                            
                            if (files.length == 0) {
                                writer.println("No files found.");
                            } else {
                                writer.println(String.join(", ", files));
                            }
                            break;

                        case "QUIT":
                            writer.println("SUCCESS: Disconnecting.");
                            return;

                        default:
                            writer.println("ERROR: Unknown command.");
                            break;
                    }

                } catch (Exception e) {
                    writer.println("ERROR: Operation failed - " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket);
            } catch (Exception ignored) {}
        }
    }
}
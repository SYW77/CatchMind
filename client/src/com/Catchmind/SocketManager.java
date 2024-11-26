package com.Catchmind;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class SocketManager {
    private static SocketManager instance; // 싱글톤
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String serverAddress;
    private int port;

    private SocketManager(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        connect();
    }

    public static SocketManager getInstance(String serverAddress, int port) {
        if (instance == null) {
            instance = new SocketManager(serverAddress, port);
        }
        return instance;
    }

    private void connect() {
        try {

            socket = new Socket(serverAddress, port);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcomeMessage = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Server connection failed: " + e.getMessage());
        }
    }

    public void sendNickname(String username) {
        if (out != null) {
            out.println(username + "\n"); // \n Terminating
        }
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

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

    private Thread listenerThread; // 수신 스레드

    private SocketManager(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        connect();
    }

    public static SocketManager getInstance() {
        return instance;
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
            System.out.println(welcomeMessage);

            startListening();
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

    public void sendDrawing(String base64Drawing) {
        if (out != null) {
            System.out.println("DRAW:" + base64Drawing);
            out.println("DRAW:" + base64Drawing); // DRAW: prefix 추가
        }
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    // 서버로부터 받은 메시지를 처리
                    handleReceivedMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenerThread.start();
    }

    // 서버로부터 받은 메시지를 처리
    private void handleReceivedMessage(String message) {
        // 예: DRAW:base64EncodedDrawing
        if (message.startsWith("DRAW:")) {
            String base64Drawing = message.substring(5); // "DRAW:" 이후 부분을 추출
            onReceiveDrawing(base64Drawing); // 그림 받기
        } else if (message.startsWith("MSG:")) {
            String chatMessage = message.substring(4); // "MSG:" 이후 부분을 추출
            onReceiveMessage(chatMessage); // 일반 메시지 처리
        }
    }

    // 그림을 받았을 때 호출되는 메서드
    private void onReceiveDrawing(String base64Drawing) {
        // `DrawingPanel`에 그림을 표시하기 위해 메서드를 호출
        SwingUtilities.invokeLater(() -> {
            DrawingPanel drawingPanel = DrawingPanel.getInstance();
            if (drawingPanel != null) {
                drawingPanel.setDrawingFromServer(base64Drawing);
            }
        });
    }

    // 일반 메시지를 수신했을 때 호출되는 메서드 (예: 채팅 메시지)
    private void onReceiveMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Received message: " + message);
        });
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

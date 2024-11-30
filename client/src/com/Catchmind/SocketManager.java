package com.Catchmind;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.function.Consumer;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.List;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class SocketManager {
    private static SocketManager instance; // 싱글톤
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String serverAddress;
    private int port;

    private Consumer<String> keywordListener; // 제시어 수신 리스너

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

            // 연결 시 Game not yet started 상태 표시
            SwingUtilities.invokeLater(() -> {
                GameScreen1.showGameNotStarted();
            });

            String welcomeMessage = in.readLine();
            System.out.println(welcomeMessage);

            startListening();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Server connection failed: " + e.getMessage());
        }
    }

    public void setKeywordListener(Consumer<String> listener) {
        this.keywordListener = listener;
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

    // 선 데이터 전송을 위한 메서드 추가
    public void sendLine(String lineData) {
        if (out != null) {
            System.out.println("Sending line data: " + lineData);
            out.println(lineData); // 선 데이터 전송
        }
    }

    // Reset 신호 전송을 위한 메서드
    public void sendReset() {
        if (out != null) {
            out.println("RESET");
        }
    }

    // 채팅 메시지 전송
    public void sendChat(String message) {
        if (out != null) {
            out.println("CHAT:" + message);
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
        if (message.startsWith("LINE:")) {
            String lineData = message.substring(5);
            onReceiveLine(lineData);
        } else if (message.startsWith("LINES:")) {
            String linesData = message.substring(6);
            onReceiveLines(linesData);
        } else if (message.startsWith("RESET")) {
            onReceiveReset();
        } else if (message.startsWith("COMPRESSED:")) {
            String compressedData = message.substring(11);
            onReceiveCompressedLines(compressedData);
        } else if (message.startsWith("MSG:")) {
            String chatMessage = message.substring(4); // "MSG:" 이후 부분을 추출
            onReceiveMessage(chatMessage); // 일반 메시지 처리
        } else if (message.startsWith("TIMER:")) {
            String timerValue = message.substring(6);
            onReceiveTimer(timerValue);
        } else if (message.startsWith("HINT:")) {
            String hint = message.substring(5);
            onReceiveHint(hint);
        } else if (message.startsWith("WORD:")) {
            String keyword = message.substring(5); // "WORD:" 이후 부분을 추출
            onReceiveWord(keyword);
            if (keywordListener != null) {
                keywordListener.accept(keyword); // 키워드 리스너 호출
            }
        } else if (message.startsWith("CHAT:")) {
            String chatMessage = message.substring(5);
            onReceiveChat(chatMessage);
        } else if (message.startsWith("ROUND:")) {
            int round = Integer.parseInt(message.substring(6));
            onReceiveRound(round);
        } else if (message.startsWith("DRAWER:")) {
            String drawerName = message.substring(7);
            onReceiveDrawer(drawerName);
        } else if (message.equals("GAME_START")) {
            onGameStart();
        } else if (message.equals("GAME_END")) {
            onGameEnd();
        } else if (message.startsWith("SCORES:")) {
            String scoresJson = message.substring(7);
            onReceiveScores(scoresJson);
        } else if (message.equals("NEXT_ROUND")) {
            onReceiveNextRound();
        }
    }

    // 힌트를 받았을 때 호출되는 메서드
    private static void onReceiveHint(String hint) {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.setHint("Hint: " + hint);
        });
    }

    // 타이머 메시지를 받았을 때 호출되는 메서드(타이머 시작)
    private static void onReceiveTimer(String timerValue) {
        try {
            int seconds = Integer.parseInt(timerValue);
            SwingUtilities.invokeLater(() -> {
                GameScreen1.updateTimer(seconds);
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // 키워드를 받았을 때 호출되는 메서드(출제자에게 전달)
    private void onReceiveWord(String keyword) {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.setKeyword(keyword); // 키워드 업데이트
        });
    }

    // 일반 메시지를 수신했을 때 호출되는 메서드 (예: 채팅 메시지)
    private void onReceiveMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Received message: " + message);
            // 시스템 메시지도 채팅창에 표시 (빨간색으로)
            GameScreen1.addSystemMessage(message);
        });
    }

    // 단일 선 데이터를 받았을 때 호출되는 메서드
    private void onReceiveLine(String lineData) {
        SwingUtilities.invokeLater(() -> {
            DrawingPanel drawingPanel = DrawingPanel.getInstance();
            if (drawingPanel != null) {
                drawingPanel.drawLineFromServer(lineData);
            }
        });
    }

    // 여러 선 데이터를 받았을 때 호출되는 메서드
    private void onReceiveLines(String linesData) {
        SwingUtilities.invokeLater(() -> {
            DrawingPanel drawingPanel = DrawingPanel.getInstance();
            if (drawingPanel != null) {
                String[] lines = linesData.split(";");
                for (String lineData : lines) {
                    if (!lineData.isEmpty()) {
                        drawingPanel.drawLineFromServer(lineData);
                    }
                }
            }
        });
    }

    // 압축된 선 데이터를 받았을 때 호출되는 메서드
    private void onReceiveCompressedLines(String compressedData) {
        try {
            byte[] decodedData = Base64.getDecoder().decode(compressedData);
            ByteArrayInputStream bais = new ByteArrayInputStream(decodedData);
            GZIPInputStream gzipIn = new GZIPInputStream(bais);
            ObjectInputStream objectIn = new ObjectInputStream(gzipIn);

            @SuppressWarnings("unchecked")
            List<Line> decompressedLines = (List<Line>) objectIn.readObject();
            objectIn.close();

            SwingUtilities.invokeLater(() -> {
                DrawingPanel drawingPanel = DrawingPanel.getInstance();
                if (drawingPanel != null) {
                    for (Line line : decompressedLines) {
                        drawingPanel.drawLineFromServer(
                                line.start.x + "," + line.start.y + "," +
                                        line.end.x + "," + line.end.y + "," +
                                        line.color.getRGB());
                    }
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Reset 신호를 받았을 때 호출되는 메서드
    private void onReceiveReset() {
        SwingUtilities.invokeLater(() -> {
            DrawingPanel drawingPanel = DrawingPanel.getInstance();
            if (drawingPanel != null) {
                drawingPanel.clearDrawing();
            }
        });
    }

    // 채팅 메시지를 받았을 때 호출되는 메서드
    private void onReceiveChat(String message) {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.addChatMessage(message);
        });
    }

    private void onReceiveRound(int round) {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.updateRound(round);
        });
    }

    private void onReceiveDrawer(String drawerName) {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.updateDrawer(drawerName);
        });
    }

    private void onGameStart() {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.startGameTimer();
        });
    }

    private void onGameEnd() {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.showGameEnded();
        });
    }

    private void onReceiveScores(String scoresJson) {
        try {
            // JSON 파싱 (간단한 구현)
            scoresJson = scoresJson.substring(1, scoresJson.length() - 1); // {} 제거
            Map<String, Integer> scores = new HashMap<>();

            if (!scoresJson.isEmpty()) {
                String[] entries = scoresJson.split(",");
                for (String entry : entries) {
                    String[] parts = entry.split(":");
                    String name = parts[0].replace("\"", "");
                    int score = Integer.parseInt(parts[1]);
                    scores.put(name, score);
                }
            }

            SwingUtilities.invokeLater(() -> {
                GameScreen1.updateLeaderboard(scores);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void onReceiveNextRound() {
        SwingUtilities.invokeLater(() -> {
            GameScreen1.amoveToNextRound();
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

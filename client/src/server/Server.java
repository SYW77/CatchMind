package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 3000;
    private static final int MAX_PLAYERS = 2;
    private static final int TOTAL_ROUNDS = 2;
    private static Set<PlayerHandler> playerHandlers = new CopyOnWriteArraySet<>();
    private static List<String> wordList = new ArrayList<>(Arrays.asList("apple", "banana", "cat", "dog", "elephant"));
    private static Set<String> usedWords = new HashSet<>();
    private static String currentWord;
    private static PlayerHandler currentDrawer;
    private static Iterator<PlayerHandler> playerIterator;
    private static int currentRound = 0;
    private static Map<String, Integer> playerScores = new HashMap<>();
    private static ScheduledExecutorService roundTimer = Executors.newSingleThreadScheduledExecutor();
    private static Future<?> currentRoundTask;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game server started on port " + PORT);
            
            while (true) {
                if (playerHandlers.size() < MAX_PLAYERS) {
                    Socket playerSocket = serverSocket.accept();
                    PlayerHandler playerHandler = new PlayerHandler(playerSocket);
                    playerHandlers.add(playerHandler);
                    new Thread(playerHandler).start();
                    broadcastMessage("MSG: A new player has joined the game! Current players: " + playerHandlers.size());
                }
                if (playerHandlers.size() == MAX_PLAYERS && currentRound == 0) {
                    broadcastMessage("MSG: All players have joined! The game will begin.");
                    initiateNextRound();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            roundTimer.shutdown(); // 서버 종료 시 타이머 종료
        }
    }

    private static void initiateNextRound() {
        if (currentRound >= TOTAL_ROUNDS) {
            broadcastMessage("MSG: The game has ended!");
            displayScores();
            roundTimer.shutdown(); // 타이머 종료
            return;
        }
        currentRound++;
        if (playerIterator == null || !playerIterator.hasNext()) {
            playerIterator = playerHandlers.iterator();
        }
        if (playerIterator.hasNext()) {
            currentDrawer = playerIterator.next();
            selectNewWord();
            sendCurrentWordToDrawer(currentDrawer); // 출제자에게 제시어 전송
            broadcastMessage("PAINTER:" + currentDrawer.getName()); // 출제자 정보를 모든 클라이언트에 브로드캐스트
            broadcastMessageExcept(currentDrawer, "MSG: Round " + currentRound + " has started! " + currentDrawer.getName() + " is drawing.");

            // 라운드 타이머 시작
            broadcastMessage("Timer: Timer has started!");
            if (currentRoundTask != null) {
                currentRoundTask.cancel(true); // 이전 타이머 취소
            }
            currentRoundTask = roundTimer.schedule(() -> {
                broadcastMessage("MSG: Time out! The correct word was: " + currentWord);
                initiateNextRound(); // 다음 라운드 진행
            }, 30, TimeUnit.SECONDS);
            
            // 20초 후 힌트 방송 (남은 시간 10초일 때)
            roundTimer.schedule(() -> {
                showHint(currentWord);
            }, 20, TimeUnit.SECONDS);
            
        }
    }

    private static void selectNewWord() {
        List<String> availableWords = new ArrayList<>(wordList);
        availableWords.removeAll(usedWords);

        if (availableWords.isEmpty()) {
            usedWords.clear();
            availableWords = new ArrayList<>(wordList);
        }

        currentWord = availableWords.get(new Random().nextInt(availableWords.size()));
        usedWords.add(currentWord);
    }

    private static void sendCurrentWordToDrawer(PlayerHandler drawer) {
        if (drawer != null) {
            drawer.sendMessage("WORD:" + currentWord);
        }
    }

    private static void displayScores() {
        StringBuilder scoreMessage = new StringBuilder("MSG: Final scores:\n");
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            scoreMessage.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        broadcastMessage(scoreMessage.toString());
    }

    private static class PlayerHandler implements Runnable {
        private Socket socket;
        private BufferedWriter out;
        private BufferedReader in;
        private String playerName;

        public PlayerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                sendMessage("MSG: Welcome to the game!");
                playerName = in.readLine();
                System.out.println(playerName + "has connected");
                broadcastMessage("MSG: " + playerName + " has joined the game! Current players: " + playerHandlers.size());
                playerScores.put(playerName, 0);

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("CHAT:")) {
                        String message = input.substring(5);
                        if (message.equalsIgnoreCase(currentWord)) {
                            broadcastMessage("MSG: " + playerName + " has guessed the word correctly! The word was: " + currentWord);
                            playerScores.put(playerName, playerScores.get(playerName) + 1);

                            // 정답을 맞췄으므로 타이머 취소
                            if (currentRoundTask != null) {
                                currentRoundTask.cancel(true);
                            }

                            initiateNextRound();
                        } else {
                            broadcastMessage("CHAT: " + playerName + ": " + message);
                        }
                    } else if (input.startsWith("DRAW:")) {
                        String base64Drawing = input.substring(5);
                        broadcastDrawing(base64Drawing, this);
                    }
                }
            } catch (IOException e) {
                System.out.println(playerName + " has disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playerHandlers.remove(this);
                broadcastMessage("MSG: A player has left the game. Current players: " + playerHandlers.size());
                if (currentDrawer == this) {
                    initiateNextRound();
                }
            }
        }

        public void sendMessage(String message) {
            try {
                if (out != null) {
                    out.write(message + "\n");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return playerName;
        }

        private void broadcastDrawing(String base64Drawing, PlayerHandler sender) {
            for (PlayerHandler handler : playerHandlers) {
                if (handler != sender) {
                    handler.sendMessage("DRAW:" + base64Drawing);
                }
            }
        }
    }

    private static void broadcastMessage(String message) {
        for (PlayerHandler handler : playerHandlers) {
            handler.sendMessage(message);
        }
    }

    private static void broadcastMessageExcept(PlayerHandler except, String message) {
        for (PlayerHandler handler : playerHandlers) {
            if (handler != except) {
                handler.sendMessage(message);
            }
        }
    }
    
    private static void showHint(String currentWord) {
    	int count;
    	count = currentWord.length();
    	String outputString = "";
    	for(int i=0;i<count;i++) {
    		outputString+="*";
    	}
    	broadcastMessage("MSG: "+outputString);
    }
    
    
}

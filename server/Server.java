package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 3000;
    private static final int MAX_PLAYERS = 5;
    private static final int TOTAL_ROUNDS = 10;
    private static Set<PlayerHandler> playerHandlers = new HashSet<>();
    private static List<String> wordList = new ArrayList<>(Arrays.asList("apple", "banana", "cat", "dog", "elephant"));
    private static Set<String> usedWords = new HashSet<>();
    private static String currentWord;
    private static PlayerHandler currentDrawer;
    private static Iterator<PlayerHandler> playerIterator;
    private static int currentRound = 0;
    private static Map<String, Integer> playerScores = new HashMap<>();

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
        }
    }

    private static void initiateNextRound() {
        if (currentRound >= TOTAL_ROUNDS) {
            broadcastMessage("MSG: The game has ended!");
            displayScores();
            return;
        }
        currentRound++;
        if (playerIterator == null || !playerIterator.hasNext()) {
            playerIterator = playerHandlers.iterator();
        }
        if (playerIterator.hasNext()) {
            currentDrawer = playerIterator.next();
            selectNewWord();
            currentDrawer.sendMessage("MSG: Your word is: " + currentWord);
            broadcastMessageExcept(currentDrawer, "MSG: Round " + currentRound + " has started! " + currentDrawer.getName() + " is drawing.");
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

                sendMessage("MSG: Welcome to the game! Please enter your name:");
                playerName = in.readLine();
                broadcastMessage("MSG: " + playerName + " has joined the game! Current players: " + playerHandlers.size());
                playerScores.put(playerName, 0);

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("CHAT:")) {
                        String message = input.substring(5);
                        if (message.equalsIgnoreCase(currentWord)) {
                            broadcastMessage("MSG: " + playerName + " has guessed the word correctly! The word was: " + currentWord);
                            playerScores.put(playerName, playerScores.get(playerName) + 1);
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
                out.write(message + "\n");
                out.flush();
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
}

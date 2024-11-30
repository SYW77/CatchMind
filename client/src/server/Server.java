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
    private static int timeRemaining = 30;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game server started on port " + PORT);

            while (true) {
                if (playerHandlers.size() < MAX_PLAYERS) {
                    Socket playerSocket = serverSocket.accept();
                    PlayerHandler playerHandler = new PlayerHandler(playerSocket);
                    playerHandlers.add(playerHandler);
                    new Thread(playerHandler).start();
                    broadcastMessage(
                            "MSG: A new player has joined the game! Current players: " + playerHandlers.size());
                }
                if (playerHandlers.size() == MAX_PLAYERS && currentRound == 0) {
                    broadcastMessage("MSG: All players have joined! Game will start in 3 seconds...");

                    broadcastMessage("TIMER:3");
                    Thread.sleep(1000);
                    broadcastMessage("TIMER:2");
                    Thread.sleep(1000);
                    broadcastMessage("TIMER:1");
                    Thread.sleep(1000);

                    broadcastMessage("GAME_START");
                    Thread.sleep(500);

                    initiateNextRound();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            roundTimer.shutdown();
        }
    }

    private static void initiateNextRound() {
        if (currentRound >= TOTAL_ROUNDS) {
            broadcastMessage("MSG: The game has ended!");
            broadcastMessage("GAME_END");
            roundTimer.shutdown();
            return;
        }
        currentRound++;
        if (playerIterator == null || !playerIterator.hasNext()) {
            playerIterator = playerHandlers.iterator();
        }
        if (playerIterator.hasNext()) {
            broadcastMessage("RESET");

            currentDrawer = playerIterator.next();
            selectNewWord();

            broadcastMessage("ROUND:" + currentRound);
            currentDrawer.sendMessage("WORD:" + currentWord);
            broadcastMessage("DRAWER:" + currentDrawer.getName());
            broadcastMessageExcept(currentDrawer,
                    "MSG: Round " + currentRound + " has started! " + currentDrawer.getName() + " is drawing.");

            timeRemaining = 30;
            if (currentRoundTask != null) {
                currentRoundTask.cancel(true);
            }
            startTimer();
        }
        broadcastScores();
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

    private static void broadcastScores() {
        StringBuilder scoresJson = new StringBuilder("SCORES:");
        scoresJson.append("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            if (!first) {
                scoresJson.append(",");
            }
            scoresJson.append("\"").append(entry.getKey()).append("\":")
                    .append(entry.getValue());
            first = false;
        }
        scoresJson.append("}");

        broadcastMessage(scoresJson.toString());
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
                broadcastMessage(
                        "MSG: " + playerName + " has joined the game! Current players: " + playerHandlers.size());

                playerScores.put(playerName, 0);
                broadcastScores();

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("CHAT:")) {
                        String message = input.substring(5);
                        if (message.equalsIgnoreCase(currentWord)) {
                            broadcastMessage("MSG:" + playerName + " has guessed the word correctly! The word was: "
                                    + currentWord);
                            playerScores.put(playerName, playerScores.get(playerName) + 1);
                            broadcastScores();

                            if (currentRoundTask != null) {
                                currentRoundTask.cancel(true);
                            }
                            initiateNextRound();
                            broadcastMessage("NEXT_ROUND");
                        } else {
                            broadcastMessage("CHAT:" + playerName + ": " + message);
                        }
                    } else if (input.startsWith("RESET")) {
                        broadcastReset(this);
                    } else if (input.startsWith("LINE:")) {
                        String lineData = input.substring(5);
                        broadcastLine(lineData, this);
                    } else if (input.startsWith("LINES:")) {
                        String linesData = input.substring(6);
                        broadcastLines(linesData, this);
                    } else if (input.startsWith("COMPRESSED:")) {
                        String compressedData = input.substring(11);
                        broadcastCompressedLines(compressedData, this);
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
                playerScores.remove(playerName);
                broadcastMessage("MSG: A player has left the game. Current players: " + playerHandlers.size());
                broadcastScores();
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

        private void broadcastLine(String lineData, PlayerHandler sender) {
            if (sender != currentDrawer)
                return;

            for (PlayerHandler handler : playerHandlers) {
                if (handler != sender) {
                    handler.sendMessage("LINE:" + lineData);
                }
            }
        }

        private void broadcastLines(String linesData, PlayerHandler sender) {
            if (sender != currentDrawer)
                return;

            for (PlayerHandler handler : playerHandlers) {
                if (handler != sender) {
                    handler.sendMessage("LINES:" + linesData);
                }
            }
        }

        private void broadcastCompressedLines(String compressedData, PlayerHandler sender) {
            if (sender != currentDrawer)
                return;

            for (PlayerHandler handler : playerHandlers) {
                if (handler != sender) {
                    handler.sendMessage("COMPRESSED:" + compressedData);
                }
            }
        }

        private void broadcastReset(PlayerHandler sender) {
            if (sender != currentDrawer)
                return;

            for (PlayerHandler handler : playerHandlers) {
                if (handler != sender) {
                    handler.sendMessage("RESET");
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
        StringBuilder hint = new StringBuilder();
        hint.append("Hint: ");
        for (int i = 0; i < currentWord.length(); i++) {
            hint.append("*");
        }
        broadcastMessage("HINT:" + hint.toString());
    }

    private static void startTimer() {
        currentRoundTask = roundTimer.scheduleAtFixedRate(() -> {
            if (timeRemaining > 0) {
                broadcastMessage("TIMER:" + timeRemaining);

                if (timeRemaining == 10) {
                    StringBuilder hint = new StringBuilder();
                    for (int i = 0; i < currentWord.length(); i++) {
                        hint.append("*");
                    }
                    broadcastMessage("HINT:" + hint.toString());
                }

                timeRemaining--;
            } else {
                broadcastMessage("MSG: Time out! The correct word was: " + currentWord);
                currentRoundTask.cancel(true);
                initiateNextRound();
                broadcastMessage("NEXT_ROUND");
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
}

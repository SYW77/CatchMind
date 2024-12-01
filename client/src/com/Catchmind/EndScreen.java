package com.Catchmind;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

public class EndScreen {

    private JFrame frame;
    private JTextPane playerInfoArea; // 플레이어 정보를 표시할 텍스트 영역


    public EndScreen() {
        initialize();
        frame.setVisible(true);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 771, 512);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.getContentPane().setLayout(null);

        JLabel resultLabel = new JLabel("Game Result");
        resultLabel.setBounds(227, 41, 314, 57);
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setFont(new Font("맑은 고딕", Font.BOLD, 36));
        frame.getContentPane().add(resultLabel);

        JPanel playerInfoPanel = new JPanel();
        playerInfoPanel.setBounds(114, 123, 521, 226);
        playerInfoPanel.setBackground(new Color(255, 230, 200));
        frame.getContentPane().add(playerInfoPanel);
        playerInfoPanel.setLayout(null);

        // 플레이어 정보를 표시하는 텍스트 영역 (JTextPane 사용)
        playerInfoArea = new JTextPane();
        playerInfoArea.setEditable(false);
        playerInfoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
        playerInfoArea.setBackground(new Color(255, 255, 255)); // 배경색 설정

        // JScrollPane에 추가
        JScrollPane scrollPane = new JScrollPane(playerInfoArea);
        scrollPane.setBounds(0, 0, 521, 226);
        playerInfoPanel.add(scrollPane);

        JButton endButton = new JButton("End");
        endButton.setBounds(329, 380, 108, 29);
        endButton.setHorizontalAlignment(SwingConstants.CENTER);
        endButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        frame.getContentPane().add(endButton);

        // 게임 종료
        endButton.addActionListener(e -> {
            frame.dispose();
        });
    }
    
    public static void showScores(String scoresJson) {
        SwingUtilities.invokeLater(() -> {
            EndScreen endScreen = new EndScreen();
            StringBuilder leaderboard = new StringBuilder();

            // 점수 데이터를 파싱 및 정렬
            String processedScoresJson = scoresJson.substring(1, scoresJson.length() - 1); // {} 제거
            Map<String, Integer> scores = new HashMap<>();
            if (!processedScoresJson.isEmpty()) {
                String[] entries = processedScoresJson.split(",");
                for (String entry : entries) {
                    String[] parts = entry.split(":");
                    String name = parts[0].replace("\"", "");
                    int score = Integer.parseInt(parts[1]);
                    scores.put(name, score);
                }
            }

            // 점수 정렬
            List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
            sortedScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // 정렬된 점수를 StringBuilder에 추가
            for (Map.Entry<String, Integer> entry : sortedScores) {
                leaderboard.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            // JTextPane에 점수 데이터 표시
            endScreen.playerInfoArea.setText(leaderboard.toString());
        });
    }

}

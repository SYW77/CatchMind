package com.Catchmind;

import java.awt.*;
import javax.swing.*;
import java.sql.*;
import javax.swing.text.*;

public class EndScreen {

    private JFrame frame;
    private JTextPane playerInfoArea; // 플레이어 정보를 표시할 텍스트 영역

    // MySQL 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CatchmindDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ekdud0412?";

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    EndScreen window = new EndScreen();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public EndScreen() {
        initialize();
        fetchAndDisplayPlayerInfo(); // 데이터베이스에서 플레이어 정보를 가져와 표시
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

    // 데이터베이스에서 플레이어 정보를 가져와 표시
    private void fetchAndDisplayPlayerInfo() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 점수를 기준으로 내림차순 정렬
            String sql = "SELECT username, score FROM Users ORDER BY score DESC";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // JTextPane에서 가운데 정렬을 위한 StyledDocument 설정
            StyledDocument doc = playerInfoArea.getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);

            StringBuilder playerInfo = new StringBuilder("<Rank>\n");
            int rank = 1; // 초기 등수 설정
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                int score = resultSet.getInt("score");
                playerInfo.append(rank).append("위: ").append(username).append(" - ").append(score).append("point\n");
                rank++; // 다음 순위로 이동
            }

            // JTextPane에 텍스트 설정
            playerInfoArea.setText(playerInfo.toString());
            doc.setParagraphAttributes(0, doc.getLength(), center, false); // 텍스트를 다시 가운데 정렬
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

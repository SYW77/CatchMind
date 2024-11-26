package com.Catchmind; 

import java.awt.*;
import javax.swing.*;
import java.sql.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;

public class GameScreen1 {

	private JFrame frame;
    private JTextArea playerInfoArea; // 플레이어 정보를 표시할 텍스트 영역
    private JLabel keywordLabel; // 제시어를 표시할 라벨
    private JLabel hintLabel; // 힌트를 표시할 라벨
    private String hint; // 힌트를 저장할 변수
    private JLabel selectedUserLabel; // 출제자를 표시할 라벨
    private String selectedUser; // 출제자를 저장할 변수
    private JLabel timerLabel; // 타이머를 표시할 라벨
    private JPanel timerPanel; // 타이머 패널
    private int timeRemaining = 30; // 제한 시간 (초)
    private Color currentColor = Color.BLACK; // 현재 선택된 색상
    private List<Line> lines = new ArrayList<>(); // 그림 데이터를 저장
    private DrawingPanel drawingPanel; // 그림판
    

    // MySQL 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CatchmindDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ekdud0412?";

    public GameScreen1(String username) {
        initialize(username);
        fetchAndDisplayPlayerInfo(); // 데이터베이스에서 플레이어 정보를 가져와 표시
        fetchRandomKeywordAndDisplay(); // 랜덤 키워드 가져와 표시
        startTimer(); // 타이머 시작
    }

    private void initialize(String username) {
        frame = new JFrame();
        frame.setBounds(100, 100, 771, 512);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.getContentPane().setLayout(null);
        

        JButton endButton = new JButton("End");
        endButton.setBounds(640, 20, 100, 30);
        endButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        frame.getContentPane().add(endButton);

        // 게임 종료 버튼(유저 정보 삭제)
        endButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Leave the game?",
                "Game end",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
            	// 데이터베이스에서 사용자 삭제
                deleteUserFromDatabase(username);
                frame.dispose(); // 현재 창 닫기
                EventQueue.invokeLater(() -> {
                    new StartScreen(); // 시작 화면으로 돌아가기
                });
            }
        });
        
        // 제시어 표시 영역
        keywordLabel = new JLabel(" Keyword: ???"); // 기본 제시어
        keywordLabel.setBounds(20, 20, 200, 30);
        keywordLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        frame.getContentPane().add(keywordLabel);
        
        // 힌트 패널
        JPanel hintPanel = new JPanel();
        hintPanel.setBounds(20, 322, 500, 38);
        hintPanel.setBackground(new Color(255, 250, 200));
        frame.getContentPane().add(hintPanel);
        hintPanel.setLayout(null);

        // 힌트 기본 라벨
        JLabel hintLabel_basic = new JLabel("Hint: ");
        hintLabel_basic.setBounds(20, 3, 500, 30);
        hintLabel_basic.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        hintLabel_basic.setVisible(true);
        hintPanel.add(hintLabel_basic);
        
        // 힌트 라벨
        hintLabel = new JLabel("");
        hintLabel.setBounds(20, 3, 500, 30);
        hintLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        hintLabel.setVisible(false); // 기본적으로 힌트 숨김
        hintPanel.add(hintLabel);

        // 그림판 툴 영역
        JPanel toolPanel = new JPanel();
        toolPanel.setBounds(232, 20, 288, 30);
        toolPanel.setBackground(new Color(255, 255, 255));
        frame.getContentPane().add(toolPanel);
        toolPanel.setLayout(null);

        // 그림판 툴 버튼들
        JButton colorButton_Black = new JButton("");
        colorButton_Black.setBounds(19, 5, 20, 20);
        colorButton_Black.setBackground(Color.BLACK); // 버튼의 배경을 검정색으로 설정
        colorButton_Black.setOpaque(true); // 불투명 설정으로 배경색이 보이도록 처리
        colorButton_Black.setBorderPainted(false); // 테두리 없애기 (선택 사항)
        toolPanel.add(colorButton_Black);
        colorButton_Black.addActionListener(e -> {
        	currentColor = Color.BLACK; // 현재 색상을 검정색으로 설정
        	drawingPanel.setColor(currentColor); // DrawingPanel에도 색상 업데이트
        });
        
        JButton colorButton_Red = new JButton("");
        colorButton_Red.setBounds(51, 5, 20, 20);
        colorButton_Red.setBackground(Color.RED);
        colorButton_Red.setOpaque(true);
        colorButton_Red.setBorderPainted(false);
        toolPanel.add(colorButton_Red);
        colorButton_Red.addActionListener(e -> {
        	currentColor = Color.RED;
        	drawingPanel.setColor(currentColor);
        });
        
        JButton colorButton_Blue = new JButton("");
        colorButton_Blue.setBounds(83, 5, 20, 20);
        colorButton_Blue.setBackground(Color.BLUE);
        colorButton_Blue.setOpaque(true);
        colorButton_Blue.setBorderPainted(false);
        toolPanel.add(colorButton_Blue);
        colorButton_Blue.addActionListener(e -> {
            currentColor = Color.BLUE;
            drawingPanel.setColor(currentColor);
        });
        
        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(207, 5, 69, 20);
        resetButton.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        toolPanel.add(resetButton);
        resetButton.addActionListener(e -> {
        	lines.clear(); // 모든 그림 데이터 삭제
            drawingPanel.repaint(); // 그림판 새로고침
        });
        
        
        // 출제자인 경우
        // keywordLabel.setVisible(true);
        // toolPanel.setVisible(true);
        
        

        // 중앙: 그림 표시 패널

        drawingPanel = DrawingPanel.getInstance(lines, currentColor);
        drawingPanel.setBounds(20, 60, 500, 252);
        drawingPanel.setBackground(new Color(255, 255, 255));
        // 패널에 테두리 추가 (검정색 테두리, 두께 2)
        drawingPanel.setBorder(new LineBorder(Color.BLACK, 2));
        frame.getContentPane().add(drawingPanel);

        JLabel drawingLabel = new JLabel("Drawing Board");
        drawingLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        drawingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        drawingPanel.setLayout(new BorderLayout());
        drawingPanel.add(drawingLabel, BorderLayout.CENTER);
        
        
        
        // 우측 상단: 라운드 정보 패널
        JPanel roundInfoPanel = new JPanel();
        roundInfoPanel.setBounds(540, 60, 200, 48);
        roundInfoPanel.setBackground(new Color(255, 255, 255));
        frame.getContentPane().add(roundInfoPanel);
        roundInfoPanel.setLayout(null);
        
        // 라운드를 표시하는 텍스트 영역
        JLabel roundLabel = new JLabel("Round");
        roundLabel.setBounds(0, 0, 200, 24);
        roundLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        roundLabel.setHorizontalAlignment(SwingConstants.LEFT);
        roundInfoPanel.add(roundLabel);
        
        // 출제자를 표시하는 텍스트 영역
        selectedUserLabel = new JLabel("  Painter: ???");
        selectedUserLabel.setBounds(0, 24, 200, 24);
        selectedUserLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        selectedUserLabel.setHorizontalAlignment(SwingConstants.LEFT);
        roundInfoPanel.add(selectedUserLabel);
        
        // 랜덤 유저 선택 및 round info에 출력
        selectRandomUserAndDisplay();
        
        // 우측 중앙: 플레이어 정보 패널
        JPanel playerInfoPanel = new JPanel();
        playerInfoPanel.setBounds(540, 118, 200, 106);
        playerInfoPanel.setBackground(new Color(255, 230, 200));
        frame.getContentPane().add(playerInfoPanel);
        playerInfoPanel.setLayout(null);

        // 플레이어 정보를 표시하는 텍스트 영역
        playerInfoArea = new JTextArea();
        playerInfoArea.setEditable(false);
        playerInfoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        playerInfoArea.setLineWrap(true);
        playerInfoArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(playerInfoArea);
        scrollPane.setBounds(0, 0, 200, 106);
        playerInfoPanel.add(scrollPane);

        // 우측 하단: 채팅 창 패널
        JPanel chatPanel = new JPanel();
        chatPanel.setBounds(540, 234, 200, 126);
        chatPanel.setBackground(new Color(240, 240, 240));
        frame.getContentPane().add(chatPanel);
        chatPanel.setLayout(null);

        JLabel chatLabel = new JLabel("Chat");
        chatLabel.setBounds(0, 0, 200, 140);
        chatLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chatPanel.add(chatLabel);

        // 하단: 타이머 및 답변 입력 영역
        timerPanel = new JPanel();
        timerPanel.setBounds(20, 370, 720, 35);
        timerPanel.setBackground(new Color(200, 220, 255));
        frame.getContentPane().add(timerPanel);

        timerLabel = new JLabel("Remaining Time: 30sec"); // 타이머 라벨 초기화
        timerLabel.setForeground(Color.black);
        timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerPanel.setLayout(new BorderLayout());
        timerPanel.add(timerLabel, BorderLayout.CENTER);

        // 답변 입력 필드와 제출 버튼
        JTextField answerField = new JTextField();
        answerField.setBounds(20, 420, 600, 30);
        frame.getContentPane().add(answerField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(640, 420, 100, 30);
        submitButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        frame.getContentPane().add(submitButton);

        submitButton.addActionListener(e -> {
            String answer = answerField.getText();
            if (!answer.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "제출한 답변: " + answer);
                answerField.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "답변을 입력해주세요.");
            }
        });

        frame.setVisible(true);
    }
    
    
    // 게임 종료시 DB에서 유저 정보 삭제
    private void deleteUserFromDatabase(String username) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM Users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.executeUpdate();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 랜덤 유저 선택
    private void selectRandomUserAndDisplay() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 랜덤으로 is_used가 false인 유저 1명 선택
            String selectSql = "SELECT username FROM Users WHERE is_used = false ORDER BY RAND() LIMIT 1";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String selectedUser = resultSet.getString("username");
                selectedUserLabel.setText("Painter: " + selectedUser);
                
                // 선택된 유저의 is_used를 true로 업데이트
                String updateSql = "UPDATE Users SET is_used = true WHERE username = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, selectedUser);
                updateStatement.executeUpdate();

            } else {
            	selectedUserLabel.setText("Painter: No available users");
                JOptionPane.showMessageDialog(frame, "No users available for this round.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 데이터베이스에서 랜덤으로 키워드 가져오기
    // 게임이 종료되면 is_used를 모두 false로 변경하는 코드 추가해야함
    private void fetchRandomKeywordAndDisplay() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT keyword, hint FROM keyword WHERE is_used = false ORDER BY RAND() LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String randomKeyword = resultSet.getString("keyword");
                hint = resultSet.getString("hint");
                
                // 제시어를 keywordLabel에 표시
                keywordLabel.setText(" Keyword: " + randomKeyword);

                // is_used 업데이트
                String updateSql = "UPDATE keyword SET is_used = true WHERE keyword = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, randomKeyword);
                updateStatement.executeUpdate();
            } else {
                JOptionPane.showMessageDialog(frame, "사용 가능한 키워드가 없습니다!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    // 데이터베이스에서 플레이어 정보를 가져와 표시
    private void fetchAndDisplayPlayerInfo() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT username, score FROM Users"; // Users 테이블에서 이름과 점수 가져오기
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder playerInfo = new StringBuilder("Player Info:\n");
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                int score = resultSet.getInt("score");
                playerInfo.append(username).append(" - ").append(score).append("point\n");
            }

            playerInfoArea.setText(playerInfo.toString()); // 가져온 정보를 텍스트 영역에 표시
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 타이머 시작
    private void startTimer() {
        Timer timer = new Timer(1000, e -> {
            if (timeRemaining > 0) {
                timerLabel.setText("Remaining Time: " + timeRemaining + "sec");
                
                int totalTime = 30; // 전체 제한 시간
                // 타이머 패널 크기 조정
                int panelWidth = (int) ((timeRemaining / (double) totalTime) * 720); // 패널 너비 계산
                timerPanel.setBounds(20, 370, panelWidth, 35);
                
                if (timeRemaining == 10) {
                    hintLabel.setText("Hint: " + hint); // 힌트를 표시
                    hintLabel.setVisible(true); // 힌트를 보이도록 설정
                }
                
                timeRemaining--;
            } else {
                ((Timer) e.getSource()).stop(); // 타이머 중지
                JOptionPane.showMessageDialog(frame, "Time End.");
                
                // 다음 라운드로 이동
                moveToNextRound();
            }
        });
        timer.start();
    }
    
    private void moveToNextRound() {
        selectRandomUserAndDisplay(); // 새로운 출제자 선택
        fetchRandomKeywordAndDisplay(); // 새로운 키워드 선택
        hintLabel.setVisible(false); // 힌트를 숨김
        timeRemaining = 30; // 타이머 초기화
        startTimer(); // 타이머 재시작
    }
    
}
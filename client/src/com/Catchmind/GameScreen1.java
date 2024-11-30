package com.Catchmind;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.*;
import java.util.Map;

public class GameScreen1 {

    private static JFrame frame;
    private static JTextArea playerInfoArea; // 플레이어 정보를 표시할 텍스트 영역
    private static JLabel keywordLabel; // 제시어를 표시할 라벨
    private static JLabel hintLabel; // 힌트를 표시할 라벨
    private static String hint; // 힌트를 저장할 변수
    private static JLabel selectedUserLabel; // 출제자를 표시할 라벨
    private static String selectedUser; // 출제자를 저장할 변수
    private static JLabel timerLabel; // 타이머를 표시할 라벨
    private static JPanel timerPanel; // 타이머 패널
    private static int timeRemaining = 30; // 제한 시간 (초)
    private static Color currentColor = Color.BLACK; // 현재 선택된 색상
    private static List<Line> lines = new ArrayList<>(); // 그림 데이터를 저장
    private static DrawingPanel drawingPanel; // 림판
    private static JPanel toolPanel;
    private static JTextPane chatArea; // JTextPane으로 변경
    private static JLabel roundLabel;
    private static String currentDrawer; // 현재 출제자의 이름
    private static String myName; // 현재 클라이언트의 이름

    // 타이머 상태 표시를 위한 상수 추가
    private static final String TIMER_NOT_STARTED = "Game not yet started";
    private static final String TIMER_ENDED = "Game ended";

    // 타이머 색상 상수 추가
    private static final Color TIMER_NORMAL_COLOR = new Color(200, 220, 255);
    private static final Color TIMER_WARNING_COLOR = Color.RED;
    private static final Color TIMER_NOT_STARTED_COLOR = Color.GRAY;
    private static final Color TIMER_ENDED_COLOR = new Color(34, 139, 34); // Forest Green

    // gameStarted 플래그 추가 및 관리
    private static boolean gameStarted = false;

    public GameScreen1(String username) {
        myName = username;
        initialize(username);
        // 초기 상태로 Game not yet started 표시
        showGameNotStarted();
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

        // 게임 종료 버튼(현재 창 닫기)
        endButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Leave the game?",
                    "Game end",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SocketManager.getInstance().closeConnection(); // 서버와 연결 종료
                frame.dispose(); // 현재 창 닫기
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
        toolPanel = new JPanel();
        toolPanel.setBounds(232, 20, 288, 30);
        toolPanel.setBackground(new Color(255, 255, 255));
        frame.getContentPane().add(toolPanel);
        toolPanel.setVisible(false);
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
            drawingPanel.clearDrawing(); // 현재 클라이언트 화면 초기화
            SocketManager.getInstance().sendReset(); // reset 신호 전송
        });

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
        roundLabel = new JLabel("  Round");
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

        // 채팅 메시지를 표시할 텍스트 영역
        chatArea = new JTextPane(); // JTextPane으로 생성
        chatArea.setEditable(false);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        ((StyledDocument) chatArea.getDocument()).setParagraphAttributes(0, 0, new SimpleAttributeSet(), true);

        // 스크롤 패널에 텍스트 영역 추가
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBounds(0, 0, 200, 100);
        chatPanel.add(chatScrollPane);

        // 채팅 입력 필드
        JTextField chatField = new JTextField();
        chatField.setBounds(0, 100, 140, 26);
        chatField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        chatPanel.add(chatField);

        // 전송 버튼
        JButton sendButton = new JButton("Send");
        sendButton.setBounds(140, 100, 60, 26);
        sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        chatPanel.add(sendButton);

        // 채팅 전송 기능
        ActionListener sendChatAction = e -> {
            String message = chatField.getText().trim();
            if (!message.isEmpty()) {
                SocketManager.getInstance().sendChat(message);
                chatField.setText("");
            }
        };

        // 전송 버튼 클릭 이벤트
        sendButton.addActionListener(sendChatAction);

        // Enter 키 입력 이벤트
        chatField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendChatAction.actionPerformed(null);
                }
            }
        });

        // 하단: 타이머 및 답변 입력 영역
        timerPanel = new JPanel();
        timerPanel.setBounds(20, 370, 720, 35);
        timerPanel.setBackground(TIMER_NORMAL_COLOR);
        frame.getContentPane().add(timerPanel);

        timerLabel = new JLabel("Remaining Time: 30sec"); // 타이머 라벨 초기화
        timerLabel.setForeground(Color.black);
        timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerPanel.setLayout(new BorderLayout());
        timerPanel.add(timerLabel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static void showTool() {
        toolPanel.setVisible(true);
    }

    public static void setKeyword(String keyword) {
        SwingUtilities.invokeLater(() -> {
            if (keywordLabel != null) {
                keywordLabel.setText(" Keyword: " + keyword);
                showTool();
            }
        });
    }

    // 타이머 업데이트 메서드
    public static void updateTimer(int seconds) {
        SwingUtilities.invokeLater(() -> {
            // 게임 시작 전 카운트다운 (3,2,1)
            if (!gameStarted && seconds <= 3) {
                timerLabel.setText("Game starts in " + seconds + "...");
                timerPanel.setBackground(TIMER_NOT_STARTED_COLOR);
                timerLabel.setForeground(Color.WHITE);
                timerPanel.setBounds(20, 370, 720, 35);
                return;
            }

            // 게임 중 타이머 표시
            if (seconds <= 3) {
                timerLabel.setText(String.valueOf(seconds));
            } else if (seconds <= 7) {
                timerLabel.setText(seconds + " sec");
            } else {
                timerLabel.setText("Remaining Time: " + seconds + " sec");
            }

            int totalTime = 30;
            int panelWidth = (int) ((seconds / (double) totalTime) * 720);
            timerPanel.setBounds(20, 370, panelWidth, 35);

            if (seconds == 10) {
                timerPanel.setBackground(TIMER_WARNING_COLOR);
                timerLabel.setForeground(Color.WHITE);
            }
        });
    }

    public static void setHint(String newHint) {
        SwingUtilities.invokeLater(() -> {
            // 힌트 라벨 업데이트
            if (hintLabel != null) {
                hintLabel.setText(newHint);
                hintLabel.setVisible(true); // 힌트를 보이게 설정
            }
        });
    }

    public static void moveToNextRound() {
        // 힌트 초기화
        hintLabel.setVisible(false); // 힌트 숨기기
        hintLabel.setText("");
        
        // 힌트 기본 라벨 초기화
        JLabel hintLabel_basic = (JLabel) hintLabel.getParent().getComponent(0);
        hintLabel_basic.setVisible(true);

        // 그림 초기화
        drawingPanel.clearDrawing();

        // 타이머 색상 초기화
        timerPanel.setBackground(TIMER_NORMAL_COLOR);
        timerLabel.setForeground(Color.BLACK);

        // 타이머 텍스트 초기화
        timerLabel.setText("Remaining Time: 30 sec");
        timerPanel.setBounds(20, 370, 720, 35); // 타이머 길이도 초기화
    }

    // 게임 시작 전 상태 표시
    public static void showGameNotStarted() {
        SwingUtilities.invokeLater(() -> {
            timerLabel.setText(TIMER_NOT_STARTED);
            timerPanel.setBackground(TIMER_NOT_STARTED_COLOR);
            timerLabel.setForeground(Color.WHITE);
            timerPanel.setBounds(20, 370, 720, 35);
        });
    }

    // 게임 종료 상태 표시
    public static void showGameEnded() {
        SwingUtilities.invokeLater(() -> {
            timerLabel.setText(TIMER_ENDED);
            timerPanel.setBackground(TIMER_ENDED_COLOR);
            timerLabel.setForeground(Color.WHITE);
            timerPanel.setBounds(20, 370, 720, 35);
        });
    }

    // 채팅 메시지 추가 메서드를 클래스 레벨로 이동
    public static void addChatMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (chatArea != null) {
                // 일반 채팅 메시지용 스타일 (검정색)
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, Color.BLACK);

                // Document 가져오기
                StyledDocument doc = (StyledDocument) chatArea.getDocument();

                try {
                    // 검정색 텍스트 추가
                    doc.insertString(doc.getLength(), message + "\n", attrs);
                    // 자동 스크롤
                    chatArea.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 시스템 메시지 추가 메서드 (빨간색으로 표시)
    public static void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (chatArea != null) {
                // 현재 스타일 저장
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, Color.RED);

                // Document 가져오기
                StyledDocument doc = (StyledDocument) chatArea.getDocument();

                try {
                    // 빨간색 텍스트 추가
                    doc.insertString(doc.getLength(), message + "\n", attrs);
                    // 자동 스크롤
                    chatArea.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 라운드 정보 업데이트
    public static void updateRound(int round) {
        SwingUtilities.invokeLater(() -> {
            roundLabel.setText("  Round " + round);
        });
    }

    // 출제자 정보 업데이트
    public static void updateDrawer(String drawerName) {
        SwingUtilities.invokeLater(() -> {
            currentDrawer = drawerName;
            selectedUserLabel.setText("  Painter: " + drawerName);

            // 그리기 도구는 출제자만 볼 수 있음
            toolPanel.setVisible(drawerName.equals(myName));

            // 키워드는 출제자만 볼 수 있음
            if (!drawerName.equals(myName)) {
                keywordLabel.setText(" Keyword: ???");
            }
        });
    }

    // 게임 시작 시 타이머 초기화
    public static void startGameTimer() {
        gameStarted = true;
        SwingUtilities.invokeLater(() -> {
            timerPanel.setBackground(TIMER_NORMAL_COLOR);
            timerLabel.setForeground(Color.BLACK);
            updateTimer(30);
        });
    }

    // 리더보드 업데이트 메서드 추가
    public static void updateLeaderboard(Map<String, Integer> scores) {
        SwingUtilities.invokeLater(() -> {
            if (playerInfoArea != null) {
                StringBuilder leaderboard = new StringBuilder();
                
                // 점수를 기준으로 정렬하기 위해 리스트로 변환
                List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
                sortedScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                
                for (Map.Entry<String, Integer> entry : sortedScores) {
                    String playerName = entry.getKey();
                    int score = entry.getValue();
                    
                    // 현재 플레이어는 강조 표시
                    if (playerName.equals(myName)) {
                        leaderboard.append("▶ ").append(playerName).append(": ").append(score).append("\n");
                    } else {
                        leaderboard.append("   ").append(playerName).append(": ").append(score).append("\n");
                    }
                }
                
                playerInfoArea.setText(leaderboard.toString());
            }
        });
    }
}
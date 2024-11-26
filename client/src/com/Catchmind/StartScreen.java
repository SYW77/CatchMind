package com.Catchmind;

import java.awt.*;
import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StartScreen {

    private JFrame frame;

    // MySQL 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CatchmindDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ekdud0412?";


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    StartScreen window = new StartScreen();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    StartScreen() {
        initialize();
    }


    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 771, 512);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.getContentPane().setLayout(null);

        // 게임 이름 라벨
        JLabel gamename = new JLabel("CatchMind");
        gamename.setBounds(250, 100, 300, 50);
        gamename.setHorizontalAlignment(SwingConstants.CENTER);
        gamename.setFont(new Font("맑은 고딕", Font.BOLD, 36));
        frame.getContentPane().add(gamename);
        
        
        // 사용자 이름 입력 필드
        JTextField username = new JTextField();
        username.setBounds(250, 200, 300, 40);
        username.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        username.setHorizontalAlignment(SwingConstants.CENTER);

        // 기본 텍스트 및 색상 설정
        username.setText("Enter your name");
        username.setForeground(Color.GRAY);

        // KeyListener로 사용자 입력 처리
        username.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                // 기본 텍스트가 표시된 상태에서 첫 입력 시 기본 텍스트 제거
                if (username.getText().equals("Enter your name")) {
                    username.setText(""); // 기본 텍스트 제거
                    username.setForeground(Color.BLACK); // 입력 텍스트 색상을 검정으로 변경
                }
            }
        });

        // FocusListener로 기본 텍스트 복원 처리
        username.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // 포커스를 얻으면 아무 작업도 하지 않음 (KeyListener가 처리함)
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 텍스트 필드가 비어 있으면 기본 텍스트 복원
                if (username.getText().trim().isEmpty()) {
                    username.setText("Enter your name");
                    username.setForeground(Color.GRAY); // 기본 텍스트 색상 복원
                }
            }
        });

        // 텍스트 필드 추가
        frame.getContentPane().add(username);
		
        
        
        // 포트 번호 입력 필드
        JTextField portNum = new JTextField();
        portNum.setBounds(250, 250, 300, 40);
        portNum.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        portNum.setHorizontalAlignment(SwingConstants.CENTER);

        // 기본 텍스트 및 색상 설정
        portNum.setText("Enter the port number");
        portNum.setForeground(Color.GRAY);

        // KeyListener로 사용자 입력 처리
        portNum.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                // 기본 텍스트가 표시된 상태에서 첫 입력 시 기본 텍스트 제거
                if (portNum.getText().equals("Enter the port number")) {
                	portNum.setText(""); // 기본 텍스트 제거
                	portNum.setForeground(Color.BLACK); // 입력 텍스트 색상을 검정으로 변경
                }
            }
        });

        // FocusListener로 기본 텍스트 복원 처리
        portNum.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // 포커스를 얻으면 아무 작업도 하지 않음 (KeyListener가 처리함)
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 텍스트 필드가 비어 있으면 기본 텍스트 복원
                if (portNum.getText().trim().isEmpty()) {
                	portNum.setText("Enter the port number");
                	portNum.setForeground(Color.GRAY); // 기본 텍스트 색상 복원
                }
            }
        });

        // 텍스트 필드 추가
        frame.getContentPane().add(portNum);
		
        
        
        // 시작 버튼
        JButton loginBtn = new JButton("Start");
        loginBtn.setBounds(350, 310, 100, 40);
        loginBtn.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        frame.getContentPane().add(loginBtn);
        
        
        // 버튼 클릭 이벤트
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userNameInput = username.getText();
                String serverInput = "localhost"; //serverAddress.getText();
                String portInput = portNum.getText();

                if (!userNameInput.isEmpty() && !userNameInput.equals("Enter your name") &&
                        !serverInput.isEmpty() && !serverInput.equals("Enter the server address") &&
                        !portInput.isEmpty() && !portInput.equals("Enter the port number")) {

                    // 소켓 연결을 위한 인스턴스 생성
                    SocketManager socketManager = SocketManager.getInstance(serverInput, Integer.parseInt(portInput));

                    // 서버로 닉네임 전송
                    socketManager.sendNickname(userNameInput);

                    // 다음 화면으로 전환
                    frame.dispose(); // 현재 화면 닫기
                    EventQueue.invokeLater(() -> {
                        try {
                            GameScreen1 gameScreen = new GameScreen1(userNameInput);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter all fields.");
                }
            }
        });
    }

    // 데이터베이스에 유저 이름 저장 및 점수 초기화
    private void saveUsernameToDatabase(String username) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // SQL INSERT 쿼리 (username과 score 삽입)
            String sql = "INSERT INTO Users (username, score) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setInt(2, 0); // 초기 점수는 0으로 설정

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Start the game?\n(Username and score saved successfully.)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

package com.Catchmind;

import java.awt.*;
import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class StartScreen {

    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                StartScreen window = new StartScreen();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public StartScreen() { // 생성자에 public 추가
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
        username.setBounds(250, 170, 300, 40);
        username.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        username.setHorizontalAlignment(SwingConstants.CENTER);
        username.setText("Enter your name");
        username.setForeground(Color.GRAY);

        username.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (username.getText().equals("Enter your name")) {
                    username.setText("");
                    username.setForeground(Color.BLACK);
                }
            }
        });

        username.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // KeyListener가 처리하므로 아무 작업도 하지 않음
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (username.getText().trim().isEmpty()) {
                    username.setText("Enter your name");
                    username.setForeground(Color.GRAY);
                }
            }
        });

        frame.getContentPane().add(username);

        // 호스트 이름 입력 필드
        JTextField hostName = new JTextField();
        hostName.setBounds(250, 220, 300, 40);
        hostName.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        hostName.setHorizontalAlignment(SwingConstants.CENTER);
        hostName.setText("localhost");
        hostName.setForeground(Color.BLACK);
        frame.getContentPane().add(hostName);

        // 포트 번호 입력 필드
        JTextField portNum = new JTextField();
        portNum.setBounds(250, 270, 300, 40);
        portNum.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        portNum.setHorizontalAlignment(SwingConstants.CENTER);
        portNum.setText("3000");
        portNum.setForeground(Color.BLACK);
        frame.getContentPane().add(portNum);

        // 호스트와 포트 번호 필드의 포커스 리스너
        hostName.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (hostName.getText().equals("localhost")) {
                    hostName.selectAll();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (hostName.getText().trim().isEmpty()) {
                    hostName.setText("localhost");
                }
            }
        });

        portNum.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (portNum.getText().equals("3000")) {
                    portNum.selectAll();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (portNum.getText().trim().isEmpty()) {
                    portNum.setText("3000");
                }
            }
        });

        // 시작 버튼
        JButton loginBtn = new JButton("Start");
        loginBtn.setBounds(351, 330, 100, 40);
        loginBtn.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        frame.getContentPane().add(loginBtn);

        // 버튼 클릭 이벤트
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userNameInput = username.getText();
                String serverInput = hostName.getText();
                String portInput = portNum.getText();

                if (!userNameInput.isEmpty() && !userNameInput.equals("Enter your name") &&
                        !serverInput.isEmpty() && !portInput.isEmpty()) {
                    try {
                        // 소켓 연결을 위한 인스턴스 생성
                        SocketManager socketManager = SocketManager.getInstance(serverInput, Integer.parseInt(portInput));

                        // 서버로 닉네임 전송
                        socketManager.sendNickname(userNameInput);

                        // 다음 화면으로 전환
                        frame.dispose(); // 현재 화면 닫기
                        EventQueue.invokeLater(() -> {
                            try {
                                new GameScreen1(userNameInput); // 게임 화면 호출
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid port number.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Connection failed. Please try again.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
                }
            }
        });
    }
}

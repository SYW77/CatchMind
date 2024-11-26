package com.Catchmind;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.io.*;
import java.util.Base64;

public class DrawingPanel extends JPanel {
    private List<Line> lines;
    private Color currentColor;
    private Point lastPoint;
    private SocketManager socketManager;

    private static DrawingPanel instance;

    public static DrawingPanel getInstance() {
        return instance;
    }

    public static DrawingPanel getInstance(List<Line> lines, Color initialColor) {
        if (instance == null) {
            instance = new DrawingPanel(lines, initialColor);
        }
        return instance;
    }

    public DrawingPanel(List<Line> lines, Color initialColor) {
        this.lines = lines;
        this.currentColor = initialColor;
        this.socketManager = SocketManager.getInstance();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint(); // 마우스 클릭 위치 저장
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getPoint();
                if (lastPoint != null) {
                    lines.add(new Line(lastPoint, currentPoint, currentColor)); // 선 추가
                    repaint(); // 화면 갱신
                    lastPoint = currentPoint; // 마지막 위치 업데이트

                    // 서버에 그린 그림을 전송
                    sendDrawingToServer();
                }
            }
        });
    }

    public void setColor(Color color) {
        this.currentColor = color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Line line : lines) {
            g.setColor(line.color);
            g.drawLine(line.start.x, line.start.y, line.end.x, line.end.y);
        }
    }

    // 서버에 그려진 그림을 Base64로 인코딩하여 전송
    private void sendDrawingToServer() {
        try {
            // 임시 이미지를 그리기 위한 BufferedImage 생성
            int width = getWidth();
            int height = getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            paint(g2d); // 현재 그려진 상태를 이미지로 그리기
            g2d.dispose();

            // 이미지를 base64로 인코딩
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // 서버에 그림 데이터를 전송
            socketManager.sendDrawing(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 서버로부터 그림을 받았을 때 호출되는 메서드
    public void setDrawingFromServer(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(byteArrayInputStream);

            Graphics g = getGraphics();
            g.drawImage(image, 0, 0, null); // 서버에서 받은 이미지를 화면에 그리기
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

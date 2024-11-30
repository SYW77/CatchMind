package com.Catchmind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.io.*;
import java.util.Base64;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

public class DrawingPanel extends JPanel {
    private List<Line> lines;
    private Color currentColor;
    private Point lastPoint;
    private SocketManager socketManager;

    private static DrawingPanel instance;

    private static final int BATCH_SIZE = 5;
    private List<Line> pendingLines = new ArrayList<>();

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
            private List<Line> currentStrokeLines = new ArrayList<>();

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                currentStrokeLines.clear(); // 새로운 스트로크 시작
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentStrokeLines.size() > 0) {
                    // 마우스를 떼면 현재까지의 선들을 압축해서 전송
                    sendCompressedLines(currentStrokeLines);
                    currentStrokeLines.clear();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getPoint();
                if (lastPoint != null) {
                    Line newLine = new Line(lastPoint, currentPoint, currentColor);
                    lines.add(newLine);
                    pendingLines.add(newLine);
                    repaint();
                    lastPoint = currentPoint;
                    
                    // 일정 개수의 선이 모이면 압축 전송
                    if (pendingLines.size() >= BATCH_SIZE) {
                        sendCompressedLines(new ArrayList<>(pendingLines));
                        pendingLines.clear();
                    }
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

    // Line 클래스에 직렬화 기능 추가
    public void sendLineToServer(Line line) {
        try {
            // 선 데이터를 문자열로 변환
            String lineData = line.start.x + "," + line.start.y + "," + 
                             line.end.x + "," + line.end.y + "," + 
                             line.color.getRGB();
            socketManager.sendLine("LINE:" + lineData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 서버로부터 선 데이터 수신 처리
    public void drawLineFromServer(String lineData) {
        String[] parts = lineData.split(",");
        Point start = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        Point end = new Point(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        Color color = new Color(Integer.parseInt(parts[4]));
        
        lines.add(new Line(start, end, color));
        repaint();
    }

    public void sendCompressedLines(List<Line> lines) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            
            objectOut.writeObject(lines);
            objectOut.close();
            
            byte[] compressedData = baos.toByteArray();
            String base64Compressed = Base64.getEncoder().encodeToString(compressedData);
            
            socketManager.sendLine("COMPRESSED:" + base64Compressed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 그림판 초기화 메서드
    public void clearDrawing() {
        lines.clear();
        pendingLines.clear();
        repaint();
    }
}

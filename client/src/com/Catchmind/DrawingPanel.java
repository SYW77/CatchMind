package com.Catchmind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

// DrawingPanel 클래스: 그림판 구현
public class DrawingPanel extends JPanel {
    private List<Line> lines;
    private Color currentColor;
    private Point lastPoint;

    public DrawingPanel(List<Line> lines, Color initialColor) {
        this.lines = lines;
        this.currentColor = initialColor;

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
}

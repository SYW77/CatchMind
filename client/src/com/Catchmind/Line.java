package com.Catchmind;

import java.awt.*;

// Line 클래스: 선 데이터 저장
public class Line {
    Point start, end;
    Color color;

    public Line(Point start, Point end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }
}
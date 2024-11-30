package com.Catchmind;

import java.awt.*;
import java.io.Serializable;

// Line 클래스: 선 데이터 저장
public class Line implements Serializable {
    private static final long serialVersionUID = 1L;
    
    Point start, end;
    Color color;

    public Line(Point start, Point end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }
}
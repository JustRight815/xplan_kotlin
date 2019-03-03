package com.zh.xplan.ui.view.weather;

import java.util.Random;

/**
 * Created by ghbha on 2016/5/16.
 */
public class Star {

    public Star(int maxX, int maxY) {
        this.x = random.nextInt(maxX);
        this.y = random.nextInt(maxY);
        this.radius = 2 + random.nextInt(2);
        currentAlpha = minAlpha + random.nextInt(110);
    }


    public void shine() {
        if (outOfBounds())
            alphaDelta = -alphaDelta;
        currentAlpha = currentAlpha + alphaDelta;
    }

    public boolean outOfBounds() {
        return currentAlpha >= maxAlpha || currentAlpha < minAlpha;
    }

    public int getCurrentAlpha() {
        return currentAlpha;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRadius() {
        return radius;
    }

    /////////////////////////////////////////////////////////////////
    protected int x; //x最大范围
    protected int y; //y最大范围
    protected Random random = new Random();
    private int radius = 4;
    private int minAlpha = 30;
    private int maxAlpha = 140;
    private int currentAlpha = minAlpha;
    private int alphaDelta = 2;
}

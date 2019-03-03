package com.zh.xplan.ui.view.weather;

import java.util.Random;

/**
 * Created by ghbha on 2016/5/16.
 */
public abstract class BaseLine {

    public BaseLine(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
        alpha = alpha + random.nextInt(115);
        initRandom();
    }

    protected abstract void resetRandom();

    protected abstract void initRandom();

    protected abstract void rain();

    protected abstract boolean outOfBounds();

    public int getStartX() {
        return startX;
    }

    public BaseLine setStartX(int startX) {
        this.startX = startX;
        return this;
    }

    public int getStartY() {
        return startY;
    }

    public BaseLine setStartY(int startY) {
        this.startY = startY;
        return this;
    }

    public int getStopX() {
        return stopX;
    }

    public BaseLine setStopX(int stopX) {
        this.stopX = stopX;
        return this;
    }

    public int getStopY() {
        return stopY;
    }

    public BaseLine setStopY(int stopY) {
        this.stopY = stopY;
        return this;
    }

    public int getAlpha() {
        return alpha;
    }

    ////////////////////////////////////////////////////
    protected Random random = new Random();
    protected int alpha = 100;
    protected int startX;
    protected int startY;
    protected int stopX;
    protected int stopY;
    protected int deltaX = 6;
    protected int deltaY = 10;
    protected int maxX; //x最大范围
    protected int maxY; //y最大范围
}

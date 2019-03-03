package com.zh.xplan.ui.view.weather;

/**
 * Created by ghbha on 2016/5/16.
 */
public class RainOrSnowLine extends BaseLine {

    public RainOrSnowLine(int maxX, int maxY) {
        super(maxX, maxY);
    }

    @Override
    public void initRandom() {
        deltaY = 20;
        stopX = startX = random.nextInt(maxX);
        startY = random.nextInt(maxY);
        stopY = startY + 20;
    }

    @Override
    public void resetRandom() {
        startY = startY - maxY;
        stopY = startY + 20;
    }

    @Override
    public void rain() {
        if (outOfBounds())
            resetRandom();
        startY += deltaY;
        stopY += deltaY;
    }

    @Override
    protected boolean outOfBounds() {
        return getStartY() >= maxY;
    }
}

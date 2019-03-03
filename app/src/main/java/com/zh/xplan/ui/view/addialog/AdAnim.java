package com.zh.xplan.ui.view.addialog;

import android.view.View;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

/**
 * 广告弹性动画
 */

public class AdAnim {
    public static AdAnim adAnim;
    public static SpringSystem springSystem;
    public SpringConfig springConfig = SpringConfig.fromBouncinessAndSpeed(8, 2);


    private double tension=-1;//张力系数
    private double friction=-1;//阻尼系数
    private View animView;//需要操作的视图

    public AdAnim(View animView) {
        this.animView = animView;
    }

    public static AdAnim getInstance(View view){
        adAnim =new AdAnim(view);
        if (springSystem==null){
            springSystem=SpringSystem.create();
        }
        return adAnim;
    }

    //属性设置
    /**
     * @param tension 张力系数
     * @return
     */
    public AdAnim setTension(double tension) {
        this.tension = tension;
        if (friction!=-1){
            springConfig=SpringConfig.fromBouncinessAndSpeed(tension,friction);
        }else{
            springConfig=SpringConfig.fromBouncinessAndSpeed(tension,2);
        }
        return this;
    }

    /**
     * @param friction  阻尼系数
     * @return
     */
    public AdAnim setFriction(double friction) {
        this.friction = friction;
        if (tension!=-1){
            springConfig=SpringConfig.fromBouncinessAndSpeed(tension,friction);
        }else{
            springConfig=SpringConfig.fromBouncinessAndSpeed(8,friction);
        }
        return this;
    }

    /**
     * @param startX  开始X坐标(像素为单位)
     * @param startY  开始Y坐标
     * @param endX    结束X坐标
     * @param endY    结束Y坐标
     * 设置平移动画
     */
    public AdAnim startTranslationAnim(final double startX, final double startY, double endX, double endY){
        //获取Spring对象
        Spring transSpringX=springSystem.createSpring();
        Spring transSpringY=springSystem.createSpring();

        //设置Spring动画配置
        transSpringX.setSpringConfig(springConfig);
        transSpringY.setSpringConfig(springConfig);

        //设置动画开始的值
        transSpringX.setCurrentValue(startX);
        transSpringY.setCurrentValue(startY);

        //设置动画结束值
        transSpringX.setEndValue(endX);
        transSpringY.setEndValue(endY);

        //设置动画监听
        transSpringX.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                 animView.setTranslationX((float)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });
        transSpringY.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                animView.setTranslationY((float)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });
        return this;
    }

    /**
     * @param startValue  开始角度
     * @param endValue    结束角度
     * 视图绕Z轴，以自己中心点为锚点旋转
     */
    public AdAnim startRotateAnim(float startValue, float endValue){
        Spring rotateSpring=springSystem.createSpring();
        rotateSpring.setSpringConfig(springConfig);
        rotateSpring.setCurrentValue(startValue);
        rotateSpring.setEndValue(endValue);
        rotateSpring.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                animView.setRotation((float)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });
        return this;
    }


    /**
     * @param startX  开始X轴缩放系数（1为原本大小，小于1缩小，大于1放大）
     * @param startY  开始Y轴缩放系数
     * @param endX    结束X轴缩放系数
     * @param endY    结束Y轴缩放系数
     * 视图缩放
     */
    public AdAnim startScaleAnim(final double startX, final double startY, double endX, double endY){
        //获取Spring对象
        Spring transSpringX=springSystem.createSpring();
        Spring transSpringY=springSystem.createSpring();

        //设置Spring动画配置
        transSpringX.setSpringConfig(springConfig);
        transSpringY.setSpringConfig(springConfig);

        //设置动画开始的值
        transSpringX.setCurrentValue(startX);
        transSpringY.setCurrentValue(startY);

        //设置动画结束值
        transSpringX.setEndValue(endX);
        transSpringY.setEndValue(endY);

        //设置动画监听
        transSpringX.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                animView.setScaleX((float)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });
        transSpringY.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                animView.setScaleY((float)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });
        return this;
    }

}

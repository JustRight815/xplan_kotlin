package com.zh.xplan.ui.logisticsdetail.bean;

public class LogisticsInfoBean {
    private String acceptTime;
    private String acceptStation;

    public LogisticsInfoBean() {
    }

    public LogisticsInfoBean(String acceptTime, String acceptStation) {
        this.acceptTime = acceptTime;
        this.acceptStation = acceptStation;
    }

    public String getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(String acceptTime) {
        this.acceptTime = acceptTime;
    }

    public String getAcceptStation() {
        return acceptStation;
    }

    public void setAcceptStation(String acceptStation) {
        this.acceptStation = acceptStation;
    }
}

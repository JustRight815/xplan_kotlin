
package com.zh.xplan.ui.camera;
import java.io.Serializable;

/**
 * 录制视频设置
 */
public class RecordVideoSet implements Serializable {
    private int limitRecordTime;//限制时间
    private int limitRecordSize;//限制大小
    private boolean isSmallVideo = false;//是否录制小视频

    public RecordVideoSet() {
    }

    public int getLimitRecordSize() {
        return limitRecordSize;
    }

    public void setLimitRecordSize(int limitRecordSize) {
        this.limitRecordSize = limitRecordSize;
    }

    public int getLimitRecordTime() {
        return limitRecordTime;
    }

    public boolean isSmallVideo() {
        return isSmallVideo;
    }

    public void setSmallVideo(boolean smallVideo) {
        isSmallVideo = smallVideo;
    }

    public void setLimitRecordTime(int limitRecordTime) {
        this.limitRecordTime = limitRecordTime;
    }

}

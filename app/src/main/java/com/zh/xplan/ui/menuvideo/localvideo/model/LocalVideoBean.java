package com.zh.xplan.ui.menuvideo.localvideo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 本地视频的数据模型
 */
public class LocalVideoBean implements Parcelable{
    public String title;//视频名称
    public String duration;//视频时长
    public long size;//视频大小
    public String path;//视频的路径

    public LocalVideoBean(String title, String duration, long size, String path) {
        this.title = title;
        this.duration = duration;
        this.size = size;
        this.path = path;
    }
    
    

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static Creator<LocalVideoBean> getCreator() {
		return CREATOR;
	}



	public static final Creator<LocalVideoBean> CREATOR  =  new Creator<LocalVideoBean>() {
        @Override
        public LocalVideoBean createFromParcel(Parcel source) {
            LocalVideoBean video = new LocalVideoBean(source.readString(),source.readString(),source.readLong(),source.readString());
            return video;
        }

        @Override
        public LocalVideoBean[] newArray(int size) {
            return new LocalVideoBean[size];
        }
    };
    
    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(duration);
        dest.writeLong(size);
        dest.writeString(path);
    }
}

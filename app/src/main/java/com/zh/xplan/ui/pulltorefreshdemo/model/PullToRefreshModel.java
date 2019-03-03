package com.zh.xplan.ui.pulltorefreshdemo.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * 下拉刷新，加载更多 demo 数据模型
 */
public class PullToRefreshModel implements Parcelable, MultiItemEntity {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
    }

    public PullToRefreshModel() {
    }

    protected PullToRefreshModel(Parcel in) {
        this.type = in.readString();
    }

    public static final Creator<PullToRefreshModel> CREATOR = new Creator<PullToRefreshModel>() {
        @Override
        public PullToRefreshModel createFromParcel(Parcel source) {
            return new PullToRefreshModel(source);
        }

        @Override
        public PullToRefreshModel[] newArray(int size) {
            return new PullToRefreshModel[size];
        }
    };

    @Override
    public String toString() {
        return "PullToRefreshModel{" +
                "type='" + type + '\'' +
                '}';
    }

    @Override
    public int getItemType() {
        return 0;
    }
}

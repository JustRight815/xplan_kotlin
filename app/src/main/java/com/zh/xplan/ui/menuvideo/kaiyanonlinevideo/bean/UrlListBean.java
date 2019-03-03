package com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UrlListBean implements Parcelable {

    /**
     * name : ucloud
     * url : https://baobab.kaiyanapp.com/api/v1/playUrl?vid=14664&editionType=normal&source=ucloud
     */

    private String name;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.url);
    }

    public UrlListBean() {
    }

    protected UrlListBean(Parcel in) {
        this.name = in.readString();
        this.url = in.readString();
    }

    public static final Creator<UrlListBean> CREATOR = new Creator<UrlListBean>() {
        @Override
        public UrlListBean createFromParcel(Parcel source) {
            return new UrlListBean(source);
        }

        @Override
        public UrlListBean[] newArray(int size) {
            return new UrlListBean[size];
        }
    };
}

package com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean;

import android.os.Parcel;
import android.os.Parcelable;
import com.chad.library.adapter.base.entity.MultiItemEntity;
public class ItemListBean implements Parcelable, MultiItemEntity {
    private String type;
    private DataBean data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeParcelable(this.data, flags);
    }

    public ItemListBean() {
    }

    protected ItemListBean(Parcel in) {
        this.type = in.readString();
        this.data = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Creator<ItemListBean> CREATOR = new Creator<ItemListBean>() {
        @Override
        public ItemListBean createFromParcel(Parcel source) {
            return new ItemListBean(source);
        }

        @Override
        public ItemListBean[] newArray(int size) {
            return new ItemListBean[size];
        }
    };

    @Override
    public String toString() {
        return "ItemListBean{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }

    @Override
    public int getItemType() {
        if("video".equals(type)){
            return 0;
        }else if("textHeader".equals(type)){
            return 1;
        }
        return 2;
    }
}

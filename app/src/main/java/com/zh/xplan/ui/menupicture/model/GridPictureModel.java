package com.zh.xplan.ui.menupicture.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.io.Serializable;

/**
 * 瀑布流所用缩略图数据模型
 */
public class GridPictureModel implements MultiItemEntity, Serializable{
	String pictureTitle;
	String pictureUrl;
	int pictureHeight;
	int pictureWidth;

	public int getPictureWidth() {
		return pictureWidth;
	}

	public void setPictureWidth(int pictureWidth) {
		this.pictureWidth = pictureWidth;
	}

	public int getPictureHeight() {
		return pictureHeight;
	}

	public void setPictureHeight(int pictureHeight) {
		this.pictureHeight = pictureHeight;
	}

	public String getPictureTitle() {
		return pictureTitle;
	}

	public void setPictureTitle(String pictureTitle) {
		this.pictureTitle = pictureTitle;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	@Override
	public int getItemType() {
		return 0;
	}

	@Override
	public String toString() {
		return "GridPictureModel{" +
				"pictureTitle='" + pictureTitle + '\'' +
				", pictureUrl='" + pictureUrl + '\'' +
				", pictureHeight=" + pictureHeight +
				", pictureWidth=" + pictureWidth +
				'}';
	}
}

package com.zh.xplan.ui.menuvideo.localvideo.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.module.common.image.ImageLoader;
import com.module.common.log.LogUtil;
import com.zh.xplan.R;
import com.zh.xplan.ui.menuvideo.localvideo.StringUtils;
import com.zh.xplan.ui.menuvideo.localvideo.model.LocalVideoBean;

import org.qcode.qskinloader.SkinManager;

import java.util.List;

/**
 * 本地视频列表的适配器
 * 
 */
public class LocalVideoListAdapter extends BaseAdapter {
	private List<LocalVideoBean> mVideoList;
	private Context mContext;
	public LocalVideoListAdapter(List<LocalVideoBean> videoList, Context context) {
		super();
		this.mVideoList = videoList;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mVideoList.size();
	}

	@Override
	public LocalVideoBean getItem(int position) {
		return mVideoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_local_video_item, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);			
		} else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		SkinManager.getInstance().applySkin(convertView, true);
		viewHolder.setData(mVideoList.get(position));
		LogUtil.e("zh","mVideoList path " + mVideoList.get(position).path);
		ImageLoader.displayImage(mContext,viewHolder.image,Uri.parse("file:///" + mVideoList.get(position).path),R.drawable.holder_img);
		return convertView;
	}
	
	private static class ViewHolder{
		private ImageView image;
		private TextView title;
		private TextView duration;

		public ViewHolder(View view){
			image = (ImageView) view.findViewById(R.id.image);
			title = (TextView) view.findViewById(R.id.title);
			duration = (TextView) view.findViewById(R.id.duration);
		}
		
		public void setData(LocalVideoBean video){
			title.setText("名称：" + video.title);
			duration.setText("时长：" + StringUtils.generateTime(Long.parseLong(video.duration)) + "   大小：" + StringUtils.generateFileSize(video.size));
//			image.setImageResource(R.drawable.holder_img);
		}
	}
}

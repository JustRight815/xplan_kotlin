package com.zh.xplan.ui.imagedetailactivity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.zh.xplan.R
import com.zh.xplan.ui.base.BaseFragment
import com.zh.xplan.ui.menupicture.model.GridPictureModel
import kotlinx.android.synthetic.main.fragment_image_detail.*
import java.io.Serializable

/**
 * 显示图片详情的fragment
 */
class ImageDetailFragment : BaseFragment() {
    private var mPictureModelList: List<GridPictureModel>? = null
    private var mPosition: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val mView = View.inflate(activity!!.baseContext,
                R.layout.fragment_image_detail, null)
        val bundle = arguments
        if (bundle != null) {
            this.mPictureModelList = bundle.getSerializable("pictureModelList") as List<GridPictureModel>
            this.mPosition = bundle.getInt("position")
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    /**
     * 初始化views
     */
    private fun initView() {
        // 6/8 6字体大小为默认的1.3倍
        val text = (mPosition + 1).toString() + "/" + mPictureModelList!!.size + " " + mPictureModelList!![mPosition].pictureTitle
        val start = text.indexOf("/")
        val end = text.length
        val textSpan = SpannableString(text)
        textSpan.setSpan(RelativeSizeSpan(1.3f), 0, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(RelativeSizeSpan(1f), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        introduction!!.text = textSpan

        ViewCompat.setTransitionName(PhotoView, mPictureModelList!![mPosition].pictureUrl)
        PhotoView!!.setOnTouchListener { v, event ->
            //				LogUtil.e("zh"," mPhotoView.getScale() " + mPhotoView.getScale());
            if (PhotoView!!.scale != 1.0f) {
                (activity as ImageDetailActivity).upDownHideLayout.lock()
            } else {
                (activity as ImageDetailActivity).upDownHideLayout.unLock()
            }
            PhotoView!!.attacher.onTouch(v, event)
        }
    }

    override fun onResume() {
        super.onResume()
        //		if(! isLoadSuccess){
        Glide.with(this).load(mPictureModelList!![mPosition].pictureUrl).into(object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                PhotoView!!.setImageDrawable(resource)
                pb_progressBar!!.visibility = View.GONE
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                pb_progressBar!!.visibility = View.GONE
            }
        })
    }

    companion object {
        fun newInstance(pictureModelList: List<GridPictureModel>, position: Int): ImageDetailFragment {
            val fragment = ImageDetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("pictureModelList", pictureModelList as Serializable)
            bundle.putInt("position", position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
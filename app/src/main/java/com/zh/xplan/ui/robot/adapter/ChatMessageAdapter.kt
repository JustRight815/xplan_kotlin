package com.zh.xplan.ui.robot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.zh.xplan.R
import com.zh.xplan.ui.robot.listener.ReSendMsgLinsener
import com.zh.xplan.ui.robot.model.ChatMessage
import java.text.SimpleDateFormat

/**
 * 智能客服聊天信息显示适配器
 * 2016-1-8
 */
class ChatMessageAdapter(private val context: Context, private val mMsgList: List<ChatMessage>) : BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var resendMsgLinsener: ReSendMsgLinsener? = null

    fun setResendMsgLinsener(resendMsgLinsener: ReSendMsgLinsener) {
        this.resendMsgLinsener = resendMsgLinsener
    }

    override fun getCount(): Int {
        return mMsgList.size
    }

    override fun getItem(position: Int): Any {
        return mMsgList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val chatMessage = mMsgList[position]
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            //根据ItemType设置不同的布局    0表示客服  1表示用户
            viewHolder = ViewHolder()
            if (getItemViewType(position) == 0) {
                convertView = mInflater.inflate(R.layout.robot_serivce_msg, parent, false)
            } else {
                convertView = mInflater.inflate(R.layout.robot_customer_msg, parent, false)
                viewHolder.mRetry = convertView.findViewById<View>(R.id.tv_retry) as TextView
                viewHolder.mProgress = convertView.findViewById<View>(R.id.pb_progress) as ProgressBar
            }
            viewHolder.mDate = convertView.findViewById<View>(R.id.tv_msg_date) as TextView
            viewHolder.mMsg = convertView.findViewById<View>(R.id.tv_msg_info) as TextView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        //显示数据
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        //		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        viewHolder.mDate.text = df.format(chatMessage.date)
        viewHolder.mMsg.text = chatMessage.msg
        if (getItemViewType(position) == 1) {

            if (chatMessage.state == 0) {
                //发送中
                viewHolder.mRetry.visibility = View.INVISIBLE
                viewHolder.mProgress.visibility = View.VISIBLE
            } else if (chatMessage.state == 1) {
                //发送成功
                viewHolder.mRetry.visibility = View.GONE
                viewHolder.mProgress.visibility = View.GONE
            } else if (chatMessage.state == -1) {
                //发送失败
                viewHolder.mProgress.visibility = View.GONE
                viewHolder.mRetry.visibility = View.VISIBLE
            }

            viewHolder.mRetry.setOnClickListener { resendMsgLinsener?.onResendMsg(position) }
        }
        return convertView
    }

    //两种不同的item
    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return if(mMsgList[position].type === ChatMessage.Type.SERVICE) {
            0
        }else{
            1
        }
    }

    private class ViewHolder {
        internal  lateinit var mDate: TextView
        internal  lateinit var mMsg: TextView
        internal  lateinit var mRetry: TextView
        internal  lateinit var mProgress: ProgressBar
    }
}

package com.zh.xplan.ui.robot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.module.common.log.LogUtil
import com.module.common.net.rx.NetManager
import com.module.common.view.snackbar.SnackbarUtils
import com.zh.xplan.R
import com.zh.xplan.ui.base.BaseActivity
import com.zh.xplan.ui.robot.adapter.ChatMessageAdapter
import com.zh.xplan.ui.robot.listener.ReSendMsgLinsener
import com.zh.xplan.ui.robot.model.ChatMessage
import com.zh.xplan.ui.robot.model.ChatResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_robot.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 * 小机器人界面
 * @author zh 2018-6-2
 */
class RobotKotlinActivity : BaseActivity() , View.OnClickListener {
    private var mContentView: View? = null
    private var mAdapter: ChatMessageAdapter? = null
    private var mMsgList: ArrayList<ChatMessage> = ArrayList()
    private val mGson = Gson()

    // 接入图灵平台所需url和key
    private val URL = " http://www.tuling123.com/openapi/api"
    private val API_KEY = "180bc6fe1df26611c2259c2f91dee61a"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContentView = View.inflate(this, R.layout.activity_robot, null)
        setContentView(mContentView)
        isEnableHideSoftInputFromWindow = false
        initView()
        initDatas()
        initListener()
    }

    private fun initView() {
        //		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);  和android:fitsSystemWindows="true" 有冲突
        title_bar_back.setOnClickListener(this)
        sendBtn.isEnabled = false
    }

    private fun initDatas() {
        mMsgList.add(ChatMessage("你好，我是小机器人，想和我说什么呢？", ChatMessage.Type.SERVICE, Date()))
        mAdapter = ChatMessageAdapter(this, mMsgList)
        msgListView.adapter = mAdapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.title_bar_back -> finish()
            else -> {
            }
        }
    }

    private fun initListener() {
        mAdapter?.setResendMsgLinsener(object : ReSendMsgLinsener {
            override fun onResendMsg(position: Int) {
                resendMessage(position)
            }
        })
        msgListView.setOnTouchListener{ v, event ->
            hideKeyBroad()
            false
        }
        inputEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                sendBtn.isEnabled = !TextUtils.isEmpty(s)
            }
        })
        sendBtn.setOnClickListener({
//            val messageStr1 = inputEditText.text.toString().trim ()
            val messageStr = inputEditText.text.toString().trim { it <= ' ' }
            sendMessage(messageStr)
        })
    }

    private fun resendMessage(position: Int) {
        if (!isNetConnected(applicationContext)) {
            SnackbarUtils.ShortToast(mContentView, "网络连接失败，请稍后再试！")
            return
        }
        val messageStr = mMsgList[position].msg
        mMsgList.removeAt(position)
        mAdapter?.notifyDataSetChanged()
        if (messageStr != null) {
            sendMessage(messageStr)
        }
    }

    private fun sendMessage(messageStr: String) {
        if (TextUtils.isEmpty(messageStr)) {
            return
        }
        if (!isNetConnected(applicationContext)) {
            val customerMessage = ChatMessage()
            customerMessage.date = Date()
            customerMessage.msg = messageStr
            customerMessage.type = ChatMessage.Type.CUSTOMER
            customerMessage.state = -1
            mMsgList.add(customerMessage)
            mAdapter?.notifyDataSetChanged()
            msgListView.setSelection(mMsgList.size - 1)
            inputEditText.setText("")
            return
        }

        val customerMessage = ChatMessage()
        customerMessage.date = Date()
        customerMessage.msg = messageStr
        customerMessage.type = ChatMessage.Type.CUSTOMER
        customerMessage.state = 0
        mMsgList.add(customerMessage)
        mAdapter?.notifyDataSetChanged()
        msgListView.setSelection(mMsgList.size - 1)
        inputEditText.setText("")
        sendMessage(messageStr, object : DisposableObserver<String>() {
            override fun onNext(response: String) {
                LogUtil.e(TAG, "sendMessage  onSuccess$response")

                if (response != "") {
                    var result: ChatResult? = null
                    try {
                        result = mGson.fromJson(response, ChatResult::class.java)
                        if(result == null){
                            return
                        }
                        val chatMessage = ChatMessage()
                        chatMessage.msg = result.text
                        chatMessage.date = Date()
                        chatMessage.type = ChatMessage.Type.SERVICE
                        //发送成功
                        customerMessage.state = 1
                        mMsgList.add(chatMessage)
                        mAdapter?.notifyDataSetChanged()
                        msgListView.setSelection(mMsgList.size - 1)
                        return
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }

                }
                //发送失败
                customerMessage.state = -1
                mAdapter?.notifyDataSetChanged()
            }

            override fun onError(e: Throwable) {
                //发送失败
                customerMessage.state = -1
                mAdapter?.notifyDataSetChanged()
            }

            override fun onComplete() {

            }
        })
    }

    private fun isNetConnected(context: Context): Boolean {
        try {
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val info = connectivity.activeNetworkInfo
                if (info != null && info.isConnected) {
                    if (info.state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun hideKeyBroad() {
        if (currentFocus != null && currentFocus?.windowToken != null) {
            val imInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imInputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * 获取到请求地址
     * @param msg
     * @return
     */
    private fun getUrl(msg: String): String? {
        var url = ""
        try {
            url = (URL + "?key=" + API_KEY + "&info="
                    + URLEncoder.encode(msg, "UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return url
    }

    private fun sendMessage(msg: String, disposableObserver: DisposableObserver<String>) {
        // 得到json格式的结果
        val url = getUrl(msg)
        NetManager.get()
                .url(url)
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(disposableObserver)
    }
}

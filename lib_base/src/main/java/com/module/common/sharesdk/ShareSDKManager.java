package com.module.common.sharesdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.module.common.R;
import java.util.HashMap;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.favorite.WechatFavorite;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 自定义分享界面dialog
 * 分享：微信好友、朋友圈、QQ好友、QQ空间、新浪微博、复制链接、系统分享
 * 登录：微信登录、微博登录、QQ登录
 * @author zh
 */
public class ShareSDKManager {
	
	/**
	 * 显示友盟自定义分享界面
	 * @param context
	 */
	public static void show(final Activity context, final OnekeyShare oks) {
        View view = LayoutInflater.from(context).inflate(R.layout.share_dialog, null);
        //用style控制默认dialog边距问题
        final Dialog dialog = new Dialog(context, R.style.share_dialog);
        dialog.setContentView(view);
        dialog.show();
        // 设置dialog的显示位置，一定要在 show()之后设置
        Window window = dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        
        // 监听
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == R.id.ly_share_weichat_circle) {

                    // 分享到朋友圈
                    Platform platformWechat1 = ShareSDK.getPlatform(WechatMoments.NAME);
                    showShare(context, oks, platformWechat1.getName());
                }else if(id == R.id.ly_share_weichat) {
                    // 分享到微信
                    Platform platformWechat = ShareSDK.getPlatform(Wechat.NAME);
                    showShare(context,oks,platformWechat.getName());
                }else if(id == R.id.ly_share_qq_zone) {
                    // 分享到qq空间
                    Platform platformQZone = ShareSDK.getPlatform(QZone.NAME);
                    showShare(context,oks,platformQZone.getName());
                }else if(id == R.id.ly_share_qq) {
                    // 分享到qq
                    //比如分享到QQ，其他平台则只需要更换平台类名，例如Wechat.NAME则是微信
                    Platform platform = ShareSDK.getPlatform(QQ.NAME);
                    showShare(context,oks,platform.getName());
                }else if(id == R.id.ly_share_sina_weibo) {
                    // 分享到qq
                    //比如分享到QQ，其他平台则只需要更换平台类名，例如Wechat.NAME则是微信
                    Platform platformSinaWeibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                    showShare(context,oks,platformSinaWeibo.getName());
                }else if(id == R.id.ly_share_copy_link) {
                    // 复制链接
                    copyTextToBoard(context,"XPlan");
                }else if(id == R.id.ly_share_more_option) {
                    // 更多 调用系统分享
                    systemShareShow(context);
                }else if(id == R.id.tv_cancel_share) {
                    // 取消分享
                    dialog.dismiss();
                }
                dialog.dismiss();
            }
        };
        
        view.findViewById(R.id.ly_share_qq).setOnClickListener(listener);
        view.findViewById(R.id.ly_share_copy_link).setOnClickListener(listener);
        view.findViewById(R.id.ly_share_more_option).setOnClickListener(listener);
        view.findViewById(R.id.ly_share_qq_zone).setOnClickListener(listener);
        view.findViewById(R.id.ly_share_weichat).setOnClickListener(listener);
        view.findViewById(R.id.ly_share_weichat_circle).setOnClickListener(listener);
        view.findViewById(R.id.tv_cancel_share).setOnClickListener(listener);
        view.findViewById(R.id.ly_share_sina_weibo).setOnClickListener(listener);
    }
	
	 /**
     * 复制到剪贴板
     * @param string
     */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void copyTextToBoard(Activity context,String string) {
    	if (TextUtils.isEmpty(string)){
    		 return;
    	}
    	ClipboardManager clip = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    	clip.setText(string);
    	Toast.makeText(context.getApplicationContext(), "已复制至剪贴板", Toast.LENGTH_SHORT).show();
    }
	
	/**
     * 调用系统的应用分享
     * 
     * @param context
     * @param title
     * @param url
     */
    public static void showSystemShareDiglog(Activity context,
	    String title, String url) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享：" + title);
		intent.putExtra(Intent.EXTRA_TEXT, title + " " + url);
		context.startActivity(Intent.createChooser(intent, "选择分享"));
    }

    /**
     * 调用系统分享
     */
    public static void showShare(Activity context,OnekeyShare oks,String platform) {
//		Intent sendIntent = new Intent();
//		sendIntent.setAction(Intent.ACTION_SEND);
//		sendIntent.putExtra(Intent.EXTRA_TEXT, "X Plan客户端");
//		sendIntent.setType("text/plain");
//		startActivity(sendIntent);

//        final OnekeyShare oks = new OnekeyShare();
        //指定分享的平台，如果为空，还是会调用九宫格的平台列表界面
        if (platform != null) {
            oks.setPlatform(platform);
        }
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("标题");
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("ShareSDK");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

        //启动分享
        oks.show(context);
    }

    /**
     * 调用系统分享
     */
    public static void systemShareShow(Activity activity) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "X Plan客户端");
		sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }




//    String picurl="http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg";
//    case R.id.openShare://打开分享面板，直接调用上边写到的方法
//    showShare("sharesdk测试","sharesdk测试",picurl);
//    break;
//    case R.id.QQLogin://QQ登录
//            ShareSDKUtils.Login(QQ.NAME);
//    break;
//    case R.id.WXLogin://微信登录
//            ShareSDKUtils.Login(Wechat.NAME);
//    break;
//    case R.id.SinaLogin://微博登录
//            ShareSDKUtils.Login(SinaWeibo.NAME);
//    break;
//    case R.id.WXLike://收藏分享(微信需要有图片)
//            ShareSDKUtils.shareWXF("微信收藏分享测试标题sharesdk","微信收藏分享测试内容sharesdk",picurl,null);
//    break;
//    case R.id.WXfriendsshare://朋友圈分享(微信需要有图片)
//            ShareSDKUtils.shareWXM("朋友圈分享测试标题sharesdk","朋友圈分享测试内容sharesdk",picurl,null);
//    break;
//    case R.id.WXshare://微信分享(微信需要有图片)
//            ShareSDKUtils.shareWX("微信分享测试标题sharesdk","微信分享测试内容sharesdk",picurl,null);
//    break;
//    case R.id.Sinashare://微博分享
//            ShareSDKUtils.shareSina("Sina分享测试sharesdk",null,MainActivity.this);
//    break;
//    case R.id.QZONEshare://空间
//            ShareSDKUtils.shareQzone("Qzone分享测试标题sharesdk","Qzone分享测试内容sharesdk",picurl,null);
//    break;
//    case R.id.QQshare://QQ
//            ShareSDKUtils.shareQQ("Qzone分享测试标题sharesdk","Qzone分享测试内容sharesdk",picurl,null);
//    break;

    private static String type="";
    private static ShareSDK myShareSDK =  new ShareSDK();

    /** * 分享到微博 * @param text 文本内容 * @param picUrl 网络图片 （通过审核后才能添加） */
    public static void shareSina(String text,String picUrl,Context context){
        type="share";
        SinaWeibo.ShareParams sp = new SinaWeibo.ShareParams();
        sp.setText(text);
        if(picUrl!=null){
            sp.setImageUrl(picUrl);
        }
        Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
        weibo.setPlatformActionListener(mPlatformActionListener); // 设置分享事件回调
// 执行图文分享
        weibo.share(sp);
    }

    /** * 分享到QQ空间 * @param title 标题 * @param content 内容 * @param PicUrl 图片 * @param titleUrl title链接 */
    public static void shareQzone(String title,String content,String PicUrl,String titleUrl){
        type="share";
        QZone.ShareParams sp = new QZone.ShareParams();
        sp.setTitle(title);
        sp.setText(content);
        if(titleUrl!=null){
            sp.setTitleUrl(titleUrl); // 标题的超链接
        }
        if(PicUrl!=null){
            sp.setImageUrl(PicUrl);
        }
        Platform qzone = ShareSDK.getPlatform(QZone.NAME);
        qzone. setPlatformActionListener (mPlatformActionListener); // 设置分享事件回调
        // 执行图文分享
        qzone.share(sp);
    }

    /** * QQ * @param title * @param content * @param PicUrl * @param titleUrl */
    public static void shareQQ(String title,String content,String PicUrl,String titleUrl){
        type="share";
        QQ.ShareParams sp=new  QQ.ShareParams();
        sp.setTitle(title);
        sp.setText(content);
        if(titleUrl!=null){
            sp.setTitleUrl(titleUrl); // 标题的超链接
        }
        if(PicUrl!=null){
            sp.setImageUrl(PicUrl);
        }
        Platform qq = ShareSDK.getPlatform(QQ.NAME);
        qq. setPlatformActionListener (mPlatformActionListener); // 设置分享事件回调
        // 执行图文分享
        qq.share(sp);
    }

    /** * 收藏 * @param title * @param content * @param PicUrl * @param titleUrl */
    public static void shareWXF(String title,String content,String PicUrl,String titleUrl){
        type="share";
        WechatFavorite.ShareParams sp=new WechatFavorite.ShareParams();
        sp.setTitle(title);
        sp.setText(content);
        if(titleUrl!=null){
            sp.setTitleUrl(titleUrl); // 标题的超链接
        }
        if(PicUrl!=null){
            sp.setImageUrl(PicUrl);
        }
        sp.setShareType(Platform.SHARE_IMAGE);
        sp.setUrl("http://www.163.com/");
        Platform qq = ShareSDK.getPlatform(WechatFavorite.NAME);
        qq. setPlatformActionListener (mPlatformActionListener); // 设置分享事件回调
        // 执行图文分享
        qq.share(sp);
    }


    /** * 朋友圈 * @param title * @param content * @param PicUrl * @param titleUrl */
    public static void shareWXM(String title,String content,String PicUrl,String titleUrl){
        type="share";
        WechatMoments.ShareParams sp=new WechatMoments.ShareParams();
        sp.setTitle(title);
        sp.setText(content);
        if(titleUrl!=null){
            sp.setTitleUrl(titleUrl); // 标题的超链接
        }
        if(PicUrl!=null){
            sp.setImageUrl(PicUrl);
        }
        sp.setShareType(Platform.SHARE_IMAGE);
        sp.setUrl("http://www.sina.com.cn");
        Platform qq = ShareSDK.getPlatform(WechatMoments.NAME);
        qq. setPlatformActionListener (mPlatformActionListener); // 设置分享事件回调
        // 执行图文分享
        qq.share(sp);
    }


    /** * 微信 * @param title * @param content * @param PicUrl * @param titleUrl * Platform.SHARE_TEXT（分享文本）， Platform.SHARE_IMAGE（分享图片）， Platform.SHARE_WEBPAGE（分享网页，既图文分享）， Platform.SHARE_MUSIC（分享音频）， Platform.SHARE_VIDEO（分享视频）， Platform.SHARE_APPS（分享应用，仅微信支持）， Platform.SHARE_FILE（分享文件，仅微信支持） Platform.SHARE_EMOJI（分享表情，仅微信支持） */
    public static void shareWX(String title,String content,String PicUrl,String titleUrl){
        type="share";
        Wechat.ShareParams sp=new Wechat.ShareParams();
        sp.setTitle(title);
        sp.setText(content);
        if(titleUrl!=null){
            sp.setTitleUrl(titleUrl); // 标题的超链接
        }
        if(PicUrl!=null){
            sp.setImageUrl(PicUrl);
        }
        sp.setShareType(Platform.SHARE_IMAGE);
        sp.setUrl("http://qq.com");
        Platform wx = ShareSDK.getPlatform(Wechat.NAME);
        wx. setPlatformActionListener (mPlatformActionListener); // 设置分享事件回调
        // 执行图文分享
        wx.share(sp);
    }

    /**
     *
     * 登录
     */
    public static void Login(String name){
        type="login";
        Platform mPlatform = ShareSDK.getPlatform(name);
        mPlatform.setPlatformActionListener(mPlatformActionListener);
        mPlatform.authorize();//单独授权,OnComplete返回的hashmap是空的
        mPlatform.showUser(null);//授权并获取用户信息
    }

    public static PlatformActionListener mPlatformActionListener= new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            if(type.equals("login")){
                Log.e("onComplete","登录成功");
                Log.e("openid",platform.getDb().getUserId());//拿到登录后的openid
                Log.e("username",platform.getDb().getUserName());//拿到登录用户的昵称
            }else{
                Log.e("onComplete","分享成功");
            }
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            Log.e("onError",throwable.toString()+"");
            if(type.equals("login")){
                Log.e("onError","登录失败"+throwable.toString());
            }else{
                Log.e("onError","分享失败"+throwable.toString());
            }
        }

        @Override
        public void onCancel(Platform platform, int i) {
            if(type.equals("login")){
                Log.e("onCancel","登录取消");
            }else{
                Log.e("onCancel","分享取消");
            }
        }
    };
}

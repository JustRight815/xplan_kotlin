package com.zh.xplan.ui.zxing.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.jaeger.library.StatusBarUtil;
import com.module.common.utils.PixelUtil;
import com.module.common.utils.StreamUtil;
import com.module.common.view.snackbar.SnackbarUtils;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.zxing.DecodeThread;
import com.zh.xplan.ui.zxing.RGBLuminanceSource;
import com.zh.xplan.ui.zxing.camera.CameraManager;
import com.zh.xplan.ui.zxing.util.Utils;
import com.zh.xplan.ui.zxing.view.ViewfinderResultPointCallback;
import com.zh.xplan.ui.zxing.view.ViewfinderView;
import com.zh.zbar.Image;
import com.zh.zbar.ImageScanner;
import com.zh.zbar.Symbol;
import com.zh.zbar.SymbolSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Vector;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 二维码扫描功能
 */
@RuntimePermissions
public class CaptureActivity extends BaseActivity implements Callback, View.OnClickListener {
    private String TAG = "CaptureActivity";
    private View mContentView;

    private static final float BEEP_VOLUME = 0.10f;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    private boolean isLightOn = false;
    private ProgressDialog mProgressDialog;
    private TextView tv_light;
    private TextView tv_gallery;
    private ImageView iv_back;
    private ImageView image;
    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    private final MyHandler mHandler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<CaptureActivity> mCaptureActivity;

        public MyHandler(CaptureActivity captureActivity) {
            mCaptureActivity = new WeakReference<CaptureActivity>(captureActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            final CaptureActivity captureActivity = mCaptureActivity.get();
            if (captureActivity != null) {
                switch (msg.what) {
                    case 1:
                        captureActivity.handleDecode((String) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        }
    }



    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        setActivityFromTranslucent(true);
        mContentView = View.inflate(this, R.layout.activity_capture, null);
        setContentView(mContentView);
        StatusBarUtil.setColor(this,getResources().getColor(R.color.black));
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_light = (TextView) findViewById(R.id.tv_light);
        tv_gallery = (TextView) findViewById(R.id.tv_gallery);
        image  = (ImageView) findViewById(R.id.image);
        tv_light.setOnClickListener(this);
        tv_gallery.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        CaptureActivityPermissionsDispatcher.initCameraWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void initCamera(){
        CameraManager.init(getApplication());
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showRecordDenied(){
        SnackbarUtils.ShortToast(mContentView,"拒绝相机权限将无法进行扫描");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CaptureActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void onRecordNeverAskAgain() {
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //打开系统设置权限
                        Intent intent = getAppDetailSettingIntent(CaptureActivity.this);
                        startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("您已经禁止了相机权限,是否去开启权限")
                .show();
    }

    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    public static Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_light:
                switchLight();
                break;
            case R.id.tv_gallery:
                showPictures(123);
                break;
            default:
                break;
        }
    }

    /**
     * 开关闪关灯
     */
    public void switchLight(){
        if(isLightOn){
            CameraManager.get().switchFlashlight(false);
        }else{
            CameraManager.get().switchFlashlight(true);
        }
        isLightOn=!isLightOn;
    }

    public void showPictures(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photo_path;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 123:
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor.moveToFirst()) {
                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        photo_path = cursor.getString(colum_index);
                        if (photo_path == null) {
                            photo_path = Utils.getPath(getApplicationContext(), data.getData());
                        }
                        zbarDecodeQRCode(photo_path);
                    }
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView.setVisibility(View.VISIBLE);
        surfaceView.postDelayed(new Runnable() {
            @Override
            public void run() {
                surfaceView.setVisibility(View.VISIBLE);
            }
        },100);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            post(new Runnable() {
                @Override
                public void run() {
                    initCamera(surfaceHolder);
                }
            });
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }

        try{
            initBeepSound();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        vibrate = true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler.clear();
            handler = null;
        }
    }

    /*
     * （非 Javadoc）
     *
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        // TODO 自动生成的方法存根
        super.onStop();

        try{
            CameraManager.get().closeDriver();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewfinderView = null;
        if(null!=mediaPlayer)
            mediaPlayer.release();
        mediaPlayer = null;
        if(null!=mProgressDialog && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    private void showNoticeDialog(String resultString) {
        final Dialog dialog = new Dialog(this,R.style.jdPromptDialog);
        View view = this.getLayoutInflater().inflate(R.layout.dialog,null);
        TextView msg = (TextView) view.findViewById(R.id.msg);
        msg.setText(resultString);
        view.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartDecodeThread();
                image.setVisibility(View.GONE);
            }
        });
        dialog.setContentView(view);
//        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 重启扫码线程
     *
     * NOTE:
     * 扫到文本或非白名单URL之后，在扫码页面弹出提示框，需要在提示框消失的时候重启扫码线程，在BarcodeUtils类中调用
     *
     */
    public void restartDecodeThread() {
        if (handler != null) {
            handler.quitSynchronously();
            handler.clear();
            handler = new CaptureActivityHandler(decodeFormats, characterSet);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            CameraManager.get().getFramingRect();
        } catch (IOException ioe) {
//            Toast.makeText(CaptureActivity.this, "启动照相机失败，请检查设备并开放权限", Toast.LENGTH_SHORT).show();
            return;
        } catch (RuntimeException e) {
//            Toast.makeText(CaptureActivity.this, "启动照相机失败，请检查设备并开放权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            post(new Runnable() {
                @Override
                public void run() {
                    initCamera(holder);
                }
            });
        }
    }
    public void post(final Runnable action) {
        new Handler().post(action);
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
        Rect rect = CameraManager.get().getFramingRect();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.txtResult).getLayoutParams();
        params.setMargins(0,rect.bottom+ PixelUtil.dp2px(15,this),0,0);
        findViewById(R.id.txtResult).invalidate();
        findViewById(R.id.txtResult).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_light).setVisibility(View.VISIBLE);
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            try {
            	File file = new File(getCacheDir(), "beep.ogg");
				InputStream is = getResources().openRawResource(R.raw.beep);
				StreamUtil.saveStreamToFile(is, file.getAbsolutePath());
				mediaPlayer.setDataSource(file.getAbsolutePath());
				
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    /**
     * 扫描成功声音提示
     */
    private void playBeepSoundAndVibrate() {
//        if (playBeep && mediaPlayer != null) {
//            mediaPlayer.start();
//        }
//        if (vibrate) {
//            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            vibrator.vibrate(VIBRATE_DURATION);
//        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
    protected String photo_path;


    private String recode(String str) {
        String format = "";

        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
            if (ISO) {
                format = new String(str.getBytes("ISO-8859-1"), "GB2312");
            } else {
                format = str;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return format;
    }



    /**
     * 判断是否支持闪光灯
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private boolean isSupportFlashLight() {
        FeatureInfo[]  features = getPackageManager().getSystemAvailableFeatures();

        if(null == features){
            return false;
        }

        for(FeatureInfo f : features) {
            if(PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This class handles all the messaging which comprises the state machine for
     * capture.
     */
    public final class CaptureActivityHandler extends Handler {

        private DecodeThread decodeThread;
        private State state;
        private WeakReference<CaptureActivity> softReference;

        public CaptureActivityHandler(Vector<BarcodeFormat> decodeFormats, String characterSet) {
            softReference = new WeakReference<CaptureActivity>(CaptureActivity.this);

            CaptureActivity captureActivity;
            if(softReference != null){
                captureActivity = softReference.get();
                if(captureActivity == null){
                    captureActivity = CaptureActivity.this;
                    softReference = new WeakReference<CaptureActivity>(CaptureActivity.this);
                }
            }else{
                captureActivity = CaptureActivity.this;
                softReference = new WeakReference<CaptureActivity>(CaptureActivity.this);
            }

            decodeThread = new DecodeThread(captureActivity, decodeFormats, characterSet,
                    new ViewfinderResultPointCallback(CaptureActivity.this.getViewfinderView()));
            decodeThread.start();
            state = State.SUCCESS;
            // Start ourselves capturing previews and decoding.
            try{
                CameraManager.get().startPreview();
            }catch (Exception e){

            }
            restartPreviewAndDecode();
        }

        @Override
        public void handleMessage(Message message) {
//            if (Log.D) {
//                Log.d(TAG, "CaptureActivity handleMessage");
//            }
            CaptureActivity captureActivity = softReference.get();
            if (captureActivity != null) {
                switch (message.what) {
                    case R.id.auto_focus:
                        // Log.d(TAG, "Got auto-focus message");
                        // When one auto focus pass finishes, start another. This is the
                        // closest thing to
                        // continuous AF. It does seem to hunt a bit, but I'm not sure what
                        // else to do.
                        if (state == State.PREVIEW) {
                            try{
                                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        break;
                    case R.id.restart_preview:
                        restartPreviewAndDecode();
                        break;
                    case R.id.decode_succeeded:
                        state = State.SUCCESS;
                        Bundle bundle = message.getData();

                        /***********************************************************************/
                        // Bitmap scan_barcode = bundle == null ? null : (Bitmap) bundle
                        // .getParcelable(DecodeThread.BARCODE_BITMAP);

                        CaptureActivity.this.handleDecode((String) message.obj);
                        break;
                    case R.id.decode_failed:
                        // We're decoding as fast as possible, so when one decode fails,
                        // start another.
                        state = State.PREVIEW;
                        try{
                            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                        }catch (Exception e){
                            e.printStackTrace();
                            return;
                        }
                        break;
                    case R.id.return_scan_result:
                        CaptureActivity.this.setResult(Activity.RESULT_OK, (Intent) message.obj);
                        CaptureActivity.this.finish();
                        break;
                    case R.id.launch_product_query:
                        String url = (String) message.obj;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        CaptureActivity.this.startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        }

        public void quitSynchronously() {
            state = State.DONE;
            try{
                CameraManager.get().stopPreview();
            }catch (Exception e){
                e.printStackTrace();
            }
            Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
            quit.sendToTarget();
            try {
                decodeThread.join();
            } catch (InterruptedException e) {
                // continue
            }

            // Be absolutely sure we don't send any queued up messages
            removeMessages(R.id.decode_succeeded);
            removeMessages(R.id.decode_failed);
        }

        private void restartPreviewAndDecode() {
            if (state == State.SUCCESS) {
                state = State.PREVIEW;
                try{
                    CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                    CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                }catch (Exception e){
                    e.printStackTrace();
                }
                CaptureActivity.this.drawViewfinder();
            }
        }


        public void clear()
        {
            if(null!=decodeThread)
                decodeThread.interrupt();
            decodeThread = null;
        }
    }

    @Override
    public void onBackPressed() {
        //mLoader.cancelPopu();
        super.onBackPressed();
    }

    /**
     * 同步解析本地图片二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param picturePath 要解析的二维码图片本地路径
     * @return 返回二维码图片里的内容 或 null
     */
    public String zbarDecodeQRCode(final String picturePath) {

        if (picturePath != null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    String resultString = "";
                    resultString = getZbarDecodeResult(picturePath,true);
                    if(resultString == null || TextUtils.isEmpty(resultString)){
                        Log.e("zh","Zbar 未识别，Zxing 再识别一次");
                        resultString = getZxingDecodeResult(picturePath);
                    }
                    Message msg= Message.obtain();
                    msg.what= 1;
                    msg.obj = resultString;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
        return null;
    }

    private String getZbarDecodeResult(String picturePath,boolean isMore) {
        //速度快识别率比较高 图片越大越容易显示出来，但是斜着手机扫一扫扫不出来，旋转45度的二维码识别不了 彩色的二维码识别不了 但是又容易出现oom，模糊的不好识别
        String resultString = "";
        final Bitmap bitmap = getDecodeAbleBitmap(picturePath,isMore);// decode bitmap
        if(bitmap == null){
            return resultString;
        }
        int W = bitmap.getWidth();
        int H = bitmap.getHeight();
        int[] photodata = new int[W * H];
        bitmap.getPixels(photodata, 0, W, 0, 0, W, H); //获取图片原始ARGB数据
        //将RGB转为灰度数据。
        byte[] greyData = new byte[W * H];
        for (int i = 0; i < greyData.length; i++) {
            greyData[i] = (byte) ((((photodata[i] & 0x00ff0000) >> 16)
                    * 19595 + ((photodata[i] & 0x0000ff00) >> 8)
                    * 38469 + ((photodata[i] & 0x000000ff)) * 7472) >> 16);
        }

        Image barcode = new Image(W, H, "GREY");
        barcode.setData(greyData);
        ImageScanner scanner = new ImageScanner();

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                image.setVisibility(View.VISIBLE);
//                image.setImageBitmap(bitmap);
//            }
//        });

        int ret = scanner.scanImage(barcode);
        Log.e("zbh", "&&&&&&&&&&&&&&&ret:  " + ret);
        if (ret != 0) {
            SymbolSet syms = scanner.getResults();
            resultString = "";
            for (Symbol sym : syms) {
                resultString += "\n" + sym.getData() + "\n";
                Log.e("zbh", "sym.getData()" + resultString);
            }
        }
        bitmap.recycle();
        return resultString;
    }




    private String getZxingDecodeResult(String picturePath) {
        //倾斜的图片识别不了,但是可以斜着手机扫一扫 旋转45度的二维码可以识别   识别率很低
        String resultString = "";
        final Bitmap bitmap = getZXingBitmap(picturePath);
        if(bitmap == null){
            return resultString;
        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                image.setVisibility(View.VISIBLE);
//                image.setImageBitmap(bitmap);
//            }
//        });
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        try {
            Result result = multiFormatReader.decode(binaryBitmap);
            if(result != null && ! TextUtils.isEmpty(result.getText())){
                Log.e("zh","Zbar 未识别，Zxing 解析结果" + result.getText());
                resultString = result.getText();
            }
        } catch (Exception e) {
            resultString = null;
        }
        bitmap.recycle();
        return resultString;
    }


    private Bitmap getDecodeAbleBitmap(String picturePath,boolean isMore) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);

            int width = options.outWidth;
            int height = options.outHeight;
            float maxWidth = 800;
            float maxHeight = 800;
//            Log.e("zbh", "压缩之前w: " + width);
//            Log.e("zbh", "压缩之前H: " + height);
//            Log.e("zbh", "压缩之前w * H: " + width*height);
//            Log.e("zbh", "压缩之前w * H 大小+++++++++++: " + FormetFileSize(width*height));
//            Log.e("zbh", "压缩之前H22 大小+++++++++++: " + FormetFileSize(width*height*4));
//            Log.e("zbh", "压缩之前H22 大小  12925248 +++++++++++: " + FormetFileSize(10000000));

            int sampleSize = 1;
//            if(isMore && width * height < 10000000){
//
//            }else{
////                if(isMore){
////                    return null;
////                }
                if(width > height && width > maxWidth) {
                    int scale = (int) (width / maxWidth);
                    if(height/scale < maxHeight){
                        scale = (int) (height / maxHeight);
                    }
                    sampleSize = scale;
                }else if (width < height && height > maxHeight) {
                    sampleSize = (int) (height / maxHeight);
                }
                if(sampleSize <= 0){
                    sampleSize = 1;
                }
//            }

            options.inSampleSize = sampleSize;
            Log.e("zbh", "压缩inSampleSize zoomRatio : " + sampleSize);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);

//            int w1 = bitmap.getWidth();
//            int h1 = bitmap.getHeight();
//            int h2 = bitmap.getByteCount();
//            Log.e("zbh", "压缩后w: " + w1);
//            Log.e("zbh", "压缩后H: " + h1);
//            Log.e("zbh", "压缩后w * H: " + w1 * h1);
//            Log.e("zbh", "压缩后w * H 大小****** : " + FormetFileSize(w1 * h1));
//            Log.e("zbh", "压缩后w * H 22 大小******: " + FormetFileSize(h2));


            // 检查图像是否旋转，如果旋转了，可以把图像给旋转过来，，影响速度
            ExifInterface exif;
            try {
                exif = new ExifInterface(picturePath);

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                boolean flag = false;
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    flag = true;
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    flag = true;
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    flag = true;
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                if(flag){
                    Log.e("zbh", "需要旋转  ");
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                            matrix, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//             w1 = bitmap.getWidth();
//             h1 = bitmap.getHeight();
//             h2 = bitmap.getByteCount();
//            Log.e("zbh", "matrix 压缩后w: " + w1);
//            Log.e("zbh", "matrix 压缩后H: " + h1);
//            Log.e("zbh", "matrix 压缩后w * H: " + w1 * h1);
//            Log.e("zbh", "matrix 压缩后w * H 大小****** : " + FormetFileSize(w1 * h1));
//            Log.e("zbh", "matrix 压缩后w * H 22 大小******: " + FormetFileSize(h2));
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
//            int compressedLen = baos.toByteArray().length; // 这里out.toByteArray()所返回的byte[]数组大小确实变小了！
//            Bitmap compressedBm = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, compressedLen);
            bitmap = bitmap2Gray(bitmap);
            return bitmap;
        } catch (Exception e) {
            Log.e("zh", "Exception() " + e.toString());
            return null;
        }
    }

    /**
     * 灰度化处理 zbar识别不了彩色的，zxing可以
     * @param bmSrc
     * @return
     */
    public Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    /**
     * 转换文件大小
     * @param fileS
     * @return
     */
    private static String FormetFileSize(long fileS)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize="0B";
        if(fileS==0){
            return wrongSize;
        }
        if (fileS < 1024){
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576){
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        }
        else if (fileS < 1073741824){
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }
        else{
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 处理扫描结果
     *
     * @param result
     */
    public void handleDecode(String result) {
        playBeepSoundAndVibrate();
        if(result == null || TextUtils.isEmpty(result)){
            showNoticeDialog("图片解析错误");
            return;
        }else{
            showNoticeDialog(result);
        }
    }


    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩。感谢 https://github.com/devilsen 提的 PR
     *
     * @param picturePath 本地图片文件路径
     * @return
     */
    private Bitmap getZXingBitmap(String picturePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int sampleSize = options.outHeight / 400;
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(picturePath, options);
        } catch (Exception e) {
            return null;
        }
    }

}
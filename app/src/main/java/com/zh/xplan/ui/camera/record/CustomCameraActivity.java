package com.zh.xplan.ui.camera.record;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.module.common.log.LogUtil;
import com.module.common.view.snackbar.SnackbarUtils;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.camera.RecordVideoSet;
import com.zh.xplan.ui.camera.utils.AndroidUtilities;
import com.zh.xplan.ui.camera.utils.NotificationCenter;
import com.zh.xplan.ui.view.camera.RecordedButton;

import java.io.File;

import static android.view.View.VISIBLE;

/**
 * 自定义相机拍摄视频
 */
public class CustomCameraActivity extends BaseActivity implements View.OnClickListener  , NotificationCenter.NotificationCenterDelegate, RecordedButton.ProgressListener {
    private View contentView;
    private FrameLayout cameraLayout;//显示相机的布局
    private CameraView cameraView;//显示相机view

    private RelativeLayout cameraControlLayout;//相机控制布局
    private ImageView switchCameraBtn;//切换前后相机
    private RecordedButton recordBtn;//录制视频按钮
    private ImageView flashBtn;//闪光灯
    private ImageView backBtn;//返回按钮
    private TextView tvRecordTime;//录制时间
    private TextView tvTouchShoot;//长按摄像提示

    private RelativeLayout cameraResultLayout;//录制结果布局
    private ImageView imgRecordRepleal;//废弃视频
    private ImageView imgRecordSave;//保存视频
    private TextView tvRecordMessage;//录制提示

    private boolean flashAnimationInProgress;//闪光灯动画
    private boolean cameraOpened;

    private int videoRecordTime;//录制视频计时
    private Runnable videoRecordRunnable;//录制视频计时

    private RecordVideoSet recordVideoSet;//录制视频设置
    private boolean videoTimeTooShort = false;//录制视频时长太短
    private boolean isSmallVideo = false;//是否是录制小视频

    private File cameraFile;//视频录制文件
    private boolean mediaCaptured;
    private boolean deviceHasGoodCamera;
    private boolean paused;
    private boolean requestingPermissions;
    private String initRecordTimes;
    private boolean pressed;
    private boolean dragging;
    private int[] viewPosition = new int[2];
    private static final String RECORD_VIDEO_CONFIG = "RECORD_VIDEO_CONFIG";

    private boolean takingPhoto = false;

    private State state;
    public enum State {
        DEFAULT,
        RECORDING
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.cameraInitied) {
            checkCamera(true);
            cameraControlLayout.bringToFront();
            tvRecordTime.bringToFront();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = View.inflate(this, R.layout.activity_camera,null);
        if (Build.VERSION.SDK_INT >= 21) {
            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        setContentView(contentView);
        initView(contentView);
        initData();
    }


    private void initView(View view) {
        cameraLayout = (FrameLayout) view.findViewById(R.id.cameraLayout);
        recordBtn = (RecordedButton) findViewById(R.id.recordBtn);
        recordBtn.setOnTouchListener(mOnRecordVideoTouchListener);
        recordBtn.setProgressListener(this);
        tvTouchShoot = (TextView) findViewById(R.id.tvTouchShoot);
        cameraResultLayout = (RelativeLayout) findViewById(R.id.cameraResultLayout);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        backBtn  = (ImageView) findViewById(R.id.backBtn);
        tvRecordTime = (TextView) findViewById(R.id.recordTime);
        tvRecordMessage = (TextView) findViewById(R.id.tvRecordMessage);
        imgRecordRepleal = (ImageView) findViewById(R.id.imgRecordRepleal);
        imgRecordSave = (ImageView) findViewById(R.id.imgRecordSave);
        imgRecordRepleal.setOnClickListener(this);
        imgRecordSave.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        FrameLayout  frameLayout = new FrameLayout(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (cameraOpened && !takingPhoto && processTouchEvent(event)) {
                    return true;
                } else {
                    return super.onTouchEvent(event);
                }
            }
        };
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
        cameraLayout.addView(frameLayout);
        cameraControlLayout = (RelativeLayout) findViewById(R.id.cameraControlLayout);
        switchCameraBtn = (ImageView) findViewById(R.id.switchCameraBtn);
        switchCameraBtn.setOnClickListener(this);
        flashBtn.setOnClickListener(this);
    }

    private void initData() {
        state = State.DEFAULT;
        recordVideoSet = (RecordVideoSet) getIntent().getSerializableExtra(RECORD_VIDEO_CONFIG);
        isSmallVideo = recordVideoSet.isSmallVideo();
        initRecordTimes = recordVideoSet.getLimitRecordTime() == 0 ? "00:00" : String.format("%02d:%02d", recordVideoSet.getLimitRecordTime() / 60, recordVideoSet.getLimitRecordTime() % 60);
        tvRecordTime.setText(initRecordTimes);
        tvRecordTime.setAlpha(0.0f);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.cameraInitied);
        checkCamera(true);
        if (deviceHasGoodCamera) {
            CameraController.getInstance().initCamera();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                closeCamera();
                finish();
                break;
            case R.id.switchCameraBtn:
                switchCamera();
                break;
            case R.id.flashBtn:
                switchFlash();
                break;
            case R.id.imgRecordRepleal:
                takingPhoto = false;
                recordRepleal();
                switchControlView(true);
                break;
            case R.id.imgRecordSave:
                takingPhoto = false;
                recordSave();
                break;
            default:
                break;
        }
    }

    /**
     * 检查相机权限、初始化
     * @param request
     */
    public void checkCamera(boolean request) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (request) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 17);
                }
                deviceHasGoodCamera = false;
            } else {
                CameraController.getInstance().initCamera();
                deviceHasGoodCamera = CameraController.getInstance().isCameraInitied();
            }
        } else if (Build.VERSION.SDK_INT >= 16) {
            CameraController.getInstance().initCamera();
            deviceHasGoodCamera = CameraController.getInstance().isCameraInitied();
        }
        if (deviceHasGoodCamera && !cameraOpened) {
            cameraOpened = true;
            showCamera();
        }
    }

    /**
     * 显示相机画面
     */
    public void showCamera() {
        if (cameraView == null) {
            cameraView = new CameraView(this, false);
            cameraLayout.addView(cameraView,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            cameraView.setDelegate(new CameraView.CameraViewDelegate() {
                @Override
                public void onCameraCreated(Camera camera) {
                }

                @Override
                public void onCameraInit() {
                    if (cameraControlLayout != null) {
                        cameraControlLayout.setAlpha(1);
                        cameraControlLayout.setVisibility(View.VISIBLE);
                    }
                    String current = cameraView.getCameraSession().getCurrentFlashMode();
                    String next = cameraView.getCameraSession().getNextFlashMode();
                    if (current.equals(next)) {
                        flashBtn.setVisibility(View.INVISIBLE);
                    } else {
                        setCameraFlashModeIcon(flashBtn, cameraView.getCameraSession().getCurrentFlashMode());
                    }
                    if (switchCameraBtn != null) {
                        switchCameraBtn.setImageResource(cameraView.isFrontface() ? R.drawable.camera_revert1 : R.drawable.camera_revert2);
                        switchCameraBtn.setVisibility(cameraView.hasFrontFaceCamera() ? View.VISIBLE : View.INVISIBLE);
                    }
                }
            });
        }
    }

    /**
     * 切换闪光灯模式
     * @param imageView
     * @param mode
     */
    private void setCameraFlashModeIcon(ImageView imageView, String mode) {
        switch (mode) {
            case Camera.Parameters.FLASH_MODE_OFF:
                imageView.setImageResource(R.drawable.camera_flash_off);
                break;
            case Camera.Parameters.FLASH_MODE_ON:
                imageView.setImageResource(R.drawable.camera_flash_on);
                break;
            case Camera.Parameters.FLASH_MODE_AUTO:
                imageView.setImageResource(R.drawable.camera_flash_auto);
                break;
        }
    }

    private Handler mHandler = new Handler();

    private Runnable runnable =  new Runnable() {
        @Override
        public void run() {
            recordBtn.enlargeBtn();
        }
    };
    /**
     * 监听录制按钮事件
     */
    private View.OnTouchListener mOnRecordVideoTouchListener = new View.OnTouchListener() {
        Long downTime = 0L;
        Long upTime = 0L;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //开始录视频动画
                    downTime = System.currentTimeMillis();
                    LogUtil.e("ACTION_DOWN   downTime :" + (downTime));
                    mHandler.postDelayed(runnable,500);
                    break;
                case MotionEvent.ACTION_UP:
                    //停止录制
                    upTime  = System.currentTimeMillis();
                    if((upTime - downTime ) / 500 >= 1 ){
                        //长按
                        if (recordBtn.videoTime < 1) {
                            videoTimeTooShort = true;
                            ObjectAnimator.ofFloat(tvTouchShoot, "alpha", 0F, 1F).setDuration(100).start();
//                            Toast.makeText(CustomCameraActivity.this, "拍摄时间太短", Toast.LENGTH_LONG).show();
                            SnackbarUtils.ShortToast(contentView,"拍摄时间太短");
                        } else {
                            videoTimeTooShort = false;
                        }
                        recordBtn.narrowBtn();
                    }else{
                        LogUtil.e("upTime - downTime mHandler.removeCallbacks(runnable); :" );
                        mHandler.removeCallbacks(runnable);
                        startTakePicture();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    /**
     * 切换摄像头
     */
    private void switchCamera(){
        if (cameraView == null || !cameraView.isInitied()) {
            return;
        }
        // cameraInitied = false;
        cameraView.switchCamera();
        switchCameraBtn.setImageResource(cameraView.isFrontface() ? R.drawable.camera_revert1 : R.drawable.camera_revert2);
    }

    /**
     * 闪光灯开关
     */
    private void switchFlash(){
        if (flashAnimationInProgress || cameraView == null || !cameraView.isInitied() || !cameraOpened) {
            return;
        }
        String current = cameraView.getCameraSession().getCurrentFlashMode();
        String next = cameraView.getCameraSession().getNextFlashMode();
        if (current.equals(next)) {
            return;
        }
        cameraView.getCameraSession().setCurrentFlashMode(next);
        flashAnimationInProgress = true;
        flashBtn.setVisibility(View.VISIBLE);
        setCameraFlashModeIcon(flashBtn, next);
        flashAnimationInProgress = false;
    }

    /**
     * 开始录制
     */
    @Override
    public void progressStart() {
        startRecord();
    }

    /**
     * 录制结束
     */
    @Override
    public void progressEnd() {
        if (!videoTimeTooShort) {
            //停止录制
            vedioReleased();
        }else{
            //停止录制 废弃录制的视频
            stopRecord();
            //录制时长等于最大时长，强制停止
            if (recordBtn.videoTime >= recordBtn.mMaxMillSecond / 1000) {
                recordBtn.narrowBtn();
            }
        }
        state = State.DEFAULT;
    }

    @Override
    public void onBtnStartEnlarge() {
    }

    @Override
    public void onnEndNarrowBtn() {
        if (!videoTimeTooShort) {
            switchControlView(false);
        }
    }

    /**
     * 切换视频录制操作栏 和 录制结果栏
     */
    private void switchControlView(boolean isShowControl) {
        if (isShowControl) {
            ObjectAnimator.ofFloat(imgRecordRepleal, "translationX", 0f).start();
            ObjectAnimator.ofFloat(imgRecordSave, "translationX", 0f).start();
            ObjectAnimator.ofFloat(tvTouchShoot, "alpha", 0F, 1F).setDuration(100).start();
            cameraControlLayout.setVisibility(View.VISIBLE);
            flashBtn.setVisibility(View.VISIBLE);
            switchCameraBtn.setVisibility(View.VISIBLE);
            cameraResultLayout.setVisibility(View.GONE);
            tvRecordMessage.setVisibility(View.GONE);
        } else {
            flashBtn.setVisibility(View.GONE);
            switchCameraBtn.setVisibility(View.GONE);
            cameraControlLayout.setVisibility(View.GONE);
            cameraResultLayout.setVisibility(VISIBLE);
            if(takingPhoto){
                tvRecordMessage.setVisibility(View.GONE);
            }else{
                tvRecordMessage.setVisibility(View.VISIBLE);
            }
            tvTouchShoot.setAlpha(0f);
            ObjectAnimator.ofFloat(imgRecordRepleal, "translationX", -(float) (getScreenWidth(this) / 3.5)).start();
            ObjectAnimator.ofFloat(imgRecordSave, "translationX", (float) (getScreenWidth(this) / 3.5)).start();
        }
    }

    @SuppressWarnings("deprecation")
    static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getWidth();
    }

    /**
     * 停止录制视频、删除录制的文件
     */
    public void stopRecord(){
        if (mediaCaptured) {
            return;
        }
        if(cameraFile != null){
            cameraFile.delete();
        }
        resetRecordState();
        CameraController.getInstance().stopVideoRecording(cameraView.getCameraSession(), true);
    }

    /**
     * 停止录制视频
     */
    private boolean vedioReleased() {
        if (cameraView == null || mediaCaptured) {
            return true;
        }
        mediaCaptured = true;
        if (state == State.RECORDING) {
            resetRecordState();
            CameraController.getInstance().stopVideoRecording(cameraView.getCameraSession(), false);
            state = State.DEFAULT;
            return true;
        }
        return false;
    }

    /**
     * 开始录制视频
     */
    public void startRecord(){
        if (mediaCaptured || this == null || cameraView == null) {
            return ;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestingPermissions = true;
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 21);
                return ;
            }
        }
        ObjectAnimator.ofFloat(tvTouchShoot, "alpha", 1F, 0F).setDuration(100).start();
        flashBtn.setVisibility(View.GONE);
        switchCameraBtn.setVisibility(View.GONE);
        cameraFile = AndroidUtilities.generateVideoPath();
        tvRecordTime.setAlpha(1.0f);
        tvRecordTime.setText(initRecordTimes);
        videoRecordTime = recordVideoSet.getLimitRecordTime();
        videoRecordRunnable = new Runnable() {
            @Override
            public void run() {
                if (videoRecordRunnable == null) {
                    return;
                }
                if (recordVideoSet.getLimitRecordTime() == 0) {
                    videoRecordTime++;

                } else {
                    videoRecordTime--;
                    if (videoRecordTime == 0) {
                        if (vedioReleased())
                            return;
                    }
                }
                if (recordVideoSet.getLimitRecordSize() != 0) {
                    int b = getSize(cameraFile);
                    if (b > 0 && b >= recordVideoSet.getLimitRecordSize() * 1024 * 1024) {
                        if (vedioReleased())
                            return;
                    }
                }
                tvRecordTime.setText(String.format("%02d:%02d", videoRecordTime / 60, videoRecordTime % 60));
                AndroidUtilities.runOnUIThread(videoRecordRunnable, 1000);
            }
        };
        AndroidUtilities.lockOrientation(CustomCameraActivity.this);
        CameraController.getInstance().recordVideo(cameraView.getCameraSession(), cameraFile, new CameraController.VideoTakeCallback() {
            @Override
            public void onFinishVideoRecording(final Bitmap thumb) {
                if (cameraFile == null || CustomCameraActivity.this == null) {
                    return;
                }
                // Log.d("VV", "录制结束");
                String msg = getVedioSize(cameraFile);
                tvRecordMessage.setText(msg);
                mediaCaptured = false;
            }
        }, new Runnable() {
            @Override
            public void run() {
                AndroidUtilities.runOnUIThread(videoRecordRunnable, 1000);
            }
        },isSmallVideo );
        state = State.RECORDING;
    }

    private String getVedioSize(File convertedFile) {
        String message = "";
        if (convertedFile.exists() && convertedFile.length() != 0 && CustomCameraActivity.this != null) {
            int b = (int) convertedFile.length();
            int kb = b / 1024;
            float mb = kb / 1024f;
            if (recordVideoSet.getLimitRecordSize() != 0 && b > recordVideoSet.getLimitRecordSize() * 1024 * 1024) {
                message += mb > 1 ? CustomCameraActivity.this.getString(R.string.over_video_size_in_mb, mb)
                        : CustomCameraActivity.this.getString(R.string.over_video_size_in_kb, kb);
            } else {
                message += mb > 1 ? CustomCameraActivity.this.getString(R.string.capture_video_size_in_mb, mb)
                        : CustomCameraActivity.this.getString(R.string.capture_video_size_in_kb, kb);
            }
            message += CustomCameraActivity.this.getString(R.string.is_send_video);
        }
        return message;
    }

    private int getSize(File convertedFile) {
        if (!convertedFile.exists() && convertedFile.length() == 0)
            return 0;
        int b = (int) convertedFile.length();
        return b;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!requestingPermissions) {
            if (cameraView != null && state == State.RECORDING) {
                resetRecordState();
                CameraController.getInstance().stopVideoRecording(cameraView.getCameraSession(), false);
                state = State.DEFAULT;
            }
            if (cameraOpened) {
                hideCamera(true);
            }
        } else {
            if (cameraView != null && state == State.RECORDING) {
                state = State.DEFAULT;
            }
            requestingPermissions = false;
        }
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            if (recordVideoSet == null)
                recordVideoSet = getIntent().getParcelableExtra(RECORD_VIDEO_CONFIG);
            checkCamera(true);
            cameraControlLayout.bringToFront();
            tvRecordTime.bringToFront();
        }
        paused = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     *  相机焦点处理
     * @param event
     * @return
     */
    private boolean processTouchEvent(MotionEvent event) {
        if (!pressed && event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                pressed = true;
        } else if (pressed) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL || event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                pressed = false;
                if (dragging) {
                    dragging = false;
                    if (cameraView != null) {
                        if (Math.abs(cameraView.getTranslationY()) > cameraView.getMeasuredHeight() / 6.0f) {
                            closeCamera();
                        } else {
                            cameraControlLayout.setTag(null);
                        }
                    }
                } else {
                    cameraView.getLocationOnScreen(viewPosition);
                    float viewX = event.getRawX() - viewPosition[0];
                    float viewY = event.getRawY() - viewPosition[1];
                    cameraView.focusToPoint((int) viewX, (int) viewY);
                }
            }
        }
        return true;
    }



    private void resetRecordState() {
        flashBtn.setVisibility(View.VISIBLE);
        switchCameraBtn.setAlpha(1.0f);
        switchCameraBtn.setVisibility(View.VISIBLE);
        tvRecordTime.setAlpha(0.0f);
        AndroidUtilities.cancelRunOnUIThread(videoRecordRunnable);
        videoRecordRunnable = null;
    }

    /**
     * 保存录制的视频
     */
    private void recordSave(){
        if (cameraFile == null) {
            return;
        }
        AndroidUtilities.addMediaToGallery(cameraFile.getAbsolutePath());
        closeCamera();
        cameraFile = null;
        finish();
    }

    /**
     * 废弃录制的视频
     */
    private void recordRepleal(){
        if (cameraOpened && cameraView != null && cameraFile != null) {
            cameraFile.delete();
            if (cameraView != null && Build.VERSION.SDK_INT >= 21) {
                cameraView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
            CameraController.getInstance().startPreview(cameraView.getCameraSession());
            cameraFile = null;
        }
    }

    public void hideCamera(boolean async) {
        if (!deviceHasGoodCamera || cameraView == null) {
            return;
        }
        cameraView.destroy(async, null);
        FrameLayout frameLayout = cameraLayout;
        frameLayout.removeView(cameraView);
        cameraView = null;
        cameraOpened = false;
    }


    /**
     * 关闭相机
     */
    public void closeCamera() {
        if (cameraView == null) {
            return;
        }
        cameraControlLayout.setVisibility(View.GONE);
        cameraOpened = false;
        if (Build.VERSION.SDK_INT >= 21) {
            cameraView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * 开始录制视频
     */
    public void startTakePicture(){
        cameraFile = AndroidUtilities.generatePicturePath();
        CameraController.getInstance().takePicture(cameraFile, cameraView.getCameraSession(), new CameraController.onPictureCallBack(){

            @Override
            public void onPictureBack(Bitmap bitmap) {

                if (cameraFile == null) {
                    return;
                }
                if(bitmap != null){
                    LogUtil.e("bitmap != null :" + bitmap.getByteCount());
                }else{
                    LogUtil.e("bitmap == null " );
                }
                int orientation = 0;
                try {
                    ExifInterface ei = new ExifInterface(cameraFile.getAbsolutePath());
                    int exif = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (exif) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            orientation = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            orientation = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            orientation = 270;
                            break;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "run " + e);
                }
                takingPhoto = true;
                switchControlView(false);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

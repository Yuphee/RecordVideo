package com.zyf.vc.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.zyf.vc.IVideoRecorder;
import com.zyf.vc.R;
import com.zyf.vc.helper.CameraHelper;
import com.zyf.vc.module.VideoObject;
import com.zyf.vc.utils.FileUtil;
import com.zyf.vc.view.CameraPreview;
import com.zyf.vc.view.RecordProgressBar;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhangyf on 2016/7/17.
 */
public class RecorderActivity extends AppCompatActivity implements IVideoRecorder {

    private static final String TAG = "RecorderActivity";

    private static final int PLAY_VIDEO_REQUEST_CODE = 100;

    private static int MAX_TIME = 30;

    private Camera mCamera;

    private MediaRecorder mMediaRecorder;

    private CameraPreview mPreview;

    private boolean isRecording = false;

    private RecordProgressBar record_pb;

    private ImageView button_start;

    private ImageView change_camera;

    private ImageView cancel_btn;

    private ImageView open_light;

    private String recordPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!CameraHelper.checkCameraHardware(this)) {
            Toast.makeText(this, "找不到相机，3秒后退出！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(!cameraIsCanUse()){
            Toast.makeText(this, "申请权限失败，请到设置中修改权限！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_preview);

        mPreview = (CameraPreview) findViewById(R.id.camera_preview);
        record_pb = (RecordProgressBar) findViewById(R.id.record_pb);
        button_start = (ImageView) findViewById(R.id.button_start);
        change_camera = (ImageView) findViewById(R.id.change_camera);
        open_light = (ImageView) findViewById(R.id.open_light);
        cancel_btn = (ImageView) findViewById(R.id.cancel_btn);

        record_pb.setRunningTime(MAX_TIME);
        record_pb.setOnFinishListener(new RecordProgressBar.OnFinishListener() {
            @Override
            public void onFinish() {
                stopRecord();
            }
        });

        open_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.toggleFlashMode();
                if(mPreview.getLightState() == 0){
                    open_light.setImageResource(R.mipmap.icon_light_off);
                }else{
                    open_light.setImageResource(R.mipmap.icon_light_on);
                }
            }
        });

        change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPreview.getCameraState() == 0){
                    mPreview.changeToFront();
                }else{
                    mPreview.changeToBack();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    stopRecord();

                }else{
                    startRecord();

                }

            }
        });

        // Create our Preview view and set it as the content of our activity.
//        mPreview = new CameraPreview(this, mCamera);
//        mPreview.initCameraView(mCamera);
//        mPreview.setupCamera(cameraId,mCamera);
//        mPreview.setZOrderOnTop(true);
//        mPreview.setZOrderMediaOverlay(true);
//        final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
//        // 根据需要输出的视频大小调整预览视图高度
//        preview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                preview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                ViewGroup.LayoutParams layoutParams = preview.getLayoutParams();
//                layoutParams.height = (int) (preview.getWidth() / RATIO);
//                preview.setLayoutParams(layoutParams);
//            }
//        });

//        findViewById(R.id.button_start).setOnTouchListener(new RecordButtonTouchListener(this));
    }



    @Override
    public VideoObject startRecord() {
        if (isRecording) {
            Toast.makeText(this, "正在录制中…", Toast.LENGTH_SHORT).show();
            return null;
        }

        // initialize video camera
        if (prepareVideoRecorder()) {
            change_camera.setEnabled(false);
            record_pb.setVisibility(View.VISIBLE);
            record_pb.start();
            button_start.setImageResource(R.mipmap.btn_recording_start);
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            mMediaRecorder.start();

            isRecording = true;
        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user
        }
        return null;
    }

    @Override
    public void stopRecord() {
        if (isRecording) {
            // stop recording and release camera
            try {
                change_camera.setEnabled(true);
                record_pb.setVisibility(View.GONE);
                record_pb.stop();
                button_start.setImageResource(R.mipmap.btn_recording);

                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();  // stop the recording
            } catch (Exception e) {
                // TODO 删除已经创建的视频文件
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            startActivityForResult(new Intent(this, PlayVideoActiviy.class).putExtra(PlayVideoActiviy.KEY_FILE_PATH, recordPath),PLAY_VIDEO_REQUEST_CODE);
            isRecording = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PLAY_VIDEO_REQUEST_CODE:
                    // TODO:
//                    Intent intent = new Intent();
//                    intent.putExtra(PlayVideoActiviy.KEY_FILE_PATH,intent.getStringExtra(PlayVideoActiviy.KEY_FILE_PATH));
//                    setResult(RESULT_OK, intent);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 页面销毁时要停止录制
        stopRecord();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 准备视频录制器
     * @return
     */
    private boolean prepareVideoRecorder(){
        if (!FileUtil.isSDCardMounted()) {
            Toast.makeText(this, "SD卡不可用！", Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = FileUtil.getOutputMediaFile(FileUtil.MEDIA_TYPE_VIDEO);
        if (null == file) {
            Toast.makeText(this, "创建存储文件失败！", Toast.LENGTH_SHORT).show();
            return false;
        }
        recordPath = file.toString();
        mMediaRecorder = new MediaRecorder();
        // 获取最新对象，因为当mPreview中切换相机之后对象指向会有问题
        mCamera = mPreview.getmCamera();
        // Step 1: Unlock and set camera to MediaRecorder
        // 解锁相机以让其他进程能够访问相机，4.0以后系统自动管理调用，但是实际使用中，不调用的话，MediaRecorder.start()报错闪退
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

//        mMediaRecorder.setAudioEncodingBitRate(44100);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        if(CamcorderProfile.hasProfile(mPreview.getCurrentCameraId(), CamcorderProfile.QUALITY_720P)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        }else if(CamcorderProfile.hasProfile(mPreview.getCurrentCameraId(), CamcorderProfile.QUALITY_HIGH)){
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        }else if(CamcorderProfile.hasProfile(mPreview.getCurrentCameraId(), CamcorderProfile.QUALITY_QVGA)){
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA));
        }else if(CamcorderProfile.hasProfile(mPreview.getCurrentCameraId(), CamcorderProfile.QUALITY_480P)){
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        }
//        mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);\
//        mMediaRecorder.setCaptureRate(20);
        mMediaRecorder.setVideoEncodingBitRate(2*1024*1024);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(recordPath);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        mMediaRecorder.setMaxDuration(MAX_TIME*1000);

        if(mPreview.getCameraState() == 0) {
            mMediaRecorder.setOrientationHint(90);
        }else{
            mMediaRecorder.setOrientationHint(270);
        }

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            // 锁相机，4.0以后系统自动管理调用，但若录制器prepare()方法失败，必须调用
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        mCamera = mPreview.getmCamera();
        if (mCamera != null){
            // 虽然我之前并没有setPreviewCallback，但不加这句的话，
            // 后面要用到Camera时，调用Camera随便一个方法，都会报
            // Method called after release() error闪退，推测可能
            // Camera内存泄露无法真正释放，加上这句可以规避该问题
            mCamera.setPreviewCallback(null);
            // 释放前先停止预览
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    private static class RecordButtonTouchListener implements View.OnTouchListener {

        private static final int CANCEL_RECORD_OFFSET = -100;

        private float mDownX, mDownY;

        private RecorderActivity activity;

        private boolean isCancelRecord = false;
        private boolean jumpToNew = true;

        public RecordButtonTouchListener(RecorderActivity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isCancelRecord = false;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    if (null != activity)
                        activity.startRecord();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float y = event.getY();
                    if (y - mDownY < CANCEL_RECORD_OFFSET) {
                        if (!isCancelRecord) {
                            // cancel record
                            isCancelRecord = true;
                            if (null != activity)
                                Toast.makeText(activity, "cancel record", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        isCancelRecord = false;
                    }

                    // 跳转到新的视频录制页面
                    if (jumpToNew && y - mDownY > -CANCEL_RECORD_OFFSET) {
                        if (null != activity) {
//                            activity.startActivity(new Intent(activity, NewRecordVideoActivity.class));
                        }
                        jumpToNew = false;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    // cancel?
//                    if (isCancelRecord) {
//
//                    } else {
//
//                    }
                    if (null != activity)
                        activity.stopRecord();
                    break;
            }

            return true;
        }
    }

    /**
     *  返回true 表示可以使用  返回false表示不可以使用
     */
    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }
}

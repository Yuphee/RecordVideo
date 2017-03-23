package com.zyf.vc.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.zyf.vc.R;
import com.zyf.vc.helper.CameraHelper;
import com.zyf.vc.ui.RecorderActivity;
import com.zyf.vc.utils.MediaUtil;
import com.zyf.vc.utils.SystemVersionUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhangyf on 2016/7/17
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";

    // 用于判断双击事件的两次按下事件的间隔
    private static final long DOUBLE_CLICK_INTERVAL = 200;

    private static int OUTPUT_WIDTH;

    private static int OUTPUT_HEIGHT;

    private Camera mCamera;

    private long mLastTouchDownTime;

    // 对焦动画视图
    private ImageView mFocusAnimationView;

    private ZoomRunnable mZoomRunnable;
    // 0 后置摄像头 1 前置摄像头
    private int cameraState = 0;
    // 0 闪光灯关闭 1 闪光灯开启
    private int lightState = 0;

    private Camera.Size mVideoSize;

    private Context context;

    private Camera.AutoFocusCallback callback;

    private int focusAreaSize;

    private Matrix matrix;

    private RecorderActivity activity;
    private int currentCameraId;
    private boolean haveSize;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        this.activity = (RecorderActivity) context;
        init(activity);
//        this.mCamera = camera;
//        this.context = context;
//        this.activity = (RecorderActivity) context;
//        // Install a SurfaceHolder.Callback so we get notified when the
//        // underlying surface is created and destroyed.
//        getHolder().addCallback(this);
////        // deprecated setting, but required on Android versions prior to 3.0
////        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.activity = (RecorderActivity) context;
        init(activity);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraPreview);
        Drawable focusDrawable = typedArray.getDrawable(R.styleable.CameraPreview_cpv_focusDrawable);
        typedArray.recycle();

//        mFocusAnimationView = new ImageView(context);
//        mFocusAnimationView.setVisibility(INVISIBLE);
//        if (focusDrawable == null) {
//            mFocusAnimationView.setImageResource(R.mipmap.ms_video_focus_icon);
//        } else {
//            mFocusAnimationView.setImageDrawable(focusDrawable);
//        }

        this.context = context;
        this.activity = (RecorderActivity) context;
        init(activity);
    }

    private void init(Activity activity){
        int cameraId = CameraHelper.getDefaultCameraID();
        if (!CameraHelper.isCameraFacingBack(cameraId)) {
            Toast.makeText(activity, "找不到后置相机，3秒后退出！", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
        // Create an instance of Camera
        mCamera = CameraHelper.getCameraInstance(cameraId);
        if (null == mCamera) {
            Toast.makeText(activity, "打开相机失败！", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
        setupCamera(cameraId,mCamera);
        getHolder().addCallback(this);
        callback = new FocusCallBack();
        focusAreaSize = getResources().getDimensionPixelSize(R.dimen.camera_focus_area_size);
        matrix = new Matrix();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Camera.Size size = mCamera.getParameters().getPreviewSize();
//        float ratio = 1f * size.height / size.width;
        int wms = 0;
        int hms = 0;
        if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY){
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
//            int navigationHeight = MediaUtil.getNavigationBarHeight(activity);
//            boolean isNavi = MediaUtil.checkDeviceHasNavigationBar(activity);
//            if(isNavi && navigationHeight > 0){
//                height = height-navigationHeight;
//            }
            wms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            hms = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        }else if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST){
            int width = MeasureSpec.getSize(widthMeasureSpec);
            float ratio = 1f * OUTPUT_WIDTH / OUTPUT_HEIGHT;
            int height = (int) (width/ratio);
            wms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            hms = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(wms, hms);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mCamera.getParameters().isZoomSupported() && event.getDownTime() - mLastTouchDownTime <= DOUBLE_CLICK_INTERVAL) {
                    zoomPreview();
                }
                mLastTouchDownTime = event.getDownTime();
//                float x = event.getX();
//                float y = event.getY();
//                autoFocus();
                // 手动对焦，还存在问题
//                Rect focusRect = calculateTapArea(x, y, 1f);
//                List<Camera.Area> focusAreas = new ArrayList<>();
//                focusAreas.add(new Camera.Area(focusRect, 1000));
//
//                Rect meteringRect = calculateTapArea(x, y, 1.5f);
//                List<Camera.Area> meteringAreas = new ArrayList<>();
//                meteringAreas.add(new Camera.Area(meteringRect, 1000));
//                manualFocus(mCamera,callback,focusAreas,meteringAreas);
                break;
        }
        return super.onTouchEvent(event);
    }

    public class FocusCallBack implements Camera.AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {

        }
    }

    /**
     * 放大预览视图
     */
    private void zoomPreview() {
        Camera.Parameters parameters = mCamera.getParameters();
        int currentZoom = parameters.getZoom();
        int maxZoom = (int) (parameters.getMaxZoom() / 2f + 0.5);
        int destZoom = 0 == currentZoom ? maxZoom : 0;
        if (parameters.isSmoothZoomSupported()) {
            mCamera.stopSmoothZoom();
            mCamera.startSmoothZoom(destZoom);
        } else {
            Handler handler = getHandler();
            if (null == handler)
                return;
            handler.removeCallbacks(mZoomRunnable);
            handler.post(mZoomRunnable = new ZoomRunnable(destZoom, currentZoom, mCamera));
        }
    }

    /**
     * 自动对焦
     */
    public void autoFocus() {
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(null);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        Log.d(TAG, "surfaceDestroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged w: " + w + "---h: " + h);

//        // If your preview can change or rotate, take care of those events here.
//        // Make sure to stop the preview before resizing or reformatting it.
//
//        if (mHolder.getSurface() == null){
//          // preview surface does not exist
//          return;
//        }
//
//        // stop preview before making changes
//        try {
//            mCamera.stopPreview();
//        } catch (Exception e){
//          // ignore: tried to stop a non-existent preview
//        }
//
//        // set preview size and make any resize, rotate or
//        // reformatting changes here
//
//        // start preview with new settings
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//        } catch (Exception e){
//            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//        }
    }

    /**
     * 放大预览视图任务
     *
     * @author Martin
     */
    private static class ZoomRunnable implements Runnable {

        int destZoom, currentZoom;
        WeakReference<Camera> cameraWeakRef;

        public ZoomRunnable(int destZoom, int currentZoom, Camera camera) {
            this.destZoom = destZoom;
            this.currentZoom = currentZoom;
            cameraWeakRef = new WeakReference<>(camera);
        }

        @Override
        public void run() {
            Camera camera = cameraWeakRef.get();
            if (null == camera)
                return;

            boolean zoomUp = destZoom > currentZoom;
            for (int i = currentZoom; zoomUp ? i <= destZoom : i >= destZoom; i = (zoomUp ? ++i : --i)) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setZoom(i);
                camera.setParameters(parameters);
            }
        }
    }

    public void changeToBack(){
        getHolder().removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();//停掉原来摄像头的预览
        mCamera.release();//释放资源
        mCamera = null;//取消原来摄像头
        mCamera = CameraHelper.getCameraInstance(CameraHelper.getDefaultCameraID());//打开当前选中的摄像头
        setupCamera(CameraHelper.getDefaultCameraID(),mCamera);
        getHolder().addCallback(this);
        try {
            mCamera.setPreviewDisplay(getHolder());//通过surfaceview显示取景画面
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();//开始预览
        cameraState = 0;
    }

    public void changeToFront(){
        getHolder().removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();//停掉原来摄像头的预览
        mCamera.release();//释放资源
        mCamera = null;//取消原来摄像头
        mCamera = CameraHelper.getCameraInstance(CameraHelper.getFrontCameraID());//打开当前选中的摄像头
        setupCamera(CameraHelper.getFrontCameraID(),mCamera);
        getHolder().addCallback(this);
        try {
            mCamera.setPreviewDisplay(getHolder());//通过surfaceview显示取景画面
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();//开始预览
        cameraState = 1;
    }

    public void toggleFlashMode() {
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters != null) {
            try {
                final String mode = parameters.getFlashMode();
                if (TextUtils.isEmpty(mode) || Camera.Parameters.FLASH_MODE_OFF.equals(mode)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    lightState = 1;
                }
                else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    lightState = 0;
                }
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                Log.e(" ", "toggleFlashMode", e);
            }
        }
    }

    public int getCameraState() {
        return cameraState;
    }

    public int getLightState() {
        return lightState;
    }

    public Camera getmCamera() {
        return mCamera;
    }

    public int getCurrentCameraId() {
        return currentCameraId;
    }

    public void setCurrentCameraId(int currentCameraId) {
        this.currentCameraId = currentCameraId;
    }

    /**
     * 设置相机参数
     */
    public void setupCamera(int cameraId,Camera mCamera) {
        haveSize = false;
        currentCameraId = cameraId;
//        List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
//        Log.e("zyf","size:"+previewSizes.size());
//        for (int i = 0; i < previewSizes.size(); i++) {
//            Camera.Size psize = previewSizes.get(i);
//            Log.e("zyf", "PreviewSize,width: " + psize.width + " height: " + psize.height);
//        }
        // 设置相机方向
        CameraHelper.setCameraDisplayOrientation(activity, cameraId, mCamera);
        // 设置相机参数
        Camera.Parameters parameters = mCamera.getParameters();
        // 若应用就是用来录制视频的，不用拍照功能，设置RecordingHint可以加快录制启动速度
        // 问题：小米手机录制视频支持的Size和相机预览支持的Size不一样（其他类似的手机可能
        // 也存在这个问题），若设置了这个标志位，会使预览效果拉伸，但是开始录制视频，预览
        // 又恢复正常，暂时不知道是为什么
//        parameters.setRecordingHint(true);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        List<String> whiteBalance = parameters.getSupportedWhiteBalance();
        if (whiteBalance.contains(Camera.Parameters.WHITE_BALANCE_AUTO)){
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

//        parameters.setFocusAreas(null);
        parameters.setMeteringAreas(null);

        int PreviewWidth = 0;
        int PreviewHeight = 0;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);//获取窗口的管理器
        Display display = wm.getDefaultDisplay();//获得窗口里面的屏幕
        PreviewWidth = display.getHeight();
        PreviewHeight = display.getWidth();
        Camera.Parameters mParameters  = mCamera.getParameters();
        // 选择合适的预览尺寸
        List<Camera.Size> sizeList = mParameters.getSupportedPreviewSizes();

        // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
        if (sizeList.size() > 1) {
            Iterator<Camera.Size> itor = sizeList.iterator();
            while (itor.hasNext()) {
                Camera.Size cur = itor.next();
                if (cur.width >= PreviewWidth
                        && cur.height >= PreviewHeight) {
                    PreviewWidth = cur.width;
                    PreviewHeight = cur.height;
                    haveSize = true;
                    break;
                }
            }
            if(!haveSize){// 如果未找到和屏幕适配的size则取最大一个
                Collections.sort(sizeList, new PreviewOrder());
                for (int i = 0; i < sizeList.size(); i++) {
                    Log.e("zyf","sort:"+sizeList.get(i).height);
                }
                PreviewWidth = sizeList.get(0).width;
                PreviewHeight = sizeList.get(0).height;
            }
        }
//        mVideoSize = CameraHelper.getCameraPreviewSizeForVideo(cameraId, mCamera);
//        parameters.setPreviewSize(mVideoSize.width, mVideoSize.height);
        parameters.setPreviewSize(PreviewWidth, PreviewHeight); //获得摄像区域的大小
        Log.e("zyf", "PreviewCurrentSize,width: " + PreviewWidth + " height: " + PreviewHeight);
        mCamera.setParameters(parameters);
        if(!focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            mCamera.cancelAutoFocus();
        }
    }

    public boolean manualFocus(Camera camera, Camera.AutoFocusCallback cb, List<Camera.Area> focusAreas
            ,List<Camera.Area> mFocusAreas) {
        //判断系统是否是4.0以上的版本
        if (camera != null && focusAreas != null && SystemVersionUtil.hasICS()) {
            try {
                camera.cancelAutoFocus();
                Camera.Parameters parameters = camera.getParameters();
                if(parameters != null){
                    // getMaxNumFocusAreas检测设备是否支持
                    if (parameters.getMaxNumFocusAreas() > 0) {
                        parameters.setFocusAreas(focusAreas);
                    }
                    // getMaxNumMeteringAreas检测设备是否支持
                    if (parameters.getMaxNumMeteringAreas() > 0)
                        parameters.setMeteringAreas(mFocusAreas);
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                    camera.setParameters(parameters);
                    camera.autoFocus(cb);
                    return true;
                }
            } catch (Exception e) {
                if (e != null)
                    Log.e(" ", "autoFocus", e);
            }
        }
        return false;
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     * <p>
     * Rotate, scale and translate touch rectangle using matrix configured in
     * {@link SurfaceHolder.Callback#surfaceChanged(SurfaceHolder, int, int, int)}
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    // preview size 排序
    public class PreviewOrder implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return rhs.height*rhs.width - lhs.height*lhs.width;
        }

    }

}
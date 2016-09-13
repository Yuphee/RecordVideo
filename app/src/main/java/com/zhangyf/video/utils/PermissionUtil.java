package com.zhangyf.video.utils;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

/**
 * Created by zhangyf on 2016/9/13.
 */
public class PermissionUtil {

    public static final int CAMERA_STATE_REQUEST_CODE = 0X001;

    public static void requestPermissionForCamera(Activity aty){
        //申请CAMERA权限 存储，相机，音频
        ActivityCompat.requestPermissions(aty,new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, CAMERA_STATE_REQUEST_CODE);
    }

}

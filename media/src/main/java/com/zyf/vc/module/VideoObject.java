package com.zyf.vc.module;

import java.io.File;

/**
 * Created by zhangyf on 2016/7/17.
 */
public class VideoObject {

    /** 视频最大时长，默认10秒 */
    private int mMaxDuration;
    /** 视频目录 */
    private String mOutputDirectory;
    /** 对象文件 */
    private String mOutputObjectPath;
    /** 视频码率 */
    private int mVideoBitrate;
    /** 最终视频输出路径 */
    private String mOutputVideoPath;
    /** 最终视频截图输出路径 */
    private String mOutputVideoThumbPath;
    /** 文件夹及文件名 */
    private String mKey;
    /** 开始时间 */
    private long mStartTime;
    /** 结束时间 */
    private long mEndTime;
    /** 视频移除标志位 */
    private boolean mRemove;

    public static int DEFAULT_VIDEO_BITRATE = 1024*1000;

    public static int DEFAULT_MAX_DURATION = 30;

    /** 两个构造方法 */
    public VideoObject(String key, String path) {
        this(key, path, DEFAULT_VIDEO_BITRATE);
    }
    public VideoObject(String key, String path, int videoBitrate) {
        this.mKey = key;
        this.mOutputDirectory = path;
        this.mVideoBitrate = videoBitrate;
        this.mOutputObjectPath = mOutputDirectory + File.separator + mKey + ".obj";
        this.mOutputVideoPath = mOutputDirectory + File.separator + mKey +".mp4";
        this.mOutputVideoThumbPath = mOutputDirectory + File.separator + mKey + ".jpg";
        this.mMaxDuration = DEFAULT_MAX_DURATION;
    }

}

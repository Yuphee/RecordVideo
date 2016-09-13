package com.zyf.vc;

import com.zyf.vc.module.VideoObject;

/**
 * Created by zhangyf on 2016/7/17.
 */
public interface IVideoRecorder {

    /**
     * 开始录制
     * @return 录制失败返回null
     */
    public VideoObject startRecord();
    /**
     * 停止录制
     */
    public void stopRecord();

}

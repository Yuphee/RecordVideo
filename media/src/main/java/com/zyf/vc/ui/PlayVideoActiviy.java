package com.zyf.vc.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.yqritc.scalablevideoview.ScalableVideoView;
import com.zyf.vc.R;
import com.zyf.vc.utils.FileUtil;
import com.zyf.vc.utils.MediaUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 播放视频页面
 *
 * @author zhangyf
 */
public class PlayVideoActiviy extends Activity {

    public static final String TAG = "PlayVideoActiviy";

    public static final String KEY_FILE_PATH = "file_path";

    private String filePath;

    private ScalableVideoView mScalableVideoView;
    private ImageView mPlayImageView;
    private ImageView mThumbnailImageView;
    private ImageView btn_cancel;
    private ImageView btn_sure;
    private static PickFinishListener listener;

    public interface PickFinishListener{
        void onPickFinish(String path);
    }

    public static void setOnPickFinishListener(PickFinishListener pickFinishListener) {
        listener = pickFinishListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePath = getIntent().getStringExtra(KEY_FILE_PATH);
        Log.d(TAG, filePath);
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, "视频路径错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_play_video);
        mScalableVideoView = (ScalableVideoView) findViewById(R.id.video_view);
        try {
            // 这个调用是为了初始化mediaplayer并让它能及时和surface绑定
            mScalableVideoView.setDataSource("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayImageView = (ImageView) findViewById(R.id.playImageView);
        btn_cancel = (ImageView) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.putExtra(KEY_FILE_PATH,filePath);
//                setResult(RESULT_CANCELED, intent);
                FileUtil.deleteFile(filePath);
                finish();
            }
        });
        btn_sure = (ImageView) findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.putExtra(KEY_FILE_PATH,filePath);
//                setResult(RESULT_OK, intent);
                if(listener != null){
                    listener.onPickFinish(filePath);
                }
                MediaUtil.notifyGallery(PlayVideoActiviy.this,filePath);
                finish();
            }
        });

        mThumbnailImageView = (ImageView) findViewById(R.id.thumbnailImageView);
        mThumbnailImageView.setImageBitmap(getVideoThumbnail(filePath));
    }

    /**
     * 获取视频缩略图（这里获取第一帧）
     * @param filePath
     * @return
     */
    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(TimeUnit.MILLISECONDS.toMicros(1));
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.video_view){
            mScalableVideoView.stop();
            mPlayImageView.setVisibility(View.VISIBLE);
            mThumbnailImageView.setVisibility(View.VISIBLE);
        }else if(id == R.id.playImageView){
            try {
                mScalableVideoView.setDataSource(filePath);
                mScalableVideoView.setLooping(true);
                mScalableVideoView.prepare();
                mScalableVideoView.start();
                mPlayImageView.setVisibility(View.GONE);
                mThumbnailImageView.setVisibility(View.GONE);
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
                Toast.makeText(this, "播放视频异常", Toast.LENGTH_SHORT).show();
            }
        }
//        switch (v.getId()) {
//            case R.id.video_view:
//                mScalableVideoView.stop();
//                mPlayImageView.setVisibility(View.VISIBLE);
//                mThumbnailImageView.setVisibility(View.VISIBLE);
//                break;
//            case R.id.playImageView:
//                try {
//                    mScalableVideoView.setDataSource(filePath);
//                    mScalableVideoView.setLooping(true);
//                    mScalableVideoView.prepare();
//                    mScalableVideoView.start();
//                    mPlayImageView.setVisibility(View.GONE);
//                    mThumbnailImageView.setVisibility(View.GONE);
//                } catch (IOException e) {
//                    Log.e(TAG, e.getLocalizedMessage());
//                    Toast.makeText(this, "播放视频异常", Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
    }


}

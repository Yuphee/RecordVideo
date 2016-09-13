package com.zhangyf.video;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zhangyf.video.bottomdialog.ActionSheetDialog;
import com.zhangyf.video.bottomdialog.OnOperItemClickL;
import com.zhangyf.video.utils.PermissionUtil;
import com.zyf.vc.ui.PlayVideoActiviy;
import com.zyf.vc.ui.RecorderActivity;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_TAKE_VIDEO_CODE = 1001;

    @Bind(R.id.path)
    TextView pathTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        PlayVideoActiviy.setOnPickFinishListener(new PlayVideoActiviy.PickFinishListener() {
            @Override
            public void onPickFinish(String path) {
                pathTv.setText(path);
                Toast.makeText(MainActivity.this,"视频已保存在"+path,Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick(R.id.start)
    void onBtnClick(View view){
        switch (view.getId()){
            case R.id.start:
                startActionSheetDialog();
                break;
        }
    }

    private void startActionSheetDialog() {
        final String[] stringItems;
        stringItems = new String[]{"拍摄视频"};
        final ActionSheetDialog dialog = new ActionSheetDialog(this, stringItems, null);
        dialog.widthScale(1f)
                .layoutAnimation(null)
                .isTitleShow(false)
                .dividerHeight(0.5f)
                .cornerRadius(0)
                .itemTextColor(Color.parseColor("#333333"))
                .cancelText(Color.parseColor("#333333"))
                .itemPressColor(Color.parseColor("#cfcfcf"))
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 拍摄视频
                checkTakeMediaPermission();
                dialog.dismiss();
            }
        });
    }

    private void checkTakeMediaPermission() {
        int permissionState_Camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionState_Storage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionState_Audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionState_Camera != PackageManager.PERMISSION_GRANTED || permissionState_Storage != PackageManager.PERMISSION_GRANTED
                || permissionState_Audio != PackageManager.PERMISSION_GRANTED) {
            // 用户已经拒绝过一次，给用户解释提示对话框
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // 展示自定义提醒框
                new MaterialDialog.Builder(this)
                        .content("你必须允许相应权限")
                        .positiveText("确定")
                        .positiveColor(getResources().getColor(R.color.color_theme))
                        .negativeText("取消")
                        .negativeColor(getResources().getColor(R.color.color_theme))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                PermissionUtil.requestPermissionForCamera(MainActivity.this);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build()
                        .show();
            } else {
                PermissionUtil.requestPermissionForCamera(this);
            }
        } else {
            skipToVideoTaker();
        }
    }

    /**
     * 跳转视频拍摄
     */
    private void skipToVideoTaker(){
        startActivityForResult(new Intent(this, RecorderActivity.class),REQUEST_TAKE_VIDEO_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            switch (requestCode) {
                case PermissionUtil.CAMERA_STATE_REQUEST_CODE:
                    skipToVideoTaker();
                    break;
            }
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
        }
    }
}

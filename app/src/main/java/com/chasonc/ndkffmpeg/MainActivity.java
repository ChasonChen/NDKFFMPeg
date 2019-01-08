package com.chasonc.ndkffmpeg;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnClickListener{


    Executor EXECUTOR = Executors.newSingleThreadExecutor();

    final static int SELECT_VIDEO_REQUEST = 100;
    final static int SELECT_IMAGE_REQUEST = 101;
    final static int REQUEST_READ_EXTERNAL_STORAGE = 102;

    TextView tvPath;
    Button btSelectVideo;
    ImageView ivGif;
    ProgressDialog progressDialog;
    VideoView vvVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btSelectVideo = findViewById(R.id.bt_get_video);
        tvPath = findViewById(R.id.tv_video_path);
        ivGif = findViewById(R.id.iv_gif);
        vvVideo = findViewById(R.id.vv_video);
        vvVideo.setMediaController(new MediaController(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("截取中...");

        btSelectVideo.setOnClickListener(this);
        findViewById(R.id.bt_get_image).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_get_video:
                selectMedia("video/*",SELECT_VIDEO_REQUEST);
                break;

            case R.id.bt_get_image:
                selectMedia("image/*",SELECT_IMAGE_REQUEST);
                break;
        }
    }

    private void selectMedia(String type,int requestCode){
        if (isGranted()) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
            intent.setType(type);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, requestCode);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    // 设备根目录路径
    private String savedPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || null == data) return;
        switch (requestCode){
            case SELECT_IMAGE_REQUEST:
                chooseVideo(data);
                break;

            case SELECT_VIDEO_REQUEST:
                Uri uri = data.getData();
                final String path = PathUtils.getImageAbsolutePath(this, uri);
                showVideoPath(path);
                break;
        }
    }

    private void chooseVideo(Intent data) {
        Uri uri = data.getData();
        final String path = PathUtils.getImageAbsolutePath(this, uri);
        File file = new File(path);
        final String parent = file.getParent();

        startLoading();
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                //执行指令
                cmdRun(Commend.imagesToVideo(parent,parent));

                // 隐藏loading
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvPath.setText(path);

                        vvVideo.setVideoPath(path+"/output/output.mp4");
                        vvVideo.start();

                        stopLoading();
                    }
                });
            }
        });
    }

    private void showVideoPath(final String path) {
        vvVideo.setVideoPath(path);
        vvVideo.start();

        startLoading();
        new Thread() {
            @Override
            public void run() {
                super.run();
                 //执行指令
                cmdRun(Commend.videoToGif(path,savedPath));

                // 隐藏loading
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopLoading();
                        // 显示gif
                        Glide.with(MainActivity.this)
                                .load(new File(savedPath + "/video_100.gif"))
                                .into(ivGif);
                    }
                });
            }
        }.start();
    }

    private void stopLoading() {
        progressDialog.dismiss();
    }

    @NonNull
    private void startLoading() {
        // 显示loading
        progressDialog.show();
    }

    /**
     * 以空格分割指令，生成String类型的数组
     *
     * @param cmd 指令
     * @return 执行code
     */
    private int cmdRun(String cmd) {
        String regulation = "[ \\t]+";
        final String[] split = cmd.split(regulation);
        return FFmpeg.run(split);
    }


    private boolean isGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

}

package com.chasonc.ndkffmpeg;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ffmpeg");
    }

    final static int SELECT_VIDEO_REQUEST = 100;
    final static int SELECT_IMAGE_REQUEST = 101;
    final static int REQUEST_READ_EXTERNAL_STORAGE = 102;

    TextView tvPath;
    Button btSelectVideo;
    ImageView ivGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btSelectVideo = findViewById(R.id.bt_get_video);
        tvPath = findViewById(R.id.tv_video_path);
        ivGif = findViewById(R.id.iv_gif);

        btSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGranted()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, SELECT_VIDEO_REQUEST);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                }
            }
        });

        findViewById(R.id.bt_get_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGranted()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, SELECT_IMAGE_REQUEST);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                }
            }
        });

    }
    // 设备根目录路径
    private String savedPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || null == data) return;


        switch (requestCode){
            case 110:
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
        String parent = file.getParent();

        // 显示loading
        final ProgressDialog[] progressDialog = {new ProgressDialog(this)};
        progressDialog[0].setTitle("截取中...");
        progressDialog[0].show();

        final String cmd = "ffmpeg -threads 4 -y -r 20 -i "+parent+"/image%04d.jpg "+parent+"/output/output.mp4";
        new Thread() {
            @Override
            public void run() {
                super.run();
                //执行指令
                cmdRun(cmd);

                // 隐藏loading
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvPath.setText(path);
                        progressDialog[0].dismiss();
                    }
                });
            }
        }.start();
//        showVideoPath(path);
    }

    private void showVideoPath(final String path) {
        // 截取视频的前100帧
        final String cmd = "ffmpeg -i " + path + " -vframes 100 -y -f gif -s 480X320 " + savedPath + "/video_100.gif";
        // 显示loading
        final ProgressDialog[] progressDialog = {new ProgressDialog(this)};
        progressDialog[0].setTitle("截取中...");
        progressDialog[0].show();

        new Thread() {
            @Override
            public void run() {
                super.run();
                 //执行指令
                cmdRun(cmd);

                // 隐藏loading
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog[0].dismiss();
//                        progressDialog[0] = null;
//
//                        // 显示gif
//                        Glide.with(MainActivity.this)
//                                .load(new File(savedPath + "/video_500.gif"))
//                                .into(ivGif);
                    }
                });
                int angle = FFmpeg.getVideoAngle(path);
                Log.d("======",angle+"");
            }
        }.start();
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

package com.arcvideo.arcvideosubhostlauncher.wallpaper.impl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.arcvideo.arcvideosubhostlauncher.bean.ArcView;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.WallPaperImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WPVideoManager implements SurfaceHolder.Callback, WallPaperImpl {
    private static final boolean DEBUG = AppUtil.DEBUG;
    private final String TAG = "WPVideoManager";
    private String pathname = null;
    private Context context;
    private MediaPlayer mediaPlayer;
    private WindowManager.LayoutParams params;
    private SurfaceView surfaceView;
    private int seektime = 0;
    public WPVideoManager(Context context) {
        this.context = context;
        initView();
        initParams();
    }
    private void initView(){
        surfaceView = new SurfaceView(context);
    }

    private void initParams(){
        params = new WindowManager.LayoutParams();
        params.setTitle("background_video");
        int flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = flag;
        params.format = PixelFormat.RGBA_8888; /*透明背景,否则会黑色*/
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
    }

    public void setWallPaperVideo(File file) {
        try {
            this.pathname = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperVideo(Uri uri) {
        try {
            this.pathname = new File(new URI(uri.toString())).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperVideo(String filepath) {
        this.pathname = filepath;
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        mediaPlayer = new MediaPlayer();
        try {
            if (DEBUG) Log.d(TAG, "surfaceCreated: init mediaplay background.");
            mediaPlayer.setDataSource(pathname);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mediaPlayer.prepareAsync();
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                    mediaPlayer.seekTo(seektime);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        seektime = mediaPlayer.getCurrentPosition();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public List<ArcView> getWallPaperView() {
        return new ArrayList<ArcView>(){{
            add(new ArcView(surfaceView, params));
        }};
    }

    @Override
    public void destroy(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        surfaceView.getHolder().removeCallback(this);
    }
}

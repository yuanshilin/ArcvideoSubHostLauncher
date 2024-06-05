package com.arcvideo.arcvideosubhostlauncher.wallpaper.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.arcvideo.arcvideosubhostlauncher.R;
import com.arcvideo.arcvideosubhostlauncher.bean.ArcView;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.WallPaperImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WPCompressManager implements SurfaceHolder.Callback, WallPaperImpl {
    private static final boolean DEBUG = AppUtil.DEBUG;
    private final String TAG = "WPCompressManager";
    private final int UPDATE_BACKGROUND = 100;
    private final int UPDATE_DEFAULT_BACKGROUND = 101;
    private final int UPDATE_ANIMATION = 102;
    private String pathname = null;
    private Context context;
    private ImageView backGround, dynaAnimal;
    private SurfaceView surfaceView;
    private WindowManager.LayoutParams params;
    private Drawable backDrawable, dynaDrawable;
    private Handler handler;
    private Thread backThread, dynaThread;
    private boolean recycle = false;
    private final int latency = 25;
    public WPCompressManager(Context context) {
        this.context = context;
        initView();
        initParams();
        initHandler();
    }

    private void initView(){
        backGround = new ImageView(context);
        dynaAnimal = new ImageView(context);
        surfaceView = new SurfaceView(context);
    }

    private void initParams(){
        params = new WindowManager.LayoutParams();
        params.setTitle("background_Animation");
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

    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case UPDATE_ANIMATION:
                        dynaAnimal.setImageDrawable(dynaDrawable);
                        break;
                    case UPDATE_BACKGROUND:
                        backGround.setImageDrawable(backDrawable);
                        break;
                    case UPDATE_DEFAULT_BACKGROUND:
                        backGround.setImageResource(R.drawable.arcvideo_host);
                        break;
                }
            }
        };
    }

    private void initThread(){
        backThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean find = false;
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(pathname);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.isDirectory()) {
                            continue;
                        } else {
                            if (entry.getName().startsWith("background.")){
                                backDrawable = Drawable.createFromStream(zipFile.getInputStream(entry), null);
                                if (DEBUG) Log.d(TAG, "run: set background wallpaper for "+ entry.getName());
                                find = true;
                                break;
                            }
                        }
                    }
                    if (DEBUG && !find) Log.d(TAG, "run: set background wallpaper to default picture.");
                    handler.sendEmptyMessage(find? UPDATE_BACKGROUND:UPDATE_DEFAULT_BACKGROUND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }finally {
                    try {
                        if (zipFile != null){
                            zipFile.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        dynaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ZipFile zipFile = null;
                while(isRecycle()){
                    try {
                        zipFile = new ZipFile(pathname);
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            if (!isRecycle()) break;
                            ZipEntry entry = entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            } else {
                                // 剔除背景图片
                                if (!entry.getName().startsWith("background.")){
                                    dynaDrawable = Drawable.createFromStream(zipFile.getInputStream(entry), null);
                                    handler.sendEmptyMessage(UPDATE_ANIMATION);
                                    Thread.sleep(latency);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    if (zipFile != null){
                        zipFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setWallPaperAnimation(File file) {
        try {
            this.pathname = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperAnimation(Uri uri) {
        try {
            this.pathname = new File(new URI(uri.toString())).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperAnimation(String filepath) {
        this.pathname = filepath;
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        setRecycle(true);
        initThread();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try{
            backThread.start();
            dynaThread.start();
        } catch (IllegalThreadStateException e){
            Log.d(TAG, "surfaceChanged: thread is already started.");
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        setRecycle(false);
    }

    @Override
    public List<ArcView> getWallPaperView() {
        return new ArrayList<ArcView>(){{
            add(new ArcView(surfaceView, params));
            add(new ArcView(backGround, params));
            add(new ArcView(dynaAnimal, params));
        }};
    }

    @Override
    public void destroy() {
        setRecycle(false);
        surfaceView.getHolder().removeCallback(this);
    }

    public boolean isRecycle() {
        return recycle;
    }

    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }
}

package com.arcvideo.arcvideosubhostlauncher.wallpaper.impl;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.arcvideo.arcvideosubhostlauncher.R;
import com.arcvideo.arcvideosubhostlauncher.bean.ArcView;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.WallPaperImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WPImageManager implements SurfaceHolder.Callback, WallPaperImpl {
    private static final boolean DEBUG = AppUtil.DEBUG;
    private final String TAG = "WPImageManager";
    private Context context;
    private ImageView imageView;
    private String filepath = null;
    private int resourceid = R.drawable.arcvideo_host;
    private SurfaceView surfaceView;
    private Drawable drawableImg;
    private WindowManager.LayoutParams params;

    public WPImageManager(Context context) {
        this.context = context;
        initView();
        initParams();
    }

    private void initView(){
        if (DEBUG) Log.d(TAG, "initView: start to init wall paper image.");
        imageView = new ImageView(context);
        surfaceView = new SurfaceView(context);
    }

    private void initParams(){
        params = new WindowManager.LayoutParams();
        params.setTitle("background_image");
        int flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = flag;
        params.format = PixelFormat.RGBA_8888; /*透明背景,否则会黑色*/
        Point sizePoint = new Point();
        ((Activity)context).getWindow().getWindowManager().getDefaultDisplay().getRealSize(sizePoint);
        params.width = sizePoint.x;
        params.height = sizePoint.y;
//        params.width = WindowManager.LayoutParams.MATCH_PARENT;
//        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        Log.d(TAG, "initParams: params.width is "+params.width+", params.height is "+params.height);
        params.gravity = Gravity.CENTER;
    }

    public void setWallPaperImage(File file) {
        try {
            filepath = file.getCanonicalPath();
            if (DEBUG) Log.d(TAG, "setWallPaperImage: set wall paper image:"+filepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperImage(Uri uri) {
        try {
            filepath = new File(new URI(uri.toString())).getCanonicalPath();
            if (DEBUG) Log.d(TAG, "setWallPaperImage: set wall paper image:"+uri.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperImage(String filepath) {
        if (DEBUG) Log.d(TAG, "setWallPaperImage: set wall paper image:"+filepath);
        this.filepath = filepath;
        surfaceView.getHolder().addCallback(this);
    }

    public void setWallPaperImage(int resourceid) {
        if (DEBUG) Log.d(TAG, "setWallPaperImage: set wall paper image:"+resourceid);
        this.resourceid = resourceid;
        filepath = null;
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        if (filepath != null){
            drawableImg = Drawable.createFromPath(filepath);
        }else {
            drawableImg = ResourcesCompat.getDrawable(context.getResources(), resourceid, null);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        imageView.setImageDrawable(drawableImg);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public List<ArcView> getWallPaperView() {
        return new ArrayList<ArcView>(){{
            add(new ArcView(surfaceView, params));
            add(new ArcView(imageView, params));
        }};
    }

    @Override
    public void destroy() {
        surfaceView.getHolder().removeCallback(this);
    }
}

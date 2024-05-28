package com.arcvideo.arcvideosubhostlauncher.wallpaperimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.arcvideo.arcvideosubhostlauncher.R;

public class WPIManager implements SurfaceHolder.Callback{
    private final String TAG = "WPIManager";
    private Canvas canvas;
    private Context context;
    private int width, height, resourceid;

    public WPIManager(Context context) {
        this.context = context;
    }

    public void setPreference(int width, int height, int resourceid){
        this.width = width;
        this.height = height;
        this.resourceid = resourceid;
    }

    public void showWallPaperImage(SurfaceHolder surfaceHolder) {
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        BitmapFactory.decodeResource(context.getResources(), resourceid, opts);
//				解析图片的头文件
        opts.inJustDecodeBounds = true;
//				得到图片高、宽
        float imageH = opts.outHeight;
        float imageW = opts.outWidth;
        Log.d(TAG, "surfaceCreated: image heigh is "+ imageH+", weight is "+imageW);
        Log.d(TAG, "surfaceCreated: screen heigh is "+ height+", weight is "+width);
        canvas = surfaceHolder.lockCanvas(null);
        PaintFlagsDrawFilter pfd= new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.setDrawFilter(pfd);//解决缩放后图片字体模糊的问题
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceid);

//		设置缩放比
        Matrix matrix = new Matrix();
        Log.d(TAG, "surfaceCreated: width/imageW="+(width / imageW)+", height/imageH="+(height / imageH));
        matrix.setScale(width / imageW, height / imageH);
        canvas.drawBitmap(bitmap, matrix, new Paint());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (canvas != null){
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}

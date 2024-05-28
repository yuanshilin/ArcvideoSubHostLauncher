package com.arcvideo.arcvideosubhostlauncher;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.arcvideo.arcvideosubhostlauncher.apprecycle.AppListManager;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;
import com.arcvideo.arcvideosubhostlauncher.wallpaperimage.WPIManager;

public class MainActivity extends Activity {
    public static final String TAG = "SubHostCarLauncher";
    private static final boolean DEBUG = AppUtil.DEBUG;
    private final int REQUESTSTORAGECODE = 1024;
    private final int REQUESTOVERLAYCODE = 1025;
    private View recyclerView = null;
    WindowManager.LayoutParams params;
    private SurfaceView surfaceView;
    private WPIManager wpiManager;
    private AppListManager appListManager;
    private void setWindowFlag(){
        Window window = getWindow();
        View decorView = window.getDecorView();
        int flag = decorView.getSystemUiVisibility();

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 状态栏隐藏
        flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        // 导航栏隐藏
        flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // 布局延伸到导航栏
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        // 布局延伸到状态栏
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        // 全屏时,增加沉浸式体验
        flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //  部分国产机型适用.不加会导致退出全屏时布局被状态栏遮挡
        // activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // android P 以下的刘海屏,各厂商都有自己的适配方式,具体在manifest.xml中可以看到
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams pa = window.getAttributes();
            pa.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(pa);
        }
        decorView.setSystemUiVisibility(flag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 隐藏导航栏和状态栏
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)){
            setWindowFlag();
        }

        initview();
        checkPermission();
    }

    private void initview(){
        surfaceView = findViewById(R.id.main_background);
    }

    private void initdata(){
        initBackGround();
        initAppList();
    }

    private void initBackGround(){
        wpiManager = new WPIManager(this);
        Point sizePoint = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(sizePoint);
//        wpiManager.setPreference(sizePoint.x, sizePoint.y,
//                (sizePoint.x>1920)?R.drawable.arcvideo_host: R.drawable.arcvideo_host_1920x1080);
        wpiManager.setPreference(sizePoint.x, sizePoint.y, R.drawable.arcvideo_host_1920x1080);
        wpiManager.showWallPaperImage(surfaceView.getHolder());
    }

    private void initAppList(){
        appListManager = new AppListManager(this);
        recyclerView = appListManager.getListView();
        params = new WindowManager.LayoutParams();
        params.setTitle("app_recycle_list");
        int flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = flag;
        params.format = PixelFormat.RGBA_8888; /*透明背景,否则会黑色*/
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindowManager().addView(recyclerView, params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindowManager().removeView(recyclerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appListManager.destroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0){
            if (DEBUG) Log.d(TAG, "onRequestPermissionsResult: permission grant failed.");
            return;
        }
        switch (requestCode){
            case REQUESTSTORAGECODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (DEBUG) {
                        Log.d(TAG, "onRequestPermissionsResult: storage permission is granted. read to check overlay permission.");
                    }
                    if (!Settings.canDrawOverlays(this)){
                        requestOverlaysPermission();
                    }else{
                        Log.d(TAG, "onRequestPermissionsResult: overlay permission is granted. read to init data.");
                        initdata();
                    }
                }else {
                    if (DEBUG) {
                        Log.d(TAG, "onRequestPermissionsResult: storage permission is not granted.");
                    }
                    finish();
                }
                break;
            case REQUESTOVERLAYCODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (DEBUG) {
                        Log.d(TAG, "onRequestPermissionsResult: overlay permission is granted. read to init data.");
                    }
                    initdata();
                }else {
                    if (DEBUG) {
                        Log.d(TAG, "onRequestPermissionsResult: overlay permission is not granted.");
                    }
                    finish();
                }
                break;
        }
    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                requestStoragePermission();
            } else if (!Settings.canDrawOverlays(this)){
                requestOverlaysPermission();
            } else {
                //已经授予了读写外置存储权限，可以直接进行读写文件操作
                initdata();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else if (!Settings.canDrawOverlays(this)){
                requestOverlaysPermission();
            } else {
                initdata();
            }
        } else {
            //已经授予了读写外置存储权限，可以直接进行读写文件操作
            initdata();
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                //已经授予了读写外置存储权限，可以直接进行读写文件操作
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUESTSTORAGECODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //已经授予了读写外置存储权限，可以直接进行读写文件操作
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUESTSTORAGECODE);
            }
        } else {
            //已经授予了读写外置存储权限，可以直接进行读写文件操作
        }
    }
    private void requestOverlaysPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUESTOVERLAYCODE);
    }
}
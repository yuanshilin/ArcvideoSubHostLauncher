package com.arcvideo.arcvideosubhostlauncher;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.arcvideo.arcvideosubhostlauncher.bean.MagicInfo;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.impl.AppListViewManager;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;
import com.arcvideo.arcvideosubhostlauncher.util.MagicUtil;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.impl.WPCompressManager;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.impl.WPImageManager;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.impl.WPVideoManager;

public class MainActivity extends Activity {
    public static final String TAG = "SubHostCarLauncher";
    private static final boolean DEBUG = AppUtil.DEBUG;
    private final int REQUESTSTORAGECODE = 1024;
    private final int REQUESTOVERLAYCODE = 1025;
    private WPVideoManager wpVideoManager = null;
    private WPImageManager wpImageManager = null;
    private WPCompressManager wpCompressManager = null;
    private AppListViewManager appListManager = null;
    private ArcViewManager arcViewManager = null;
    private RefreshReceiver receiver = null;
    private boolean onresume = false;
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

        checkPermission();
        registerRefreshReceiver();
    }
    private void registerRefreshReceiver(){
        receiver = new RefreshReceiver();
        IntentFilter intentFilter = new IntentFilter(AppUtil.UPDATE_WALLPAPER_BROADCAST);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED);
        }else{
            registerReceiver(receiver, intentFilter);
        }
    }

    private void initdata(){
        arcViewManager = new ArcViewManager(this);
        displayViews();
    }

    private void displayViews() {
        initBackGroundSurface();
        initAppList();
    }

    private void initBackGroundSurface(){
        String filepath = AppUtil.Table_preImgPath;
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)){
            filepath = AppUtil.Auto_preWallpaper;
        }
        int type = MagicUtil.getFileType(filepath);
        if (DEBUG) Log.d(TAG, "initBackGroundSurface: file type is "+ MagicInfo.getTypeInfo(type));
        switch (type) {
            case MagicInfo.VIDEO_TYPE:
                wpVideoManager = new WPVideoManager(this);
                wpVideoManager.setWallPaperVideo(filepath);
                arcViewManager.addview(wpVideoManager);
                break;
            case MagicInfo.IMAGE_TYPE:
                wpImageManager = new WPImageManager(this);
                wpImageManager.setWallPaperImage(filepath);
                arcViewManager.addview(wpImageManager);
                break;
            case MagicInfo.COMPRESS_TYPE:
                wpCompressManager = new WPCompressManager(this);
                wpCompressManager.setWallPaperAnimation(filepath);
                arcViewManager.addview(wpCompressManager);
                break;
            default:
                wpImageManager = new WPImageManager(this);
                Point sizePoint = new Point();
                getWindow().getWindowManager().getDefaultDisplay().getRealSize(sizePoint);
                if (sizePoint.x==3048 && sizePoint.y==2032){
                    wpImageManager.setWallPaperImage(R.drawable.arcvideo_host_xiaomi);
                }else{
                    wpImageManager.setWallPaperImage(R.drawable.arcvideo_host);
                }
                arcViewManager.addview(wpImageManager);
        }
    }

    private void initAppList(){
        appListManager = new AppListViewManager(this);
        arcViewManager.addview(appListManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnresume(true);
        arcViewManager.showViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setOnresume(false);
        arcViewManager.hideViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arcViewManager.destroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
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

    private void resetWallPaper(){
        if (isOnresume()){
            arcViewManager.hideViews();
        }
        arcViewManager.destroy();
        displayViews();
        if (isOnresume()) {
            arcViewManager.showViews();
        }
    }

    private class RefreshReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: read to reset wallpaper.");
            resetWallPaper();
        }
    }

    public boolean isOnresume() {
        return onresume;
    }

    public void setOnresume(boolean onresume) {
        this.onresume = onresume;
    }
}
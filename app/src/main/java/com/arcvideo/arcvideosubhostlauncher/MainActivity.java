package com.arcvideo.arcvideosubhostlauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SubHostCarLauncher";
    private static final boolean DEBUG = true;
    private AppRecycleViewAdapter appRecycleViewAdapter;
    private List<String> acrvideo_app;
    private List<AppInfo> appInfos = new ArrayList<>();
    private RecyclerView recyclerView;
    private AppBehaviorReceive appBehaviorReceive = null;
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
        setWindowFlag();

        initview();
        initdata();
        registerAppBehavior();
    }

    private void initview(){
        recyclerView = findViewById(R.id.applist);
    }

    private void initdata(){
        appInfos = loadArcVideoApp();
        appRecycleViewAdapter = new AppRecycleViewAdapter(appInfos);
        appRecycleViewAdapter.setOnItemListener(new AppRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                startActivity(new Intent(appInfos.get(position).getLaunchIntent()));
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(appRecycleViewAdapter);
        appRecycleViewAdapter.notifyDataSetChanged();
    }

    private List<AppInfo> loadArcVideoApp(){
        acrvideo_app = Arrays.asList(getResources().getStringArray(R.array.acrvideo_app));
        if (DEBUG) {
            Log.d(TAG, "initdata: display app list is "+acrvideo_app.toString());
        }
        List<AppInfo> list = new ArrayList<>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent,
                PackageManager.GET_META_DATA);
        for (ResolveInfo app: apps){
            Log.d(TAG, "loadArcVideoApp: package name is "+String.valueOf(app.activityInfo.loadLabel(packageManager)));
            if (app.activityInfo.packageName.equals(getPackageName())){
                continue;
            }
            String label = String.valueOf(app.activityInfo.loadLabel(packageManager));
            if (!acrvideo_app.contains(label)){
                continue;
            }
            list.add(new AppInfo(app, packageManager));
        }
        return list;
    }

    private void registerAppBehavior(){
        appBehaviorReceive = new AppBehaviorReceive();
        IntentFilter intentFilter = new IntentFilter();
        // 安卓8.0之后必须动态注册监听系统卸载应用广播才能生效
        intentFilter.addDataScheme("package");
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);//安装app
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);//卸载app
        this.registerReceiver(appBehaviorReceive, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appBehaviorReceive != null) {
            unregisterReceiver(appBehaviorReceive);
        }
    }

    private class AppBehaviorReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            String action = intent.getAction();
            if (DEBUG) Log.d(TAG, "onReceive: boardcast action is "+action+",package name is "+packageName);
            switch(action){
                case Intent.ACTION_PACKAGE_REMOVED:
                    for (AppInfo app: appInfos) {
                        if(app.getLaunchIntent().getComponent().getPackageName().equals(packageName)){
                            if (DEBUG) Log.d(TAG, "onReceive: ready to remove "+packageName+" from list.");
                            appInfos.remove(app);
                            appRecycleViewAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    break;
                case Intent.ACTION_PACKAGE_ADDED:
                    List<AppInfo> templist = loadArcVideoApp();
                    if (templist.size() != appInfos.size()) {
                        if (DEBUG) Log.d(TAG, "onReceive: ready to add "+packageName+" to list.");
                        appInfos.clear();
                        appInfos.addAll(templist);
                        appRecycleViewAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
    }
}
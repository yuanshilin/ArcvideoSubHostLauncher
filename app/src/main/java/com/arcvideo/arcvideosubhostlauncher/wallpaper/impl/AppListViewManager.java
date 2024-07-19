package com.arcvideo.arcvideosubhostlauncher.wallpaper.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcvideo.arcvideosubhostlauncher.R;
import com.arcvideo.arcvideosubhostlauncher.adapter.AppRecycleViewAdapter;
import com.arcvideo.arcvideosubhostlauncher.bean.AppInfo;
import com.arcvideo.arcvideosubhostlauncher.bean.ArcView;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;
import com.arcvideo.arcvideosubhostlauncher.util.ArcIconConfig;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.WallPaperImpl;

import java.util.ArrayList;
import java.util.List;

public class AppListViewManager implements WallPaperImpl {
    private final boolean DEBUG = AppUtil.DEBUG;
    private final String TAG = "AppListView";
    private Context mContext;
    private List<AppInfo> appInfos = new ArrayList<>();
    private View view = null;
    private WindowManager.LayoutParams params = null;
    private List<String> arcvideo_app;
    private RecyclerView recyclerView;
    private AppRecycleViewAdapter appRecycleViewAdapter;
    private AppBehaviorReceive appBehaviorReceive = null;
    private ArcIconConfig arcIconConfig;

    public AppListViewManager(Context mContext) {
        this.mContext = mContext;
        initListView();
        initViewParams();
        registerAppBehavior();

    }

    private void initListView(){
        view = LayoutInflater.from(mContext).inflate(R.layout.app_recycle_list, null);
        recyclerView = view.findViewById(R.id.applist);
        arcIconConfig = new ArcIconConfig(mContext);
        appInfos = loadArcVideoApp();
        appRecycleViewAdapter = new AppRecycleViewAdapter(appInfos);
        appRecycleViewAdapter.setOnItemListener(new AppRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(appInfos.get(position).getLaunchIntent());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(appRecycleViewAdapter);
        appRecycleViewAdapter.notifyDataSetChanged();
    }

    private void initViewParams(){
        params = new WindowManager.LayoutParams();
        params.setTitle("app_recycle_list");
        int flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = flag;
        params.format = PixelFormat.RGBA_8888; /*透明背景,否则会黑色*/
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
    }

    private List<AppInfo> loadArcVideoApp() {
        arcvideo_app = arcIconConfig.getPkglist();
        if (DEBUG) {
            Log.d(TAG, "initdata: display app list is "+arcvideo_app.toString());
        }
        List<AppInfo> list = new ArrayList<>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent,
                PackageManager.GET_META_DATA);
        for (ResolveInfo app: apps){
            if (DEBUG){
                Log.d(TAG, "loadArcVideoApp: package name is "+app.activityInfo.packageName+", label is "+String.valueOf(app.activityInfo.loadLabel(packageManager)));
            }
            if (arcvideo_app.contains(app.activityInfo.packageName)){
                list.add(new AppInfo(app, packageManager));
            }
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
        mContext.registerReceiver(appBehaviorReceive, intentFilter);
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
    public List<ArcView> getWallPaperView() {
        return new ArrayList<ArcView>(){{
            add(new ArcView(view, params));
        }};
    }

    @Override
    public void destroy() {
        if (appBehaviorReceive != null) {
            mContext.unregisterReceiver(appBehaviorReceive);
        }
    }
}

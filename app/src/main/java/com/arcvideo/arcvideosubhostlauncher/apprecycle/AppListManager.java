package com.arcvideo.arcvideosubhostlauncher.apprecycle;

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
import com.arcvideo.arcvideosubhostlauncher.bean.AppInfo;
import com.arcvideo.arcvideosubhostlauncher.util.AppUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppListManager {
    private final boolean DEBUG = AppUtil.DEBUG;
    private final String TAG = "AppListView";
    private Context mContext;
    private List<AppInfo> appInfos = new ArrayList<>();
    private List<String> acrvideo_app;
    private RecyclerView recyclerView;
    private AppRecycleViewAdapter appRecycleViewAdapter;
    private AppBehaviorReceive appBehaviorReceive = null;

    public AppListManager(Context mContext) {
        this.mContext = mContext;
        registerAppBehavior();
    }

    public View getListView(){
        View listview = LayoutInflater.from(mContext).inflate(R.layout.app_recycle_list, null);
        recyclerView = listview.findViewById(R.id.applist);
        appInfos = loadArcVideoApp();
        appRecycleViewAdapter = new AppRecycleViewAdapter(appInfos);
        appRecycleViewAdapter.setOnItemListener(new AppRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                mContext.startActivity(new Intent(appInfos.get(position).getLaunchIntent()));
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(appRecycleViewAdapter);
        appRecycleViewAdapter.notifyDataSetChanged();

        return listview;
    }

    public void destroy(){
        if (appBehaviorReceive != null) {
            mContext.unregisterReceiver(appBehaviorReceive);
        }
    }

    private List<AppInfo> loadArcVideoApp(){
        acrvideo_app = Arrays.asList(mContext.getResources().getStringArray(R.array.acrvideo_app));
        if (DEBUG) {
            Log.d(TAG, "initdata: display app list is "+acrvideo_app.toString());
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
            if (app.activityInfo.packageName.equals(mContext.getPackageName())){
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
}

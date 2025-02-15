package com.arcvideo.arcvideosubhostlauncher.bean;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable mIcon;
    private String mLable;
    private Intent mLaunchIntent;
    public AppInfo(ResolveInfo info, PackageManager packageManager) {
        mIcon = info.loadIcon(packageManager);
        mLable = String.valueOf(info.loadLabel(packageManager));
        mLaunchIntent = new Intent();
        mLaunchIntent.setComponent(new ComponentName(info.activityInfo.packageName,
                info.activityInfo.name));
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public String getmLable() {
        return mLable;
    }

    public Intent getLaunchIntent() {
        return mLaunchIntent;
    }

    public ComponentName getComponentName() {
        return mLaunchIntent.getComponent();
    }
}

package com.arcvideo.arcvideosubhostlauncher.wallpaper;

import android.view.View;
import android.view.WindowManager;

import com.arcvideo.arcvideosubhostlauncher.bean.ArcView;

import java.util.List;

public interface WallPaperImpl {
    public abstract List<ArcView> getWallPaperView();
    public abstract void destroy();
}

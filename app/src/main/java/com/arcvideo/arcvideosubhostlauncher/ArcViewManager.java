package com.arcvideo.arcvideosubhostlauncher;

import android.app.Activity;
import android.view.WindowManager;

import com.arcvideo.arcvideosubhostlauncher.bean.ArcView;
import com.arcvideo.arcvideosubhostlauncher.wallpaper.WallPaperImpl;

import java.util.ArrayList;
import java.util.List;

public class ArcViewManager {
    private WindowManager windowManager = null;
    private List<ArcView> arcViews = new ArrayList<>();
    private List<WallPaperImpl> wpImpls = new ArrayList<>();

    public ArcViewManager(Activity activity) {
        windowManager = activity.getWindowManager();
    }
     public void addview(WallPaperImpl wpi){
        wpImpls.add(wpi);
        for (ArcView view: wpi.getWallPaperView()){
            arcViews.add(view);
        }
     }

     public void showViews(){
        for (ArcView arcView:arcViews){
            windowManager.addView(arcView.getView(), arcView.getViewparams());
        }
     }

     public void hideViews(){
         for (ArcView arcView:arcViews){
             windowManager.removeView(arcView.getView());
         }
     }

     public void destroy(){
         for (WallPaperImpl impl: wpImpls){
             impl.destroy();
         }
         wpImpls.clear();
         arcViews.clear();
     }
}

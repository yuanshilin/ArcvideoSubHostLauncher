package com.arcvideo.arcvideosubhostlauncher.bean;

import android.view.View;
import android.view.WindowManager;

public class ArcView {
    private View view;
    private WindowManager.LayoutParams viewparams;

    public ArcView(View view, WindowManager.LayoutParams viewparams) {
        this.view = view;
        this.viewparams = viewparams;
    }

    public View getView() {
        return view;
    }

    public WindowManager.LayoutParams getViewparams() {
        return viewparams;
    }
}

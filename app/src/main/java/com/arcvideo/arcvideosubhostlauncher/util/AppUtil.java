package com.arcvideo.arcvideosubhostlauncher.util;

import android.os.Environment;

import java.io.File;

public class AppUtil {
    public static final boolean DEBUG = true;
    public static String Auto_Main_ScreenIcon = "/oem/launcher/MainScreenIcon.xml";
    public static String Auto_Aux_ScreenIcon = "/oem/launcher/AuxScreenIcon.xml";
    public static String Table_ScreenIcon = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            + File.separator+"MainScreenIcon.xml";
    public static String Auto_preWallpaper = "/oem/launcher/wallpaper/wallpaper";
    public static String Table_preImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            + File.separator+"wallpaper";
    public static String UPDATE_WALLPAPER_BROADCAST = "com.arcvideo.receiver.wallpaper";
}

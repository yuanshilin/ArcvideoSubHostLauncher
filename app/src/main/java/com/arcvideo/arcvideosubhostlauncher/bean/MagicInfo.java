package com.arcvideo.arcvideosubhostlauncher.bean;

public class MagicInfo {
    public static final int UNKNOW_TYPE = 100;
    public static final int VIDEO_TYPE = 101;
    public static final int IMAGE_TYPE = 102;
    private String format;
    private int type;

    public MagicInfo(String format, int type) {
        this.format = format;
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public int getType() {
        return type;
    }

    public static String getTypeInfo (int type){
        switch (type){
            case VIDEO_TYPE:
                return "VIDEO_TYPE";
            case IMAGE_TYPE:
                return "IMAGE_TYPE";
            default:
                return "UNKNOW_TYPE";
        }
    }
}

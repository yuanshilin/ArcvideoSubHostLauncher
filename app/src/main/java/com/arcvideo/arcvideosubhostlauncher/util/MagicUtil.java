package com.arcvideo.arcvideosubhostlauncher.util;

import android.util.Log;

import com.arcvideo.arcvideosubhostlauncher.bean.MagicInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MagicUtil {
    private static final boolean DEBUG = AppUtil.DEBUG;
    private static final String TAG = "SubHostCarLauncher_MagicUtil";
    private static Map<String, MagicInfo> fileTypeMap = null;
    public static int getFileType(String filepath){
        return matchFileType(getMagicNumberFromPath(filepath));
    }

    public static int getFileType(File file){
        return matchFileType(getMagicNumberFromFile(file));
    }

    private static String getMagicNumberFromPath(String filepath){
        return getMagicNumber(new File(filepath));
    }

    private static String getMagicNumberFromFile(File file){
        return getMagicNumber(file);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static String getMagicNumber(File file){
        if (!file.exists()) return "null";
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
            byte[] magicNumber = new byte[5];
            randomAccessFile.read(magicNumber);
            randomAccessFile.close();
            return bytesToHexString(magicNumber);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null";
    }

    private static int matchFileType(String magicnumber){
        if (fileTypeMap == null) {
            mapFileType();
        }
        if (DEBUG) Log.d(TAG, "matchFileType: magic number is "+ magicnumber);
        Iterator<Map.Entry<String, MagicInfo>> iterator = fileTypeMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, MagicInfo> entry = iterator.next();
            String key = entry.getKey();
            if (key.startsWith(magicnumber.toLowerCase())){
                return entry.getValue().getType();
            }
        }
        return MagicInfo.UNKNOW_TYPE;
    }

    /**
     * 常用文件格式
     */
    private static void mapFileType(){
        fileTypeMap = new HashMap<>();
        fileTypeMap.put("ffd8ffe000104a464946", new MagicInfo("jpg", MagicInfo.IMAGE_TYPE)); //JPEG (jpg)
        fileTypeMap.put("89504e470d0a1a0a0000", new MagicInfo("png", MagicInfo.IMAGE_TYPE)); //PNG (png)
        fileTypeMap.put("47494638396126026f01", new MagicInfo("gif", MagicInfo.IMAGE_TYPE)); //GIF (gif)
        fileTypeMap.put("49492a00227105008037", new MagicInfo("tif", MagicInfo.IMAGE_TYPE)); //TIFF (tif)
        fileTypeMap.put("424d228c010000000000", new MagicInfo("bmp", MagicInfo.IMAGE_TYPE)); //16色位图(bmp)
        fileTypeMap.put("424d8240090000000000", new MagicInfo("bmp", MagicInfo.IMAGE_TYPE)); //24位位图(bmp)
        fileTypeMap.put("424d8e1b030000000000", new MagicInfo("bmp", MagicInfo.IMAGE_TYPE)); //256色位图(bmp)
        fileTypeMap.put("2e524d46000000120001", new MagicInfo("rmvb", MagicInfo.VIDEO_TYPE)); // rmvb/rm相同
        fileTypeMap.put("464c5601050000000900", new MagicInfo("flv", MagicInfo.VIDEO_TYPE)); // flv与f4v相同
        fileTypeMap.put("0000001c667479706d70", new MagicInfo("mp4", MagicInfo.VIDEO_TYPE)); // mp4 格式一
        fileTypeMap.put("00000020667479706d70", new MagicInfo("mp4", MagicInfo.VIDEO_TYPE)); // mp4 格式二
        fileTypeMap.put("000001ba210001000180", new MagicInfo("mpg", MagicInfo.VIDEO_TYPE)); // mpg
        fileTypeMap.put("3026b2758e66cf11a6d9", new MagicInfo("wmv", MagicInfo.VIDEO_TYPE)); // wmv与asf相同
        fileTypeMap.put("52494646e27807005741", new MagicInfo("wav", MagicInfo.VIDEO_TYPE)); // Wave (wav)
        fileTypeMap.put("52494646d07d60074156", new MagicInfo("avi", MagicInfo.VIDEO_TYPE)); // avi
        fileTypeMap.put("504b0304140000000800", new MagicInfo("zip", MagicInfo.COMPRESS_TYPE)); // zip 标准压缩文件
        fileTypeMap.put("526172211a0700cf9073", new MagicInfo("rar", MagicInfo.COMPRESS_TYPE)); // rar 文件或 zip 存储压缩文件
        fileTypeMap.put("504b03040a0000000000", new MagicInfo("jar", MagicInfo.COMPRESS_TYPE)); //jar 压缩
//        fileTypeMap.put("1f8b0800000000000000", new MagicInfo("gz", MagicInfo.COMPRESS_TYPE));//gz文件
        fileTypeMap.put("null", new MagicInfo("null", MagicInfo.UNKNOW_TYPE)); // null
    }
}

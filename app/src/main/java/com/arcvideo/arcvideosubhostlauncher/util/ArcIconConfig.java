package com.arcvideo.arcvideosubhostlauncher.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ArcIconConfig {
    private String TAG= "ArcIconConfig";
    private File file;
    private List<String> pkglist;
    private final String parseTag = "packagename";
    private Context context;
    private int userid;
    public ArcIconConfig(Context context){
        this.context = context;
        initdata();
    }

    private void initdata(){
        initConfig();
        pkglist = new ArrayList<String>();
        Log.d(TAG, "start to parse "+file.getName());
        if(file.exists()){
            try {
                FileInputStream fs = new FileInputStream(file);
                pull2xml(fs);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "the file:"+file.getName()+" is not exist, parse is failed!");
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, file.getName()+" parse is failed!");
                e.printStackTrace();
            }
        }else{
            Log.d(TAG, "the file:"+file.getName()+" is not exist");
        }
    }

    private void initConfig(){
        try {
            final Method method = UserHandle.class.getDeclaredMethod("myUserId");
            method.setAccessible(true);
            final Object object = method.invoke(null);
            if (object instanceof Integer) {
                userid = (int) object;
                TAG = TAG+"_"+userid;
            }
        } catch (Exception ignored) {
            userid = -1;
        }
        Log.d(TAG, "initConfig: userid is "+userid);
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)){
            file = new File((userid==10)? AppUtil.Auto_Main_ScreenIcon : AppUtil.Auto_Aux_ScreenIcon);
        }else{
            file = new File(AppUtil.Table_ScreenIcon);
        }
    }

    private void pull2xml(InputStream is) throws Exception {
        //创建xmlPull解析器
        XmlPullParser parser = Xml.newPullParser();
        ///初始化xmlPull解析器
        parser.setInput(is, "utf-8");
        //读取文件的类型
        int type = parser.getEventType();
        String result = null;
        //无限判断文件类型进行读取
        while (type != XmlPullParser.END_DOCUMENT) {
            switch (type) {
                //开始标签
                case XmlPullParser.START_TAG:
                    if (parseTag.equals(parser.getName())) {
                        result = parser.nextText().trim();
                        pkglist.add(result);
                        result = null;
                    }
                    break;
                //结束标签
                case XmlPullParser.END_TAG:
                    break;
            }
            //继续往下读取标签类型
            type = parser.next();
        }
    }

    public List<String> getPkglist() {
        return pkglist;
    }
}

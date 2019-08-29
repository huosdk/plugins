package io.flutter.plugins.deviceinfo.utils;

import android.content.Context;

public class ApplicationHolder {
    private static Context application;
    public static void init(Context context){
        application=context;
    }
    public static Context getApplication(){
        return application;
    }
}

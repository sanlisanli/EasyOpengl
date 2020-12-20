package me.wawa.opengl;

import android.app.Application;

import com.yinge.opengl.camera.util.OpenGlCameraSdk;


public class OpenGlApplication extends Application {

    private static OpenGlApplication app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        OpenGlCameraSdk.getInstance().init(this);
    }

    public static OpenGlApplication getInstance() {
        return app;
    }


}

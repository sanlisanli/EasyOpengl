package me.wawa.opengl.image;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.yinge.opengl.image.filter.AbsImageFilter;

import java.io.IOException;


public class GLImageView extends GLSurfaceView {

    private GLImageRender mRender;

    public GLImageView(Context context) {
        super(context);
        init();
    }

    public GLImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
        setEGLContextClientVersion(2);
        mRender = new GLImageRender(getContext());
        setRenderer(mRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);


        try {
            mRender.setBitmap(BitmapFactory.decodeStream(getResources().getAssets().open("texture/fengj.png")));
            requestRender();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GLImageRender getRender() {
        return mRender;
    }

    /**
     * 设置过滤器
     * @param filter
     */
    public void setFilter(AbsImageFilter filter) {
        mRender.setImageFilter(filter);

    }


}

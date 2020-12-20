package me.wawa.opengl.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;


public class GLShapeView extends GLSurfaceView {

    private GLShapeRender shapeRender;

    public GLShapeView(Context context) {
        super(context);
        init();
    }

    public GLShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(shapeRender = new GLShapeRender(this));
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * 设置形状
     * @param clazz
     */
    public void setShape(Class<? extends Shape> clazz) {

        try {
            shapeRender.setShape(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}

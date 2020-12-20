package me.wawa.opengl.transform;

import android.opengl.GLSurfaceView;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yinge.opengl.R;


/**
 * 矩阵变化 demo
 */

public class TransformActivity extends AppCompatActivity {

    private GLSurfaceView mGLView;
    private TransformRender render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tranform);
        initGL();
    }

    public void initGL(){
        mGLView= (GLSurfaceView) findViewById(R.id.mGLView);
        mGLView.setEGLContextClientVersion(2);
        render = new TransformRender(this);
        mGLView.setRenderer(render);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

}

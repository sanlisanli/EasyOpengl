package me.wawa.opengl.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.View;


public abstract class Shape implements GLSurfaceView.Renderer {

    protected View mView;

    public Shape(View view) {
        this.mView = view;
    }

    /**
     * 加载，编译着色器
     * @param type       {@link GLES20#GL_VERTEX_SHADER,GLES20#GL_FRAGMENT_SHADER}
     * @param shaderCode 着色器源代码
     * @return
     */
    public int loadShader(int type, String shaderCode) {
        // 根据type创建顶点着色器或者片元着色器
        int shader = GLES20.glCreateShader(type);
        // 将着色器代码加入着色器
        GLES20.glShaderSource(shader, shaderCode);
        // 编译着色器
        GLES20.glCompileShader(shader);
        // 获取着色器的编译状态
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        return shader;

    }



}

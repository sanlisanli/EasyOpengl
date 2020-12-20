package me.wawa.opengl.render;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.View;

import com.yinge.opengl.util.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class Oval extends Shape {


    // 每个坐标是三维
    private static final int COORDS_PER_VERTEX = 3;

    private FloatBuffer vertexBuffer;

    // 主程序
    private int mProgram;
    // 顶点着色器 位置句柄
    private int mPositionHandle;
    // 片元着色器 颜色句柄
    private int mColorHandle;
    // 顶点之间的偏移量
    private final int vertexStride = 0;// COORDS_PER_VERTEX * 4

    // 设置颜色，依次为红绿蓝和透明通道 此处是白色
    private float color[] = {1.0f, 1.0f, 1.0f, 1.0f};


    // 矩阵相关
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    // 矩阵的句柄
    private int mMatrixHandle;

    // 半径
    private float radius = 1.0f;
    // 切割份数
    private int n = 360;
    // 所有三角形的顶点
    private float[] shapePositions;

    private float height = 0.0f;


    public Oval(View view) {
        this(view, 0.0f);
    }

    public Oval(View view,float height) {
        super(view);
        this.height=height;
        // 计算顶点坐标
        shapePositions = createPositions();


        // 申请底层空间，一个float占4个字节
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(shapePositions.length * 4);
        // 设置字节顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        // 转换为float型缓冲池
        vertexBuffer = byteBuffer.asFloatBuffer();
        // 向缓冲区中放入顶点坐标数据
        vertexBuffer.put(shapePositions);
        //设置缓冲区起始位置
        vertexBuffer.position(0);


        // 加载并编译 顶点着色器
        String vertexShaderCode1 = OpenGlUtils.loadShaderSrcFromAssetFile(mView.getResources(), "shape/vshader" +
                "/vOval.glsl");
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode1);

        // 加载并编译 片元着色器
        String fragmentShaderCode1 = OpenGlUtils.loadShaderSrcFromAssetFile(mView.getResources(), "shape/fshader" +
                "/fOval.glsl");
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode1);

        // 创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        // 将片元着色器加入到程序
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }

    /**
     * @return
     */
    private float[]  createPositions(){
        ArrayList<Float> data=new ArrayList<>();
        //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        data.add(height);
        // 每一份的角度
        float angDegSpan = 360f/n;
        for(float i=0 ; i< 360+angDegSpan ; i+=angDegSpan){

            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(height);
        }
        float[] f = new float[data.size()];
        for (int i=0 ; i< f.length ; i++){
            f[i] = data.get(i);
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        float ratio =(float) width / height;
        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {


        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        //获取变换矩阵vMatrix成员句柄
        mMatrixHandle= GLES20.glGetUniformLocation(mProgram,"vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle,1,false,mMVPMatrix,0);

        // 获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // 获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color,0);

        // 绘制三角形
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 扇形绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, shapePositions.length / 3);

        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }
}

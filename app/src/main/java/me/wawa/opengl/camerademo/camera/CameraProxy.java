package me.wawa.opengl.camerademo.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraProxy implements Camera.AutoFocusCallback {

    public static final String TAG = "CameraProxy";

    private Activity mActivity;
    private Camera mCamera;
    private Parameters mParameters;
    private CameraInfo mCameraInfo;
    private int mCameraId;
    private int mPreviewWidth = 1440; // default 1440
    private int mPreviewHeight = 1080; // default 1080
    private float mPreviewScale = mPreviewHeight * 1f / mPreviewWidth;
    // 相机预览的数据回调
    private PreviewCallback mPreviewCallback;
    // 方向监听器
    private OrientationEventListener mOrientationEventListener;
    //
    private int mLatestRotation = 0;
    // 预览帧缓存
    private byte[] mPreviewBuffer;

    public CameraProxy(Activity activity) {
        mActivity = activity;
        mCameraInfo = new CameraInfo();
        mCameraId = CameraInfo.CAMERA_FACING_BACK;

        mOrientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d(TAG, "onOrientationChanged：orientation = " + orientation);
                setPictureRotate(orientation);
            }
        };
    }

    /**
     * 打开相机
     */
    public void openCamera() {
        Log.d(TAG, "openCamera cameraId: " + mCameraId);
        mCamera = Camera.open(mCameraId);
        Camera.getCameraInfo(mCameraId, mCameraInfo);
        initConfig();
        setDisplayOrientation();
        Log.d(TAG, "openCamera enable mOrientationEventListener");
        mOrientationEventListener.enable();
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (mCamera != null) {
            Log.v(TAG, "releaseCamera");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        Log.d(TAG, "openCamera disable mOrientationEventListener");
        mOrientationEventListener.disable();
    }

    /**
     * 开始预览
     * @param holder
     */
    public void startPreview(SurfaceHolder holder) {
        if (mCamera != null) {
            Log.v(TAG, "startPreview");
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    }

    /**
     * 开始预览
     * @param surface
     */
    public void startPreview(SurfaceTexture surface) {
        if (mCamera != null) {
            Log.v(TAG, "startPreview");
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            Log.v(TAG, "stopPreview");
            mCamera.stopPreview();
        }
    }

    /**
     * @return 是否前置摄像头
     */
    public boolean isFrontCamera() {
        return mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * 初始化相机参数
     */
    private void initConfig() {
        Log.v(TAG, "initConfig");
        try {
            mParameters = mCamera.getParameters();
            // 如果摄像头不支持这些参数都会出错的，所以设置的时候一定要判断是否支持

            // 设置闪光模式
            List<String> supportedFlashModes = mParameters.getSupportedFlashModes();
            if (supportedFlashModes != null && supportedFlashModes.contains(Parameters.FLASH_MODE_OFF)) {
                mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            }
            // 设置聚焦模式
            List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
                mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            }
            // 设置预览图片格式
            mParameters.setPreviewFormat(ImageFormat.NV21);
            // 设置拍照图片格式
            mParameters.setPictureFormat(ImageFormat.JPEG);
            // 设置曝光强度
            mParameters.setExposureCompensation(0);
            // 设置预览图片大小
            Size previewSize = getSuitableSize(mParameters.getSupportedPreviewSizes(), "preview");
            mPreviewWidth = previewSize.width;
            mPreviewHeight = previewSize.height;
            mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            Log.d(TAG, "previewWidth: " + mPreviewWidth + ", previewHeight: " + mPreviewHeight);

            // 设置拍照图片大小
            Size pictureSize = getSuitableSize(mParameters.getSupportedPictureSizes(), "picture");
            mParameters.setPictureSize(pictureSize.width, pictureSize.height);
            Log.d(TAG, "pictureWidth: " + pictureSize.width + ", pictureHeight: " + pictureSize.height);

            // 将设置好的parameters添加到相机里
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param sizes
     * @param type
     * @return
     */
    private Size getSuitableSize(List<Size> sizes, String type) {
        int minDelta = Integer.MAX_VALUE; // 最小的差值，初始值应该设置大点保证之后的计算中会被重置
        int index = 0; // 最小的差值对应的索引坐标
        for (int i = 0; i < sizes.size(); i++) {
            Size size = sizes.get(i);
            Log.d(TAG, " type = " + type + " SupportedSize, width: " + size.width + ", height: " + size.height);
            // 先判断比例是否相等
            if (size.width * mPreviewScale == size.height) {
                int delta = Math.abs(mPreviewWidth - size.width);
                if (delta == 0) {
                    return size;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes.get(index);
    }

    /**
     * 设置相机显示的方向，必须设置，否则显示的图像方向会错误
     */
    private void setDisplayOrientation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Log.d(TAG, "setDisplayOrientation: rotation = " + rotation + " degrees = " + degrees );

        int result;
        if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror

            Log.d(TAG, "setDisplayOrientation: front - mCameraInfo.orientation = "
                    + mCameraInfo.orientation
                    + " result = " + result );

        } else {  // back-facing
            result = (mCameraInfo.orientation - degrees + 360) % 360;


            Log.d(TAG, "setDisplayOrientation: back - mCameraInfo.orientation = "
                    + mCameraInfo.orientation
                    + " result = " + result );
        }
        mCamera.setDisplayOrientation(result);
    }

    /**
     * 设置拍照的图片的旋转角度
     * @param orientation
     */
    private void setPictureRotate(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;

        orientation = (orientation + 45) / 90 * 90;
        int rotation;
        if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (mCameraInfo.orientation - orientation + 360) % 360;

            Log.d(TAG, "setPictureRotate：front" +
                    " mCameraInfo.orientation  = " + mCameraInfo.orientation +
                    " rotation = " + rotation);

        } else {  // back-facing camera
            rotation = (mCameraInfo.orientation + orientation) % 360;

            Log.d(TAG, "setPictureRotate：back" +
                    " mCameraInfo.orientation  = " + mCameraInfo.orientation +
                    " rotation = " + rotation);
        }
        mLatestRotation = rotation;
//         mCamera.getParameters().setRotation(rotation);
    }

    public int getLatestRotation() {
        return mLatestRotation;
    }

    /**
     * 设置预览帧回调
     * @param previewCallback
     */
    public void setPreviewCallback(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
        if (mPreviewBuffer == null) {
            mPreviewBuffer = new byte[mPreviewWidth * mPreviewHeight * 3 / 2];
        }
        mCamera.addCallbackBuffer(mPreviewBuffer);
        mCamera.setPreviewCallbackWithBuffer(mPreviewCallback); // 设置预览的回调
    }

    /**
     * 拍照
     * @param pictureCallback
     */
    public void takePicture(Camera.PictureCallback pictureCallback) {
        mCamera.takePicture(null, null, pictureCallback);
    }

    /**
     * 切换是摄像头
     */
    public void switchCamera() {
        mCameraId ^= 1; // 先改变摄像头朝向
        releaseCamera();
        openCamera();
    }

    public void focusOnPoint(int x, int y, int width, int height) {
        Log.v(TAG, "touch point (" + x + ", " + y + ")");
        if (mCamera == null) {
            return;
        }
        Parameters parameters = mCamera.getParameters();
        // 1.先要判断是否支持设置聚焦区域
        if (parameters.getMaxNumFocusAreas() > 0) {
            // 2.以触摸点为中心点，view窄边的1/4为聚焦区域的默认边长
            int length = Math.min(width, height) >> 3; // 1/8的长度
            int left = x - length;
            int top = y - length;
            int right = x + length;
            int bottom = y + length;
            // 3.映射，因为相机聚焦的区域是一个(-1000,-1000)到(1000,1000)的坐标区域
            left = left * 2000 / width - 1000;
            top = top * 2000 / height - 1000;
            right = right * 2000 / width - 1000;
            bottom = bottom * 2000 / height - 1000;
            // 4.判断上述矩形区域是否超过边界，若超过则设置为临界值
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            Log.d(TAG, "focus area (" + left + ", " + top + ", " + right + ", " + bottom + ")");
            ArrayList<Camera.Area> areas = new ArrayList<>();
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 600));
            parameters.setFocusAreas(areas);
        }
        try {
            mCamera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            mCamera.setParameters(parameters);
            mCamera.autoFocus(this); // 调用聚焦
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleZoom(boolean isZoomIn) {
        if (mParameters.isZoomSupported()) {
            int maxZoom = mParameters.getMaxZoom();
            int zoom = mParameters.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            Log.d(TAG, "handleZoom: zoom: " + zoom);
            mParameters.setZoom(zoom);
            mCamera.setParameters(mParameters);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.d(TAG, "onAutoFocus: " + success);
    }

}

package me.wawa.opengl.camera.filter;

import android.content.Context;

public class GrayFilter extends AbsOesImageFilter {


    public GrayFilter(Context context) {
        super(context);
    }

    @Override
    public String getVertexResPath() {
        return "camera/base_vertex.glsl";
    }

    @Override
    public String getFragmentResPath() {
        return "camera/gray_fragment.glsl";
    }

    @Override
    public void initOtherHandle() {

    }

    @Override
    public void onSizeChanged(int width, int height) {

    }

    @Override
    public void setOtherHandle() {

    }
}

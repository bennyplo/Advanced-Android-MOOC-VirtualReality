package com.bennyplo.virtualreality;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class MyView extends GLSurfaceView {
    private final MyRenderer mRenderer;
    private int ptcount=0;
    private float m1TouchEventX,m1TouchEventY;
    private float m2TouchEventX,m2TouchEventY;
    private float mTouchDistance;//distance between the 2 finger touches
    private float mViewScaledTouchSlop;//number of pixels that a finger is allowed to move
    public MyView(Context context) {
        super(context);
        setEGLContextClientVersion(2);// Create an OpenGL ES 2.0 context.
        mRenderer = new MyRenderer();// Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private final float TOUCH_ZOOM_FACTOR=1.0f/320;
    private float mPreviousX;
    private float mPreviousY;
    public float distance(MotionEvent e, int first, int second) {
        if (e.getPointerCount() >= 2) {
            final float x = e.getX(first) - e.getX(second);
            final float y = e.getY(first) - e.getY(second);

            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }
    private boolean isPinchGesture(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            final float distanceCurrent = distance(event, 0, 1);
            final float diffPrimX = m1TouchEventX - event.getX(0);
            final float diffPrimY = m1TouchEventY - event.getY(0);
            final float diffSecX = m2TouchEventX - event.getX(1);
            final float diffSecY = m2TouchEventY - event.getY(1);

            if (// if the distance between the two fingers has increased past
                // our threshold
                    Math.abs(distanceCurrent - mTouchDistance) > mViewScaledTouchSlop
                            // and the fingers are moving in opposing directions
                            && (diffPrimY * diffSecY) <= 0
                            && (diffPrimX * diffSecX) <= 0) {
                return true;
            }
        }

        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent e){
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_POINTER_UP: case MotionEvent.ACTION_UP:
                ptcount--;
                if (ptcount<-2)
                {
                    m2TouchEventX=-1;
                    m2TouchEventY=-1;
                }
                if (ptcount<-1)
                {
                    m1TouchEventX=-1;
                    m1TouchEventY=-1;
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:case MotionEvent.ACTION_DOWN:
                ptcount++;
                if (ptcount==1)//1 finger
                {
                    m1TouchEventX=e.getX(0);
                    m1TouchEventY=e.getY(0);
                }
                else if (ptcount==2)
                {
                    m2TouchEventX=e.getX(0);
                    m2TouchEventY=e.getY(0);
                    Log.d("Touch", "Down2");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                m2TouchEventX=e.getX(0);
                m2TouchEventY=e.getY(0);
                if (isPinchGesture(e)) {
                    m2TouchEventX=e.getX(0);
                    m2TouchEventY=e.getY(0);
                    mTouchDistance=distance(e,0,1);

                    mRenderer.setZoom(mTouchDistance* TOUCH_ZOOM_FACTOR);  // = 180.0f / 320
                    requestRender();
                }
                else {
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;

                    // reverse direction of rotation above the mid-line
                    if (y > getHeight() / 2) {
                        dx = dx * -1;
                    }

                    // reverse direction of rotation to left of the mid-line
                    if (x < getWidth() / 2) {
                        dy = dy * -1;
                    }

                    mRenderer.setAngle(
                            mRenderer.getAngle() +
                                    //((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                                    ((dx) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                    mRenderer.setXAngle((mRenderer.getXAngle() +
                            (dy * TOUCH_SCALE_FACTOR)));
                    requestRender();
                }
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}

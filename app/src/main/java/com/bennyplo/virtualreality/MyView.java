package com.bennyplo.virtualreality;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class MyView extends GLSurfaceView {
    private final MyRenderer mRenderer;
    private int ptcount=0;//touch counter
    private float m1TouchEventX,m1TouchEventY;//1st finger touch location
    private float m2TouchEventX,m2TouchEventY;//2nd finger touch location
    private float mTouchDistance;//distance between the 2 finger touches
    private float mViewScaledTouchSlop;//number of pixels that a finger is allowed to move
    public MyView(Context context) {
        super(context);
        setEGLContextClientVersion(2);// Create an OpenGL ES 2.0 context.
        mRenderer = new MyRenderer();// Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        final ViewConfiguration viewConfig = ViewConfiguration.get(context);//get the view configuration
        mViewScaledTouchSlop = viewConfig.getScaledTouchSlop();//number of pixels that a finger is allowed to move
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;//scale factor for the touch motions
    private final float TOUCH_ZOOM_FACTOR=1.0f/320;//zoom factor
    private float mPreviousX;//previous touch x position
    private float mPreviousY;//previous touch y position

    @Override
    public boolean onTouchEvent(MotionEvent e){//touch event
        float x = e.getX();//x position of the touch
        float y = e.getY();//y position of the touch
        switch (e.getAction()) {
            case MotionEvent.ACTION_POINTER_UP: case MotionEvent.ACTION_UP://finish touching
                ptcount--;//decrement the counter
                if (ptcount<-2)//if it is less than -2 -> reset the 2nd touch event positions
                {
                    m2TouchEventX=-1;
                    m2TouchEventY=-1;
                }
                if (ptcount<-1)//if it is less than -1 -> reset the 1st touch event positions
                {
                    m1TouchEventX=-1;
                    m1TouchEventY=-1;
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:case MotionEvent.ACTION_DOWN://touch down
                ptcount++;
                if (ptcount==1)//1 finger
                {
                    m1TouchEventX=e.getX(0);
                    m1TouchEventY=e.getY(0);
                }
                else if (ptcount==2)//2 finger
                {
                    m2TouchEventX=e.getX(0);
                    m2TouchEventY=e.getY(0);
                }
                break;
            case MotionEvent.ACTION_MOVE://moving
                m2TouchEventX=e.getX(0);
                m2TouchEventY=e.getY(0);
                if (isPinchGesture(e)) {//check to see if it is a pinch gesture
                    m2TouchEventX=e.getX(0);
                    m2TouchEventY=e.getY(0);
                    mTouchDistance=distance(e,0,1);//calculate the distance
                    mRenderer.setZoom(mTouchDistance* TOUCH_ZOOM_FACTOR);  // set the zoom
                    requestRender();//update the screen
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
                    //set the rotation angles
                    mRenderer.setYAngle( mRenderer.getYAngle() + ((dx) * TOUCH_SCALE_FACTOR));
                    mRenderer.setXAngle((mRenderer.getXAngle() +(dy * TOUCH_SCALE_FACTOR)));
                    requestRender();
                }
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }


    public float distance(MotionEvent e, int first, int second) {//distance between 2 touch motion events
        if (e.getPointerCount() >= 2) {
            final float x = e.getX(first) - e.getX(second);
            final float y = e.getY(first) - e.getY(second);
            return (float) Math.sqrt(x * x + y * y);//Euclidean distance
        } else {
            return 0;
        }
    }
    private boolean isPinchGesture(MotionEvent event) {//check if it is a pinch gesture
        if (event.getPointerCount() == 2) {//multi-touch
            //check the distances between the touch locations
            final float distanceCurrent = distance(event, 0, 1);
            final float diffPrimX = m1TouchEventX - event.getX(0);
            final float diffPrimY = m1TouchEventY - event.getY(0);
            final float diffSecX = m2TouchEventX - event.getX(1);
            final float diffSecY = m2TouchEventY - event.getY(1);

            if ( Math.abs(distanceCurrent - mTouchDistance) > mViewScaledTouchSlop && (diffPrimY * diffSecY) <= 0
                    && (diffPrimX * diffSecX) <= 0) {
                //if the distance between the touch is above the threshold and the fingers are moving in opposing directions
                return true;
            }
        }

        return false;
    }

}

package com.bennyplo.virtualreality;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyView GLView;
    private View mControlsView;
    private static Context mContext;//context of the activity
    public static Context getContext(){return mContext;}//get the activity context

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext=this;//set the context
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        GLView =new MyView(this);
        //setContentView(R.layout.activity_fullscreen);
        setContentView(GLView);
        //set full screen
        mControlsView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        mControlsView.setSystemUiVisibility(uiOptions);

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {//ensure that no matter which orientation, the app will use full screen!
        super.onConfigurationChanged(newConfig);
        mControlsView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        mControlsView.setSystemUiVisibility(uiOptions);
    }

}

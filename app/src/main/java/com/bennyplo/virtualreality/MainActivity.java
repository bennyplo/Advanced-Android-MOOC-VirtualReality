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
    private static final int READ_TEXT_FILE_REQUEST_CODE=12;//read file request code
    private static final int READ_IMAGE_FILE_REQUEST_CODE=13;//read image file request code
    public static Context getContext(){return mContext;}//get the activity context
    public static String textfile;
    public static Bitmap texturebitmap;//bitmap file for texture
    public static Bitmap getTextureBitmap()
    {
        return texturebitmap;
    }//get the bitmap for texture

    private String getTextFile(Uri filepath) throws IOException {
        //read the text file from the selected filepath
        //set to read the file path first
        ParcelFileDescriptor parcelFileDescriptor=getContentResolver().openFileDescriptor(filepath,"r");
        FileDescriptor fileDescriptor=parcelFileDescriptor.getFileDescriptor();//get the descriptor of the file
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor);//get the file input stream from the file descriptor
        StringBuilder stringBuilder=new StringBuilder();//create a string builder -> to create the string
        //read the text from the file
        int i=0;
        while((i=fileInputStream.read())!=-1){
            stringBuilder.append((char)i);
        }
        Log.i("ReadTextFile",stringBuilder.toString());
        fileInputStream.close();
        parcelFileDescriptor.close();
        //ParseFileString(stringBuilder.toString());
        return stringBuilder.toString();
    }

    public void FileTextSearch()
    {//search for a file
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);//set the intent to open a document
        intent.addCategory(Intent.CATEGORY_OPENABLE);//select only those are openable
        intent.setType("text/*");//set the type for searching
        startActivityForResult(intent,READ_TEXT_FILE_REQUEST_CODE);//start the request
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent resultData)
    {//once the file has been selected
        if (requestCode == READ_IMAGE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try { texturebitmap = getBitmapFile(uri); }//read the bitmap file
                catch (IOException e)
                { Log.i("MainActivity","get bitmap file error!"); }
            }
        }
        else if (requestCode==READ_TEXT_FILE_REQUEST_CODE && resultCode== Activity.RESULT_OK)
        {//handle the event from the read request only
            Uri uri=null;
            if (resultData!=null)
            {   uri=resultData.getData();//get the url selected
                try { textfile=getTextFile(uri);}//read the text file
                catch (IOException e)
                { Log.i("MainActivity","get file error!"); }
            }
        }
    }
    private Bitmap getBitmapFile(Uri filepath) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(filepath, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }
    public void ImageFileSearch() {//search for image file
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_IMAGE_FILE_REQUEST_CODE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext=this;//set the context
        //FileTextSearch();//search for a file
        //ImageFileSearch();//search for an image file
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

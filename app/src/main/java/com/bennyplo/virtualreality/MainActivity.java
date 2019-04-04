package com.bennyplo.virtualreality;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MyView GLView;
    private View mControlsView;
    private static Context mContext;//context of the activity
    private static final int READ_REQUEST_CODE=12;//read file request code
    public static Context getContext(){return mContext;}//get the activity context
    public static String textfile;

    private String getTextFile(Uri filepath) throws IOException {
        //read the text file from the selected filepath
        //set to read the file path first
        ParcelFileDescriptor parcelFileDescriptor=getContentResolver().openFileDescriptor(filepath,"r");
        //get the descriptor of the file
        FileDescriptor fileDescriptor=parcelFileDescriptor.getFileDescriptor();
        //get the file input stream from the file descriptor
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
        //BufferedReader bufferedReader =  new BufferedReader( new InputStreamReader(fileInputStream,"UTF-8");
        StringBuilder stringBuilder=new StringBuilder();//create a string builder -> to create the string
        //read the text from the file
        int i=0;
        while((i=fileInputStream.read())!=-1){
            //System.out.print((char)i);
            stringBuilder.append(i);
        }
        fileInputStream.close();
        parcelFileDescriptor.close();
        return stringBuilder.toString();
    }
    public void FileSearch()
    {//search for a file
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);//set the intent to open a document
        intent.addCategory(Intent.CATEGORY_OPENABLE);//select only those are openable
        intent.setType("*/*");//set the type for searching
        startActivityForResult(intent,READ_REQUEST_CODE);//start the request
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent resultData)
    {//once the file has been selected
        if (requestCode==READ_REQUEST_CODE && resultCode== Activity.RESULT_OK)
        {//handle the event from the read request only
            Uri uri=null;
            if (resultData!=null)
            {
                uri=resultData.getData();//get the url selected
                Log.i("MainActivity","Uri:"+uri.toString());
                try {
                    textfile=getTextFile(uri);//read the text file
                }
                catch (IOException e)
                {
                    Log.i("MainActivity","get file error!");
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext=this;//set the context
        FileSearch();//search for a file

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
}

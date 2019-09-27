package com.example.opencvcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG="MainActivity";
     CascadeClassifier cascadeClassifier;
     int width,height;
    int absoluteFaceSize;
    Button btn;
    int a,i;

    //Instance of java Camera View
    JavaCameraView javaCameraView;
    Mat mRgba,m1,m2,imgGrey,imgCnny;
    BaseLoaderCallback mLoaderCallBack=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    initializeOpenCVDependencies();break;
                    //javaCameraView.enableView();break;
                }

                default:{
                    super.onManagerConnected(status);
                    break;

                }
            }
        }
    };


    private void initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }


        // And we are ready to go
        javaCameraView.enableView();
    }


    static{

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        //Binding Java Camera view in layout
        javaCameraView=(JavaCameraView)findViewById(R.id.cam);
        btn=(Button)findViewById(R.id.button);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        //setting a callback function
        javaCameraView.setCvCameraViewListener(this);




    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView!=null)
        {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView!=null)
        {
            javaCameraView.disableView();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.i(TAG,"Opencv Loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.i(TAG,"Opencv not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9,this,mLoaderCallBack);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        m1=new Mat(height,width, CvType.CV_8UC4);
        m2=new Mat(height,width, CvType.CV_8UC4);
        imgGrey=new Mat(height,width, CvType.CV_8UC1);
        absoluteFaceSize=(int)(height*0.2);



    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        m1.release();
        m2.release();
        imgGrey.release();

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
       //rotate the frame to 90
       Core.transpose(mRgba,m1);
   Imgproc.resize(m1,m2,m2.size(),0,0,0);
        Core.flip(m2,mRgba,1);
        Imgproc.cvtColor(mRgba,imgGrey,Imgproc.COLOR_RGB2GRAY);

        // Use the classifier to detect faces

        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(imgGrey, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = faces.toArray();
        for ( i = 0; i <facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
        a=i;



        return mRgba;


    }
    public void onlogin(View view){
        Toast.makeText(MainActivity.this, "No.of faces "+i, Toast.LENGTH_SHORT).show();

    }

}

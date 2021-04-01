package com.example.opencvcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
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
    private static String TAG = "MainActivity";
    CascadeClassifier cascadeClassifier;
    public static final int detector = 0, detector1 = 1;
    private int detect = detector;
    int width, height;
    int absoluteFaceSize = 0;
    float relativeFaceSize = 0.2f;
    Button btn;
    int a, i;
    private cameraView cameraView;
    int count = 0;
    public static final int j = 0, k = 1;
    public static final int train = 0, idle = 2;
    private int state = idle;
    int max = 10;
    String path="";
    //Instance of java Camera View
    JavaCameraView javaCameraView;
    Mat mRgba, m1, m2, imgGrey, imgCnny;
    private ImageView imageView;
    Bitmap bitmap;
    Handler handler;
    ToggleButton toggleButton;
    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    initializeOpenCVDependencies();
                    break;
                    //javaCameraView.enableView();break;
                }

                default: {
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
            if (cascadeClassifier.empty()) {
                cascadeClassifier = null;
            } else
                cascadeDir.delete();
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }


        // And we are ready to go
        cameraView.enableView();
    }


    static {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        //Binding Java Camera view in layout
        cameraView = (cameraView) findViewById(R.id.cameraView);
        btn = (Button) findViewById(R.id.button);
        //javaCameraView.setVisibility(SurfaceView.VISIBLE);
        //setting a callback function
        cameraView.setCvCameraViewListener(this);
        imageView = findViewById(R.id.image);
        toggleButton = (ToggleButton) findViewById(R.id.toggle);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                captureOnClick();
            }
        });
        path=getFilesDir()+"facedetect";
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.obj == "IMG") {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(bitmap);
                    imageView.setImageBitmap(bitmap);
                    if (count > max - 1) {
                        toggleButton.setChecked(false);
                        captureOnClick();
                    }
                }
            }
        };
        boolean success=(new File(path)).mkdirs();
    }

    void captureOnClick(){
        if(toggleButton.isChecked())
            state=train;
        else
        {
            Toast.makeText(this,"captured",Toast.LENGTH_SHORT);
            count=0;
            state=idle;
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

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
      //  m1=new Mat(height,width, CvType.CV_8UC4);
       // m2=new Mat(height,width, CvType.CV_8UC4);
        imgGrey=new Mat(height,width, CvType.CV_8UC1);
        //absoluteFaceSize=(int)(height*0.2);



    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
//        m1.release();
//        m2.release();
        imgGrey.release();

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
        imgGrey=inputFrame.gray();
       //rotate the frame to 90
       //Core.transpose(mRgba,m1);
        Mat mrgbat =mRgba.t();

        Core.flip(mrgbat,mrgbat,1);
        Imgproc.resize(mrgbat,mrgbat,mRgba.size());
        //Imgproc.cvtColor(mRgba,imgGrey,Imgproc.COLOR_RGB2GRAY);

        // Use the classifier to detect faces

        if(absoluteFaceSize==0){
            int height=imgGrey.rows();
            if(Math.round(height*relativeFaceSize)>0){
                absoluteFaceSize=Math.round(height*relativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        if(detect==detector) {

            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(imgGrey, faces, 1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"facedetect","");
            }
        }
        else if(detect==detector1){

        }
        else{

        }


        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = faces.toArray();
        if((facesArray.length==1)&&(state==train)&&(count<max)){
            Mat mat;
            Rect rect=facesArray[0];
            mat=mRgba.submat(rect);
            bitmap=Bitmap.createBitmap(mat.width(),mat.height(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat,bitmap);
            Message message=new Message();
            String text="IMG";
            message.obj=text;
            handler.sendMessage(message);
            if(count<max){
                count=count+1;
            }
        }
        for ( i = 0; i <facesArray.length; i++)
            Core.rectangle(mrgbat, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
        a=i;



        return mrgbat;


    }
    public void onlogin(View view){
        Toast.makeText(MainActivity.this, "No.of faces "+i, Toast.LENGTH_SHORT).show();

    }

}

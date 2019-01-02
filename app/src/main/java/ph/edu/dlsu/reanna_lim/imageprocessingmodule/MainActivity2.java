package ph.edu.dlsu.reanna_lim.imageprocessingmodule;

import android.app.Activity;
import android.content.Context;
//import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.drawable.Drawable;
//import android.media.Image;
//import android.net.Uri;
//import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MenuItem;
//import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.*;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
//import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
//import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.video.BackgroundSubtractorMOG2;
//import org.opencv.videoio.VideoCapture;
//
//import java.io.Console;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//import static android.R.attr.width;
import static android.R.attr.left;
import static android.R.attr.right;
import static ph.edu.dlsu.reanna_lim.imageprocessingmodule.R.drawable.c;
import static ph.edu.dlsu.reanna_lim.imageprocessingmodule.R.drawable.e;
import static ph.edu.dlsu.reanna_lim.imageprocessingmodule.R.drawable.k;
import static org.opencv.core.CvType.CV_8UC3;
//import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity2 extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    ImageView   imageView1;
    Point clickedPoint = new Point(0, 0);
    private CameraBridgeViewBase mOpenCvCameraView;
    //    private boolean              mIsJavaCamera = true;
//    private MenuItem mItemSwitchCamera = null;
    static Context mContext;

    int imageWidth = 512;
    int imageHeight = 384;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity2() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        instantiateVariables();
//
//        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
//
//        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
//
//        mOpenCvCameraView.setCvCameraViewListener(this);
        //  Mat test1= doBackgroundRemovalAbsDiff();
        Mat test1 = subtractStuff();
//
        System.out.println("back to main.");

//        for (int i = 0; i < sizeA.height; i++) {
//            for (int j = 0; j < sizeA.width; j++) {
//                double[] rgb = test1.get(i, j);
//                 Log.i("", "red:"+rgb[0]+"green:"+rgb[1]+"blue:"+rgb[2]);
//
//            }
        //        System.out.println("dump: "+test1.dump());
//        }
        Bitmap bm = Bitmap.createBitmap(test1.cols(), test1.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(test1, bm);
        imageView1.setImageBitmap(bm);
//        imageView2.setImageBitmap(bm);
//        imageView3.setImageBitmap(bm);
//        imageView4.setImageBitmap(bm);

        System.out.println("system end.");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private void instantiateVariables(){
        mContext = getBaseContext();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        imageView1= (ImageView) findViewById(R.id.imageView1);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Mat processImage(){
        Mat oldFrame,currFrame;
        Mat greyImage = new Mat();
        Mat greyImage1 = new Mat();
        Mat foregroundImage = new Mat();
        Mat edged= new Mat();
//        oldFrame =  readImage(2);
        currFrame = readImage(6);
        Mat contour;
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        int thresh_type = Imgproc.THRESH_BINARY;
        Imgproc.cvtColor(currFrame, greyImage, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.cvtColor(oldFrame,greyImage1,Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(greyImage,edged,50,100);
        Imgproc.threshold(greyImage, greyImage, 100, 200, thresh_type);
        Imgproc.adaptiveThreshold(greyImage, foregroundImage, 200, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
//        Imgproc.dilate(greyImage,greyImage,edged);
//        Imgproc.erode(greyImage,greyImage,edged);

        Imgproc.findContours(foregroundImage, contours, foregroundImage.clone(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);;

        for(Mat c :contours){
            if(Imgproc.contourArea(c)<100){
                //insignificant.
            }else{
                Mat orig = currFrame.clone();
//              Mat box = Imgproc.minAreaRect(c);



            }
        }


//        # sort the contours from left-to-right and initialize the
//        # 'pixels per metric' calibration variable

//        contours=contours.sort(contours);

        return foregroundImage;
    }


    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    private Mat doBackgroundRemovalFloodFill()
    {
        Mat oldFrame,currFrame;
        Mat greyImage = new Mat();
        Mat foregroundImage = new Mat();

        oldFrame =  readImage(2);
        Scalar newVal = new Scalar(255, 255, 255);
        Scalar loDiff = new Scalar(50, 50, 50);
        Scalar upDiff = new Scalar(50, 50, 50);
        Point seedPoint = clickedPoint;
        Mat mask = new Mat();
        Rect rect = new Rect();

        // Imgproc.floodFill(frame, mask, seedPoint, newVal);
        Imgproc.floodFill(oldFrame, mask, seedPoint, newVal, rect, loDiff, upDiff, Imgproc.FLOODFILL_FIXED_RANGE);

        return oldFrame;
    }

    /**
     * Perform the operations needed for removing a uniform background
     *
     *
     *            the current frame
     * @return an image with only foreground objects
     */
    private Mat doBackgroundRemoval()
    {
        Mat oldFrame,currFrame;
        Mat greyImage = new Mat();
        Mat foregroundImage = new Mat();

        oldFrame =  readImage(2);
        currFrame = readImage(1);

        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;

        // threshold the image with the average hue value
        hsvImg.create(oldFrame.size(), CvType.CV_8U);
        Imgproc.cvtColor(oldFrame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(oldFrame.size(), CV_8UC3, new Scalar(255, 255, 255));
        oldFrame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public Mat readImage(int i) {
        Log.i(TAG, "reading image");
        InputStream stream = null;
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.white_bg, imageWidth, imageHeight);
        //Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.drawable.b);

        if(i==1)
            bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.white_bg, imageWidth, imageHeight); //BitmapFactory.decodeResource(getResources(), R.drawable.white_bg_ruler); //a
        else if(i==2)
            bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.front_1, imageWidth, imageHeight); //BitmapFactory.decodeResource(getResources(), R.drawable.front_1); //b
        else if(i==3)
            bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.side_1, imageWidth, imageHeight); //BitmapFactory.decodeResource(getResources(), c);
        else if(i==4)
            bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.front_2, imageWidth, imageHeight); //BitmapFactory.decodeResource(getResources(), R.drawable.d);
        else if(i==5)
            bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.side_2, imageWidth, imageHeight); //BitmapFactory.decodeResource(getResources(), e);
        /*else if(i==6)
            bmp = BitmapFactory.decodeResource(getResources(),R.drawable.h);
        else if(i==7)
            bmp = BitmapFactory.decodeResource(getResources(), k);
        else if(i==8)
            bmp = BitmapFactory.decodeResource(getResources(),R.drawable.k1);
        else if(i==9)
            bmp = BitmapFactory.decodeResource(getResources(),R.drawable.a1);*/
        Mat ImageMat = new Mat();
        Utils.bitmapToMat(bmp, ImageMat);
        return ImageMat;
    }
    private double getHistAverage(Mat hsvImg, Mat hueValues)
    {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }
    private Mat doBackgroundRemovalAbsDiff()
    {
        Mat oldFrame,currFrame;
        Mat greyImage = new Mat();
        Mat greyImage1 = new Mat();
        Mat foregroundImage = new Mat(); InputStream ims=null;



        currFrame = readImage(1);
        oldFrame = readImage(8);


//        int rows = currFrame.rows();
//        int cols = currFrame.cols();
//
//        System.out.println("fiwrst rows: "+rows);
//        System.out.println("first cols: "+cols);
//
//
//
//         rows = oldFrame.rows();
//         cols = oldFrame.cols();
//
//        System.out.println("second rows: "+rows);
//        System.out.println("second cols: "+cols);
//        Size s = currFrame.size();
//        rows = s.height;
//        cols = s.width;

//        Core.subtract(oldFrame,currFrame,foregroundImage);
        int thresh_type = Imgproc.THRESH_BINARY;
        Imgproc.cvtColor(oldFrame, greyImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(currFrame, greyImage1, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.cvtColor(foregroundImage, foregroundImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(greyImage,greyImage,80,255,thresh_type);
//



//        Imgproc.threshold(greyImage1, greyImage1, 20, 200, thresh_type);
//        Core.subtract(greyImage,greyImage1,foregroundImage);
//        Core.subtract(greyImage1,greyImage , foregroundImage);
//        if (this.inverse.isSelected())
//            thresh_type = Imgproc.THRESH_BINARY;
//       Imgproc.adaptiveThreshold(oldFrame, oldFrame, 190, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Imgproc.adaptiveThreshold(greyImage, greyImage, 250, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

//        Imgproc.threshold(greyImage, greyImage, 40, 255, thresh_type);
//          currFrame.copyTo(foregroundImage, greyImage);
//
//        //oldFrame = currFrame;
//        Log.d(TAG, "finished subtracting images.");
        System.out.println("finish subtracting");
        return greyImage;

    }


    private Mat subtractStuff(){
        Log.i(TAG, "subtracting image");
        Mat oldFrame,currFrame;
        Mat greyImage = new Mat();
        Mat greyImage1 = new Mat();
        Mat foregroundImage = new Mat(); InputStream ims=null;



        currFrame = readImage(1);
        oldFrame = readImage(4);
        int thresh_type = Imgproc.THRESH_BINARY;
        Imgproc.threshold(foregroundImage,foregroundImage,100,255,thresh_type);
        Imgproc.cvtColor(currFrame, greyImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(oldFrame, greyImage1, Imgproc.COLOR_BGR2GRAY);


        Core.subtract(currFrame,oldFrame,foregroundImage);

        Imgproc.cvtColor(foregroundImage, foregroundImage, Imgproc.COLOR_BGR2GRAY);


        Imgproc.threshold(foregroundImage,foregroundImage,20,255,thresh_type);

        Imgproc.adaptiveThreshold(foregroundImage, foregroundImage, 250, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
//
        Log.i(TAG, "returning foreground image");
        return foregroundImage;
    }

}

package ph.edu.dlsu.reanna_lim.imageprocessingmodule;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GetbetterMain::Activity";
    private static final int CAMERA_REQUEST = 1888;
    private final int SELECT_PHOTO = 1;
    private final int SELECT_BG_PHOTO = 2;
    private ImageView imageView;
    private double WIDTH =30.48;
    private EditText et_modifier;
    private int IMAGENUM = 5;
    private Mat imageToCalculate;
    private Mat backgroundImageToSubtract;
    private Mat mat_targetOrigImage,mat_subtractedImage,mat_thresHoldedImage,mat_ImageInView;
    private Boolean hasBeenGrayScaled;
    private int modifier=0;
    private Bitmap bmp_targetImage;
    static Context mContext;
    public int n = 0;

    Mat mat_adaptedTresholdedImage;
    Button photoButton, btn_loadFromDrawable,btn_sdcompute,btn_pick,btn_loadImageToSub,btn_subtractImages,btn_basicTreshold,btn_adaptiveThreshold;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instantiateVariables();

        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        btn_loadFromDrawable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mat test1 =preProcessImage(readImage(IMAGENUM));
//                loadDrawables();
                Bitmap bm = Bitmap.createBitmap(test1.cols(), test1.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(test1, bm);


//                File sdCardDirectory = Environment.getExternalStorageDirectory();
//                File image = new File(sdCardDirectory, "test.png");
//
//                boolean success = false;
//
//                // Encode the file as a PNG image.
//                FileOutputStream outStream;
//                try {
//
//                    outStream = new FileOutputStream(image);
//                    bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//        /* 100 to keep full quality of the image */
//
//                    outStream.flush();
//                    outStream.close();
//                    success = true;
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                if(success){
//                    System.out.println("yey");
//                }
                imageView.setImageBitmap(bm);
            }
        });

        btn_pick.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        btn_sdcompute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Mat test1 = tryCanny();
                Bitmap bm = Bitmap.createBitmap(test1.cols(), test1.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(test1, bm);

                imageView.setImageBitmap(bm);
//                SaveImage(bm);
            }
        });
        btn_loadImageToSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_BG_PHOTO);

            }
        });

        btn_subtractImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mat_subtractedImage = backgroundSubtractionAlgo(mat_targetOrigImage);
                Bitmap bm = Bitmap.createBitmap(mat_subtractedImage.cols(), mat_subtractedImage.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat_subtractedImage, bm);
                mat_ImageInView = mat_subtractedImage;
                imageView.setImageBitmap(bm);
            }
        });
        btn_basicTreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mat_thresHoldedImage = basicThresholdAlgo(mat_ImageInView);

                Bitmap bm = Bitmap.createBitmap(mat_thresHoldedImage.cols(), mat_thresHoldedImage.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat_thresHoldedImage, bm);
                mat_ImageInView = mat_thresHoldedImage;
                imageToCalculate = mat_thresHoldedImage;
                imageView.setImageBitmap(bm);
            }
        });
        btn_adaptiveThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mat x = new Mat();
                if(!hasBeenGrayScaled){
                    Imgproc.cvtColor(mat_ImageInView, x, Imgproc.COLOR_BGR2GRAY);
                    hasBeenGrayScaled= true;
                }
                Imgproc.adaptiveThreshold(x, mat_adaptedTresholdedImage, 250, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

                mat_ImageInView = mat_adaptedTresholdedImage;
                Bitmap bm = Bitmap.createBitmap(mat_adaptedTresholdedImage.cols(), mat_adaptedTresholdedImage.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat_adaptedTresholdedImage, bm);
                imageView.setImageBitmap(bm);
            }
        });
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        n++;
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        addImageToGallery(root+"/saved_images/"+fname, getBaseContext());
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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
        hasBeenGrayScaled=false;
        mat_targetOrigImage = new Mat();
        mat_subtractedImage= new Mat();
        mat_adaptedTresholdedImage = new Mat();
        mat_thresHoldedImage = new Mat();
        mat_ImageInView = new Mat();
        backgroundImageToSubtract = new Mat();
        et_modifier = (EditText) this.findViewById(R.id.et_modifier);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        photoButton = (Button) this.findViewById(R.id.button1);
        btn_loadFromDrawable = (Button) this.findViewById(R.id.button2);
        btn_sdcompute = (Button) this.findViewById(R.id.btn_sdcompute);
        btn_pick = (Button) this.findViewById(R.id.btn_pick);
        btn_loadImageToSub = (Button) this.findViewById(R.id.btn_loadBG);
        btn_subtractImages = (Button) this.findViewById(R.id.btn_subtractImages);
        btn_basicTreshold = (Button) this.findViewById(R.id.btn_basicTreshold);
        btn_adaptiveThreshold = (Button) this.findViewById(R.id.btn_adaptiveThreshold);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Mat test1 = preProcessImage(photo);

            Bitmap bm = Bitmap.createBitmap(test1.cols(), test1.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(test1, bm);
            imageView.setImageBitmap(bm);
        }

        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                bmp_targetImage = selectedImage;

                Utils.bitmapToMat(bmp_targetImage, mat_targetOrigImage);

                mat_ImageInView = mat_targetOrigImage;
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (requestCode == SELECT_BG_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
                Utils.bitmapToMat(selectedImage, backgroundImageToSubtract);
                mat_ImageInView = backgroundImageToSubtract;
                Toast.makeText(this, "background image loaded.",
                        Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    private Mat preProcessImage(Bitmap photo){
        Mat ImageMat = new Mat();
        Utils.bitmapToMat(photo, ImageMat);

        ImageMat = backgroundSubtractionAlgo(ImageMat);
        ImageMat = basicThresholdAlgo(ImageMat);
        imageToCalculate = ImageMat;
        return ImageMat;
    }
    private Mat tryCanny( ) {
        //convert to Mat
        Mat ImageMat;
        Mat contourFrame;


        modifier = Integer.parseInt(et_modifier.getText().toString());
        ImageMat = imageToCalculate;
        List<MatOfPoint> contours = new ArrayList<>();
        Rect[] boundingBoxes;
        //find contours in the edge map
        Imgproc.findContours(ImageMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //sort contours and return bounding boxes.
        boundingBoxes = sortContours(contours, "left-to-right");

        double pixelsPerMetric = -1;
        double computedHeight = 0;
        double computedWidth = 0;
        double initialHeight = 0;
        double initialWidth = 0;
        int x = 0;
        // loop over the contours individually
        contourFrame = ImageMat;
        for (Rect r : boundingBoxes) {
            x++;
            if(r==null){
                break;
            }else {
                //draw bounding box in image.
                Imgproc.rectangle(contourFrame, r.tl(), r.br(), new Scalar(255, 125, 152), 3, 8, 0);

                if(x==1){
                    pixelsPerMetric = r.width / WIDTH;
                    Imgproc.putText (
                            contourFrame,                          // Matrix obj of the image
                            "x: "+x,          // Text to be added
                            r.tl(),               // point
                            Core.FONT_HERSHEY_SIMPLEX ,      // front face
                            5,                               // front scale
                            new Scalar(255, 150, 150),             // Scalar object for color
                            4);                                // Thickness
                    double x1= r.tl().x;
                    double y1= r.tl().y;
                    double x2=  r.br().x;
                    double y2= r.br().y;

                    double height=x2-x1;   //height or length
                    double width=y2-y1;    // width or breadth


                    initialHeight= height/pixelsPerMetric;
                    initialWidth =width/pixelsPerMetric;
                }
                else if(x==2){
                    computedHeight = r.height / pixelsPerMetric;
                    computedWidth = r.width / pixelsPerMetric;

                    Imgproc.putText (
                            contourFrame,                          // Matrix obj of the image
                            "xa: "+x,          // Text to be added
                            r.tl(),               // point
                            Core.FONT_HERSHEY_SIMPLEX ,      // front face
                            5,                               // front scale
                            new Scalar(255, 0, 0),             // Scalar object for color
                            4                                // Thickness
                    );

                }
            }}

        Toast.makeText(this, "computed height: "+computedHeight,
                Toast.LENGTH_SHORT).show();
        System.out.println("pixels per metric: "+pixelsPerMetric);
        System.out.println("initial height:"+initialHeight);
        System.out.println("initial width:"+initialWidth);
        System.out.println("computed height:"+computedHeight);
        System.out.println("computed width:"+computedWidth);
        System.out.println("computed: "+x);
        return contourFrame;
    }
    public Mat basicThresholdAlgo(Mat ImageMat){
        int thresh_type = Imgproc.THRESH_BINARY;
        //CONVERT to gray, then blur it slightly
        if(!hasBeenGrayScaled){
            Imgproc.cvtColor(ImageMat, ImageMat, Imgproc.COLOR_BGR2GRAY);
            hasBeenGrayScaled= true;
        }

        org.opencv.core.Size s = new Size(3, 3);
        Imgproc.GaussianBlur(ImageMat, ImageMat, s, 2);
        Imgproc.threshold(ImageMat, ImageMat, 100, 155, thresh_type);

//        //perform edge detection, then perform a dilation + erosion to# close gaps in between object edges
//        Imgproc.Canny(ImageMat, ImageMat, 50, 100);
//        Imgproc.dilate(ImageMat, ImageMat, Imgproc.getStructuringElement(Imgproc.CHAIN_APPROX_NONE, new Size(2, 2)));
//        Imgproc.erode(ImageMat, ImageMat, Imgproc.getStructuringElement(Imgproc.CHAIN_APPROX_NONE, new Size(2, 2)));
////        Imgproc.RETR_EXTERNAL
        return ImageMat;
    }

    public Mat backgroundSubtractionAlgo(Mat ImageMat){
        Mat oldFrame,currFrame;
        Mat greyImage = new Mat();
        Mat greyImage1 = new Mat();

        Mat foregroundImage = new Mat();
//        InputStream ims=null;
        oldFrame = new Mat();
        currFrame=ImageMat;


        oldFrame = backgroundImageToSubtract;


//        int thresh_type = Imgproc.THRESH_BINARY;
//        Imgproc.threshold(foregroundImage,foregroundImage,100,255,thresh_type);
        if(!hasBeenGrayScaled){
            hasBeenGrayScaled= true;
            Imgproc.cvtColor(currFrame, greyImage, Imgproc.COLOR_BGR2GRAY);}
        Imgproc.cvtColor(oldFrame, greyImage1, Imgproc.COLOR_BGR2GRAY);


        Size sz = new Size(640,480);
        Imgproc.resize( oldFrame, oldFrame, sz );
        Imgproc.resize( currFrame, currFrame, sz );



        Core.subtract(oldFrame,currFrame,foregroundImage);

        Imgproc.cvtColor(foregroundImage, foregroundImage, Imgproc.COLOR_BGR2GRAY);

        hasBeenGrayScaled= true;
//        Imgproc.threshold(foregroundImage,foregroundImage,20,255,thresh_type);

//        Imgproc.adaptiveThreshold(foregroundImage, foregroundImage, 250, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        return foregroundImage;
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


    public Bitmap readImage(int i) {
//        InputStream stream = null;
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.drawable.k);
        if(i==1){
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.k);}
        if(i==2){
            System.out.println("i is 2.");
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a2);}
        if(i==3){
            bmp = BitmapFactory.decodeResource(getResources(),R.drawable.z1);
        }
        if(i==4){
            bmp = BitmapFactory.decodeResource(getResources(),R.drawable.a11a);
        }
        if(i==5){
            bmp = decodeSampledBitmapFromResource(getResources(), R.drawable.front_2, 512, 384);
            //bmp = BitmapFactory.decodeResource(getResources(),R.drawable.front_3);
        }
        if(i==6){
            bmp = BitmapFactory.decodeResource(getResources(),R.drawable.white_bg);

        }
        return bmp;
    }

    private Rect[] sortContours(List<MatOfPoint> contours, String method) {
        // initialize the reverse flag and sort index
        boolean reverse;
        reverse = false;
        int numberOfTimesCalled= 0;
        // handle if needed to sort in reverse
        if (method == "right-to-left" || method=="bottom-to-top")
            reverse = true;
        else{
            reverse =false;
        }


        // construct the list of bounding boxes and sort them from top to
        // bottom
        Rect[] boundingBoxes = new Rect[contours.size()];
//        Rect rect = Imgproc.boundingRect(c);
        Rect temp;
        System.out.println("counter numbers"+contours.size());
        for (int j = 0; j < contours.size(); j++) {
//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
            boundingBoxes[numberOfTimesCalled] = Imgproc.boundingRect(contours.get(j));
//                System.out.println("bounding boxes numbers: "+boundingBoxes[numberOfTimesCalled].tl().x);
//                System.out.println("called.");
            numberOfTimesCalled++;
//            }
        }
//        System.out.println("bounding boxes numbers"+boundingBoxes.length);

        System.out.println("number of time called"+numberOfTimesCalled );
        //sort
        if(!reverse) { //sort ascending
            for (int j = 0; j < numberOfTimesCalled; j++) {
                for (int k = 0; k < numberOfTimesCalled - 1-j; k++) {
//                    System.out.println("called");
                    if (boundingBoxes[k].tl().x > boundingBoxes[k + 1].tl().x) {
                        temp = boundingBoxes[k];
                        boundingBoxes[k] = boundingBoxes[k + 1];
                        boundingBoxes[k + 1] = temp;
                    }

                }
            }
        }
        else{ //sort descending
            for (int j = 0; j < numberOfTimesCalled; j++) {
                for (int k = 0; k < numberOfTimesCalled- 1-j; k++) {
                    if (boundingBoxes[k].tl().x < boundingBoxes[k + 1].tl().x) {
                        temp = boundingBoxes[k];
                        boundingBoxes[k] = boundingBoxes[k + 1];
                        boundingBoxes[k + 1] = temp;
                    }

                }
            }
        }
        return boundingBoxes;
    }





//    public  void loadDrawables() {
//        for(int identifier = (R.drawable.aaaa + 1);
//            identifier <= (R.drawable.zzzz - 1);
//            identifier++) {
//            String name = getResources().getResourceEntryName(identifier);
//            //name is the file name without the extension, indentifier is the resource ID
//        }
//    }


}
//OLD ALGO
//for(MatOfPoint c : contours){
//
//            //if the contour is not sufficiently large, ignore it
//            if(Imgproc.contourArea(c)<100){
////                System.out.println("oh no.");
//            }else{
//                x++;
//                MatOfPoint2f c2f = new MatOfPoint2f();
//                c.convertTo(c2f, CvType.CV_32FC2);
//
//
//                //find boundingbox.
////                Imgproc.rectangle(c, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), (255, 0, 0, 255), 3);
//
////                rect.width
////                rect.height
//
////                 if the pixels per metric has not been initialized, then
////                 compute it as the ratio of pixels to supplied metric
////                 (in this case, inches)
//                 if(x==13){
//                 pixelsPerMetric = rect.width / WIDTH;
//                     Imgproc.putText (
//                             contourFrame,                          // Matrix obj of the image
//                             "x: "+x,          // Text to be added
//                             rect.tl(),               // point
//                             Core.FONT_HERSHEY_SIMPLEX ,      // front face
//                             5,                               // front scale
//                             new Scalar(255, 0, 0),             // Scalar object for color
//                             4                                // Thickness
//                     );
//
//
//                     double x1= rect.tl().x;
//                     double y1= rect.tl().y;
//                     double x2=  rect.br().x;
//                     double y2= rect.br().y;
//
//                     double height=x2-x1;   //height or length
//                     double width=y2-y1;    // width or breadth
//
//
//                     initialHeight= height/pixelsPerMetric;
//                     initialWidth =width/pixelsPerMetric;
//
//                 }
//
//                if(x==14) {
//                    computedHeight = rect.height / pixelsPerMetric;
//                    computedWidth = rect.width / pixelsPerMetric;
//
//                    Imgproc.putText (
//                            contourFrame,                          // Matrix obj of the image
//                            "xa: "+x,          // Text to be added
//                            rect.tl(),               // point
//                            Core.FONT_HERSHEY_SIMPLEX ,      // front face
//                            5,                               // front scale
//                            new Scalar(255, 0, 0),             // Scalar object for color
//                            4                                // Thickness
//                    );
//                }
//
//                //compute midpoints between points
////                findMidpoint(rect.tl(),rect.br());
////
//
////                RotatedRect rc = Imgproc.minAreaRect(c2f);
////                Imgproc.boxPoints(rc,c);
//                //box = np.array(box, dtype="int")
//
//            }


////        }
//        System.out.println("pixels per metric: "+pixelsPerMetric);
//        System.out.println("initial height:"+initialHeight);
//        System.out.println("initial width:"+initialWidth);
//        System.out.println("computed height:"+computedHeight);
//        System.out.println("computed width:"+computedWidth);
//        System.out.println("computed: "+x);
//        return contourFrame;
//    }
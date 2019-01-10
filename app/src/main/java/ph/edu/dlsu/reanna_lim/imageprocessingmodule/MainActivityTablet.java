package ph.edu.dlsu.reanna_lim.imageprocessingmodule;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivityTablet extends AppCompatActivity {
    private static final String TAG = "GetbetterMain::Activity";
    private static final int CAMERA_REQUEST1 = 1888;
    private static final int CAMERA_REQUEST2 = 1889;
    private final int SELECT_PHOTO1 = 1;
    private final int SELECT_PHOTO2 = 2;



    private ImageView imageView1, imageView2;
    private Mat mat_targetOrigImage;
    private Bitmap bmp_targetImage;
//    static Context mContext;
    int numberOfTimesCalled;
    public int n = 0;
    Mat mat_adaptedTresholdedImage;
    Button btn_take_picture1, btn_load_image1, btn_take_picture2, btn_load_image2, btn_compute, btn_go_back, btn_cancel,btn_calibrate;
    TextView tv_height, tv_weight;
    int currentImage = 0; // 0 for front, 1 for side
    Double finalHeight=0.0;
    Double final_weight = 0.0;
    private String path1, path2;
    private Bitmap bmp_silhouette;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tablet_layout);

        instantiateVariables();

        btn_take_picture1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST1);
            }
        });

        btn_take_picture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST2);
            }
        });

        btn_load_image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO1);
            }
        });

        btn_load_image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO2);
            }
        });

        btn_compute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { int currentPhoto = 0;

                Mat mat_startImage = new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_startImage);
                Mat mat_cloneForBiggestX =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_cloneForBiggestX);



                Double imageResolution = Double.valueOf(mat_startImage.rows());


//**************************************** FIND REFERENCE OBJECT
                int twentyPercent = (int) (mat_startImage.cols() * .20);
                Rect referenceObject = IPUtils.findReferenceObjectRect(mat_startImage,imageView1);
//****************************************** END FIND REFERENCE OBJECT.

//****************************************FIND BOTTOM Y
                Mat thresHoldedY;
                thresHoldedY = cropImageForBottomY(mat_startImage,referenceObject,twentyPercent);
                thresHoldedY= IPUtils.basicThresholdAlgo(thresHoldedY);
                Rect bottomY = findTopMostY(thresHoldedY);
//****************************************END FIND BOTTOM Y

//***************************************FIND BIGGEST X
//
//                Rect biggestX=null;
//                mat_cloneForBiggestX =  cropImageForReference(mat_cloneForBiggestX,twentyPercent,fifteenPercent);
////                mat_cloneForBiggestX = IPUtils.basicThresholdAlgo(mat_cloneForBiggestX);
//
////                biggestX = findBiggestX(mat_cloneForBiggestX);
////                mat_cloneForBiggestX = drawBoxes(mat_cloneForBiggestX);
////                imageView1.setImageBitmap(bm2);
//                Bitmap bitmap_biggestXFinal = Bitmap.createBitmap(mat_cloneForBiggestX.cols(), mat_cloneForBiggestX.rows(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(mat_cloneForBiggestX, bitmap_biggestXFinal);
//                imageView2.setImageBitmap(bitmap_biggestXFinal);
//****************************************END FIND BIGGEST X

//****************************************FIND SECOND BOTTOM Y
                Rect bottomY2=  null;
                thresHoldedY = new Mat();
                thresHoldedY = cropImageForBottomY2(mat_startImage,referenceObject,twentyPercent);
                thresHoldedY= IPUtils.basicThresholdAlgo(thresHoldedY);

                bottomY2 = findTopMostYForBot(thresHoldedY);
//****************************************END FIND BOTTOM Y
//***************************************FIND BIGGEST X
//
                Rect biggestX=null;
                mat_cloneForBiggestX =  cropImageForTopY(mat_cloneForBiggestX,referenceObject,twentyPercent);
                mat_cloneForBiggestX = IPUtils.basicThresholdAlgo(mat_cloneForBiggestX);

                biggestX = findBiggestX(mat_cloneForBiggestX);


                int calculatedSideWidth =biggestX.width;
//****************************************END FIND BIGGEST X
//*****************************************FIND TOPMOST Y

                Mat  mat_topMostY;
                mat_topMostY = cropImageForTopY(mat_startImage,referenceObject,twentyPercent);
                mat_topMostY = IPUtils.basicThresholdAlgo(mat_topMostY);
                Rect topMostY = findTopMostY(mat_topMostY);

                Bitmap startBM = IPUtils.basicTresholdedForView(mat_startImage) ;
                IPUtils.display(imageView2,startBM);
//
// *******************************************END TOPMOST YY


//*******************************************REFORMED ALGORITHM:
                Double subjectHeightInPixels,subjectHeightInPixels2;

                Boolean feetFound = false;
                Boolean feetFound2= false;
                if(bottomY==null){
                    subjectHeightInPixels = imageResolution-topMostY.tl().y;
                }else {
                    subjectHeightInPixels = (bottomY.tl().y)+referenceObject.br().y-topMostY.tl().y;
                    feetFound=true;
                }
                if(bottomY2==null){
                    subjectHeightInPixels2 = imageResolution-topMostY.tl().y;
                }else {
                    subjectHeightInPixels2 = (bottomY2.tl().y)+referenceObject.br().y-topMostY.tl().y;
                    feetFound2=true;
                }
                Boolean bothFrontFeetFound= true;

                Double pixelsPerMetric = referenceObject.height/30.48;
                Double trueHeight = subjectHeightInPixels/pixelsPerMetric;
                Double trueHeight2 = subjectHeightInPixels2/pixelsPerMetric;
                Double oldtrueHeight = (imageResolution-topMostY.tl().y)/pixelsPerMetric;


                if(difference(trueHeight,trueHeight2)>10){
                    trueHeight= oldtrueHeight;
                    trueHeight2 = oldtrueHeight;
                }

                if(feetFound || feetFound2){
                    if(!feetFound){
                        trueHeight =99999999.0;
                        bothFrontFeetFound=false;
                    }else if(!feetFound2){
                        trueHeight2 =99999999.0;
                        bothFrontFeetFound=false;
                    }
                }





                System.out.println("FOR side ***************************");
                System.out.println("pixels per metric: "+pixelsPerMetric);
                System.out.println("OLD ALGO TRUE HEIGHT"+oldtrueHeight);
                System.out.println("NEW ALGO TRUE HEIGHT USING TL "+trueHeight);
                System.out.println("NEW ALGO TRUE HEIGHT USING BR "+trueHeight2);

//*********************************************SIDE**********************************************************
                currentPhoto = 1;
                Mat mat_startImageSide = new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_startImageSide);
                Mat mat_cloneForBiggestXSide =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_cloneForBiggestXSide);
                Double imageResolution2 = Double.valueOf(mat_startImageSide.rows());


//**************************************** FIND REFERENCE OBJECT
                twentyPercent = (int) (mat_startImageSide.cols() * .20);
                referenceObject = IPUtils.findReferenceObjectRect(mat_startImageSide,imageView1);
//****************************************** END FIND REFERENCE OBJECT.

//****************************************FIND BOTTOM Y
                Mat thresHoldedYSide;
                thresHoldedYSide = cropImageForBottomY(mat_startImageSide,referenceObject,twentyPercent);
                thresHoldedYSide= IPUtils.basicThresholdAlgo(thresHoldedYSide);
                Rect bottomYSide = findTopMostY(thresHoldedYSide);
//****************************************END FIND BOTTOM Y


//****************************************FIND SECOND BOTTOM Y
                Rect bottomY2Side=  null;
                Mat thresHoldedY2Side;
                thresHoldedY2Side = cropImageForBottomY2(mat_startImageSide,referenceObject,twentyPercent);
                thresHoldedY2Side= IPUtils.basicThresholdAlgo(thresHoldedY2Side);

                bottomY2Side = findTopMostYForBot(thresHoldedY2Side);
//****************************************END FIND BOTTOM Y

//***************************************FIND BIGGEST X
                Rect biggestXSide=null;
                mat_cloneForBiggestXSide =  cropImageForTopY(mat_startImageSide,referenceObject,twentyPercent);
                mat_cloneForBiggestXSide = IPUtils.basicThresholdAlgo(mat_cloneForBiggestXSide);

                biggestXSide = findBiggestX(mat_cloneForBiggestXSide);
                int calculatedFrontWidth =biggestXSide.width;

                //****************************************END FIND BIGGEST X
//*****************************************FIND TOPMOST Y

                Mat  mat_topMostYSide;
                mat_topMostYSide = cropImageForTopY(mat_startImageSide,referenceObject,twentyPercent);
                mat_topMostYSide = IPUtils.basicThresholdAlgo(mat_topMostYSide);
                Rect topMostYSide = findTopMostY(mat_topMostYSide);

                try {
                    Bitmap bm = IPUtils.basicTresholdedForView(mat_startImageSide);
                    IPUtils.display(imageView1, bm);
                }catch(NullPointerException e ){
                    System.out.println("null error.");
                }
//
// *******************************************END TOPMOST YY

//*******************************************REFORMED ALGORITHM:

                Double subjectHeightInPixels3,subjectHeightInPixels4;
                Boolean sideFeetFound=false;
                Boolean sideFeetFound2=false;
                if(bottomYSide==null){
                    subjectHeightInPixels3 = imageResolution2-topMostY.tl().y;
                }else {
                    subjectHeightInPixels3 = (bottomYSide.tl().y)+referenceObject.br().y-topMostYSide.tl().y;
                    sideFeetFound =true;

                }
                if(bottomY2Side==null){
                    subjectHeightInPixels4 = imageResolution2-topMostY.tl().y;
                }else {
                    subjectHeightInPixels4 = (bottomY2Side.tl().y)+referenceObject.br().y-topMostYSide.tl().y;
                    sideFeetFound2 =true;
                }
                Boolean bothSideFeetFound= true;
                Double pixelsPerMetricSide = referenceObject.height/30.48;
                Double trueHeightSide = subjectHeightInPixels3/pixelsPerMetricSide;
                Double trueHeight2Side = subjectHeightInPixels4/pixelsPerMetricSide;

                Double oldtrueHeightSide = (imageResolution2-topMostYSide.tl().y)/pixelsPerMetricSide;

                if(difference(trueHeightSide,trueHeight2Side)>10){
                    trueHeightSide = oldtrueHeightSide;
                    trueHeight2Side =oldtrueHeightSide;
                }

                if(sideFeetFound || sideFeetFound2){
                    if(!sideFeetFound){
                        bothSideFeetFound=false;
                        trueHeightSide = 999999.0;
                    }else if(!sideFeetFound2){
                        trueHeight2Side =999999.0;
                        bothSideFeetFound=false;
                    }
                }

                if(trueHeight<90){
                    trueHeight = 9999999.0;
                }
                if(trueHeight2<90){
                    trueHeight2= 9999999.0;
                }


                if(trueHeightSide<90){
                    trueHeightSide = 999999.0;
                }

                if(trueHeight2Side<90){
                    trueHeight2Side =999999.0;
                }
                if(bothFrontFeetFound&& bothSideFeetFound){

                }else if(bothFrontFeetFound || bothSideFeetFound){
                    if(!bothFrontFeetFound){
                        trueHeight = 999999999999.0;
                        trueHeight2 = 999999999999.0;
                    }else{
                        trueHeightSide = 999999.0;
                        trueHeight2Side =999999.0;
                    }
                }

                System.out.println("biggest X"+biggestX.width/pixelsPerMetric );
                System.out.println(" biggestXSide: "+ biggestXSide.width/pixelsPerMetricSide);
                System.out.println("FOR front*********************************");
                System.out.println("pixels per metric: "+pixelsPerMetricSide);
                System.out.println("OLD ALGO TRUE HEIGHT"+oldtrueHeightSide);
                System.out.println("NEW ALGO TRUE HEIGHT USING TL "+trueHeightSide);
                System.out.println("NEW ALGO TRUE HEIGHT USING BR "+trueHeight2Side);

            if(oldtrueHeightSide<trueHeight2Side){
                trueHeight2Side = oldtrueHeightSide;
            }if(oldtrueHeightSide <trueHeightSide){
                    trueHeightSide = oldtrueHeightSide;
                }
            if(oldtrueHeight<trueHeight){
                trueHeight = oldtrueHeight;
            }if (oldtrueHeight<trueHeight2){
                    trueHeight2 = oldtrueHeight;
                }

                finalHeight = findSmallest(trueHeight,trueHeight2,trueHeightSide,trueHeight2Side);

                calculatedFrontWidth=biggestX.width/pixelsPerMetric.intValue();
                calculatedSideWidth=biggestXSide.width/pixelsPerMetricSide.intValue();
                final_weight = (calculatedSideWidth * -0.306) + (calculatedFrontWidth * 0.0721) + (finalHeight * 0.2606);

                System.out.println("side width: "+(calculatedSideWidth * -0.306) );
                System.out.println("front width: "+ (calculatedFrontWidth * 0.0721));
                System.out.println("final height" + (finalHeight * 0.2606));
                double valueRounded = Math.round(finalHeight * 100D) / 100D;
                double valueRounded2 = Math.round(final_weight * 100D) / 100D;


                tv_height.setText(valueRounded+"cm");
                tv_weight.setText(valueRounded2+"kg");
            }

            private Double findSmallest(Double trueHeight,Double trueHeight2,Double trueHeightSide, Double trueHeightSide2) {


                Double smallest = trueHeight;

                if(trueHeight2<smallest) {
                    smallest = trueHeight2;
                }
                if(trueHeightSide<smallest){
                    smallest=trueHeightSide;
                }
                if(trueHeightSide2<smallest){
                    smallest = trueHeightSide2;
                }

                return smallest;
            }
            private Double difference(Double num1,Double num2){


                return Math.abs(num1-num2);
            }
        });




        btn_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(finalHeight == 0 || final_weight == 0) { //if no height or weight was entered
                    Toast.makeText(getBaseContext(), "No height and weight detected", Toast.LENGTH_LONG).show();
                }
                else{
                    // convert the silhouette from bitmap to byte[]
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp_silhouette.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] silhouette = stream.toByteArray();

                    Intent backIntent = getIntent();
                    backIntent.putExtra("height", finalHeight);
                    backIntent.putExtra("weight", final_weight);
                    backIntent.putExtra("silhouette", silhouette);
                    setResult(RESULT_OK, backIntent);
                    finish();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = getIntent();
                setResult(RESULT_CANCELED, backIntent);
                finish();
            }
        });
    }


    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        n++;
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        addImageToGallery(root + "/saved_images/" + fname, getBaseContext());
        Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void instantiateVariables() {
//        mContext = getBaseContext();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        numberOfTimesCalled = 0;
        mat_targetOrigImage = new Mat();
        mat_adaptedTresholdedImage = new Mat();
//        btn_calibrate = (Button) this.findViewById(R.id.btn_calibrate);
        imageView1 = (ImageView) this.findViewById(R.id.imageView1);
        imageView2 = (ImageView) this.findViewById(R.id.imageView2);
        btn_take_picture1 = (Button) this.findViewById(R.id.btn_take_picture1);
        btn_load_image1 = (Button) this.findViewById(R.id.btn_load_image1);
        btn_take_picture2 = (Button) this.findViewById(R.id.btn_take_picture2);
        btn_load_image2 = (Button) findViewById(R.id.btn_load_image2);
        btn_compute = (Button) findViewById(R.id.btn_compute);
        tv_height = (TextView) findViewById(R.id.tv_height);
        tv_weight = (TextView) findViewById(R.id.tv_weight);

        btn_go_back = (Button) findViewById(R.id.btn_go_back);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST1 && resultCode == Activity.RESULT_OK ||
                requestCode == CAMERA_REQUEST2 && resultCode == Activity.RESULT_OK ||
                requestCode == SELECT_PHOTO1 && resultCode == Activity.RESULT_OK ||
                requestCode == SELECT_PHOTO2 && resultCode == Activity.RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                System.out.println("imageURI: "+data.getData());
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                bmp_targetImage = selectedImage;

                Utils.bitmapToMat(bmp_targetImage, mat_targetOrigImage);
                path1="";
                path2="";
                if(requestCode == CAMERA_REQUEST1 || requestCode == SELECT_PHOTO1){
                    //currentImage = 0;
                    imageView1.setImageBitmap(selectedImage);
                    path1 = getRealPathFromURI(imageUri);
                }
                if(requestCode == CAMERA_REQUEST2 || requestCode == SELECT_PHOTO2){
                    //currentImage = 1;
                    imageView2.setImageBitmap(selectedImage);
                    path2 = getRealPathFromURI(imageUri);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }
    public String getRealPathFromURI(Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }
    private Mat cropImageForBottomY(Mat imageMat,Rect reference,int twentyCropped) {
        Mat newMat = imageMat.clone();

        //FOR COLUMN
        Mat roi = newMat.submat((int) reference.br().y,newMat.rows(), (int) (reference.tl().x+twentyCropped), (int) (reference.br().x+twentyCropped));
        newMat = roi;

        return  newMat;

    }

    private Mat cropImageForBottomY2(Mat imageMat,Rect reference,int twentyCropped) {
        Mat newMat = imageMat.clone();

        int twentyPercent = (int) (newMat.cols() * .20);
        int tenPercent = (int) (newMat.cols()*.10);
        //FOR COLUMN
        Mat roi = newMat.submat((int) reference.br().y,newMat.rows(), (int) tenPercent*6,tenPercent*7);
        newMat = roi;

        return  newMat;

    }

    private static Mat cropImageForTopY(Mat mat_currentImage,Rect reference,int twentyCropped){
        int twentyPercent = (int) (mat_currentImage.cols() * .20);
        int rightSet = (int) (mat_currentImage.cols() - twentyPercent);

        //FOR COLUMN
        Mat roi = mat_currentImage.submat(0,mat_currentImage.rows(), (int) reference.br().x+twentyCropped,rightSet);
        mat_currentImage = roi;
        //FOR ROW
        double uped = mat_currentImage.cols();
        int tenPercent = (int) (mat_currentImage.rows() * .10);
        mat_currentImage.submat(mat_currentImage.rows() - tenPercent, mat_currentImage.rows(), 0, (int) uped).setTo(new Scalar(255, 255, 255));

        return  mat_currentImage;
    }

    private static Rect findTopMostY(Mat mat_currentImage){
        Rect lowestY = null;
        List<MatOfPoint> frontContours = new ArrayList<>();
        Mat mat_topMostY = mat_currentImage.clone();
        Imgproc.findContours(mat_topMostY, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        Double fivePercent = mat_currentImage.rows()*.1;
        try {
            lowestY = Imgproc.boundingRect(frontContours.get(0));
        }catch (IndexOutOfBoundsException error){
            System.out.println("no bounding boxes found");
        }
        if(frontContours.size()>0) {
            for (int j = 0; j < frontContours.size(); j++) {

                Rect r = Imgproc.boundingRect(frontContours.get(j));
                if (r.area() > 300) {
                    //takes the heighest y coordinate, thus the top of the head of the person.
                    if(r.height<fivePercent){
                        //do nothing.
                    }
                    else if (lowestY.tl().y > r.tl().y) {
                        lowestY = r;
                    }
                }
            }
        }
        if(lowestY==null){
        }
        return lowestY;

    }
    private static Rect findTopMostYForBot(Mat mat_currentImage){
        Rect lowestY = null;
        List<MatOfPoint> frontContours = new ArrayList<>();
        Mat mat_topMostY = mat_currentImage.clone();
        Imgproc.findContours(mat_topMostY, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        Double fivePercent = mat_currentImage.rows()*.05;
        if(frontContours.size()>3){
            return lowestY;
        }
        try {
            lowestY = Imgproc.boundingRect(frontContours.get(0));
        }catch (IndexOutOfBoundsException error){
//            System.out.println("no bounding boxes found");
        }
        if(frontContours.size()>0) {
            for (int j = 0; j < frontContours.size(); j++) {
                Rect r = Imgproc.boundingRect(frontContours.get(j));
                if (r.area() > 300) {
                    //takes the heighest y coordinate, thus the top of the head of the person.
                    if(r.height<fivePercent){
                        //do nothing.

//                        System.out.println("too high.");
                    }
                    else if (lowestY.tl().y > r.tl().y) {
                        lowestY = r;
                    }


                }
            }
        }
        if(lowestY==null){
        }
        return lowestY;

    }

    private static Rect findBiggestX(Mat mat_currentImage){
        Rect biggestX = null;
        List<MatOfPoint> frontContours = new ArrayList<>();
        Mat mat_biggestX = mat_currentImage.clone();
        Imgproc.findContours(mat_biggestX, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        biggestX = Imgproc.boundingRect(frontContours.get(0));
        for (int j = 0; j < frontContours.size(); j++) {

//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
            Rect r = Imgproc.boundingRect(frontContours.get(j));
            if(r.area()>300) {
                //takes the heighest y coordinate, thus the top of the head of the person.

                //gets the reference object contour.
                if (biggestX.width < r.width) {
                    biggestX = r;
                }
            }
        }
        return biggestX;
    }

    private Mat getMatPhoto(int currentPhoto){
        Bitmap image =null;
        if(currentPhoto ==1 ){
            image =((BitmapDrawable)imageView1.getDrawable()).getBitmap();
        }else{
            image = ((BitmapDrawable)imageView2.getDrawable()).getBitmap();
        }



        Mat mat = new Mat();
        Utils.bitmapToMat(image,mat);
        return mat;
    }
}
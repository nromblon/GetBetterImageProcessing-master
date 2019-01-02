package ph.edu.dlsu.reanna_lim.imageprocessingmodule;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Test extends AppCompatActivity {

    private static final String TAG = "GetbetterMain::test";
    //declaration shit.
    Button  btn_loadPicture1,
            btn_loadPicture2,
            btn_stopCheck;

    ImageView imageView1,
              imageView2;


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        instantiateVariables();

        btn_loadPicture1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Constants.SELECT_PHOTO1);
            }
        });

        btn_loadPicture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Constants.SELECT_PHOTO2);
            }
        });
        btn_stopCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPhoto = 0;

                Mat mat_startImage = new Mat();
                        getMatPhoto(currentPhoto).copyTo(mat_startImage);
                Mat mat_clonedForBotY =new Mat();
                        getMatPhoto(currentPhoto).copyTo(mat_clonedForBotY);
                Mat mat_clonedForBotY2 =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_clonedForBotY2);
                Mat mat_cloneForTopY =new Mat();
                        getMatPhoto(currentPhoto).copyTo(mat_cloneForTopY);
                Mat mat_cloneForBiggestX =new Mat();
                        getMatPhoto(currentPhoto).copyTo(mat_cloneForBiggestX);



                Double imageResolution = Double.valueOf(mat_startImage.rows());


//**************************************** FIND REFERENCE OBJECT
                int twentyPercent = (int) (mat_startImage.cols() * .20);
                Rect referenceObject = IPUtils.findReferenceObjectRect(mat_startImage,imageView1);
//****************************************** END FIND REFERENCE OBJECT.

//****************************************FIND BOTTOM Y
                Mat thresHoldedY = mat_clonedForBotY.clone();
                thresHoldedY = cropImageForBottomY(thresHoldedY,referenceObject,twentyPercent);
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
                Mat thresHoldedY2 = mat_clonedForBotY2.clone();
                thresHoldedY2 = cropImageForBottomY2(thresHoldedY2,referenceObject,twentyPercent);
                thresHoldedY2= IPUtils.basicThresholdAlgo(thresHoldedY2);

                bottomY2 = findTopMostYForBot(thresHoldedY2);
                thresHoldedY2 = drawBoxes(thresHoldedY2);
//                IPUtils.display(imageView2,thresHoldedY2);
//****************************************END FIND BOTTOM Y

//*****************************************FIND TOPMOST Y

                Mat  mat_topMostY = mat_cloneForTopY;
                mat_topMostY = cropImageForTopY(mat_topMostY,referenceObject,twentyPercent);
                mat_topMostY = IPUtils.basicThresholdAlgo(mat_topMostY);
                Rect topMostY = findTopMostY(mat_topMostY);

                double topMostTlPoints[] = {topMostY.tl().x+referenceObject.br().x+twentyPercent,topMostY.tl().y};
                double topMostBrPoints[] = {topMostY.br().x+referenceObject.br().x+twentyPercent,topMostY.br().y};
                Point p = new Point();
                Point p2 = new Point();
                p.set(topMostTlPoints);
                p2.set(topMostBrPoints);


                double referenceTLPoints[] = {referenceObject.tl().x+twentyPercent,referenceObject.tl().y};
                double referenceBRPoints[] = {referenceObject.br().x+twentyPercent,referenceObject.br().y};

                Point p3 = new Point(referenceTLPoints);
                Point p4 = new Point(referenceBRPoints);


                Bitmap startBM = IPUtils.basicTresholdedForView(mat_startImage) ;
                Mat displayMat = new Mat();
                Utils.bitmapToMat(startBM,displayMat);
                IPUtils.display(imageView2,startBM);
//
// *******************************************END TOPMOST YY


//*******************************************REFORMED ALGORITHM:
                Double foundBottomy,foundBottomy2;

                Double subjectHeightInPixels,subjectHeightInPixels2;

                Boolean feetFound = false;
                Boolean feetFound2= false;
                if(bottomY==null){
                    foundBottomy = imageResolution;
                    subjectHeightInPixels = imageResolution-topMostY.tl().y;
                }else {
                    foundBottomy = bottomY.tl().y;
                    subjectHeightInPixels = (bottomY.tl().y)+referenceObject.br().y-topMostY.tl().y;
                    feetFound=true;
                }
                if(bottomY2==null){
                    foundBottomy2 = imageResolution;
                    subjectHeightInPixels2 = imageResolution-topMostY.tl().y;
                }else {
                    foundBottomy2 = bottomY2.tl().y;
                    subjectHeightInPixels2 = (bottomY2.tl().y)+referenceObject.br().y-topMostY.tl().y;
                    feetFound2=true;
                }
//                System.out.println("bottom y 1"+ bottomY);
//                System.out.println("bottom y 2"+ bottomY2);

                if(feetFound || feetFound2){
                    if(!feetFound){
                        subjectHeightInPixels =99999999.0;
                    }else if(!feetFound2){
                        subjectHeightInPixels2 =99999999.0;
                    }
                }
                Double pixelsPerMetric = referenceObject.height/30.48;
                Double trueHeight = subjectHeightInPixels/pixelsPerMetric;
                Double trueHeight2 = subjectHeightInPixels2/pixelsPerMetric;
                Double oldtrueHeight = (imageResolution-topMostY.tl().y)/pixelsPerMetric;
                System.out.println("FOR side ***************************");
                System.out.println("pixels per metric: "+pixelsPerMetric);
                System.out.println("OLD ALGO TRUE HEIGHT"+oldtrueHeight);
                System.out.println("NEW ALGO TRUE HEIGHT USING TL "+trueHeight);
                System.out.println("NEW ALGO TRUE HEIGHT USING BR "+trueHeight2);

//*********************************************SIDE**********************************************************
            currentPhoto = 1;
                Mat mat_startImageSide = new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_startImageSide);
                Mat mat_clonedForBotYSide =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_clonedForBotYSide);
                Mat mat_clonedForBotY2Side =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_clonedForBotY2Side);
                Mat mat_cloneForTopYSide =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_cloneForTopYSide);
                Mat mat_cloneForBiggestXSide =new Mat();
                getMatPhoto(currentPhoto).copyTo(mat_cloneForBiggestXSide);
                Double imageResolution2 = Double.valueOf(mat_startImageSide.rows());


//**************************************** FIND REFERENCE OBJECT
                twentyPercent = (int) (mat_startImageSide.cols() * .20);
                referenceObject = IPUtils.findReferenceObjectRect(mat_startImageSide,imageView1);
//****************************************** END FIND REFERENCE OBJECT.

//****************************************FIND BOTTOM Y
                Mat thresHoldedYSide = mat_clonedForBotYSide.clone();
                thresHoldedYSide = cropImageForBottomY(thresHoldedYSide,referenceObject,twentyPercent);
                thresHoldedYSide= IPUtils.basicThresholdAlgo(thresHoldedYSide);
                Rect bottomYSide = findTopMostY(thresHoldedYSide);


//                IPUtils.display(imageView2,thresHoldedYSide);
//****************************************END FIND BOTTOM Y


//****************************************FIND SECOND BOTTOM Y
                Rect bottomY2Side=  null;
                Mat thresHoldedY2Side = mat_clonedForBotY2Side.clone();
                thresHoldedY2Side = cropImageForBottomY2(thresHoldedY2Side,referenceObject,twentyPercent);
                thresHoldedY2Side= IPUtils.basicThresholdAlgo(thresHoldedY2Side);

                bottomY2Side = findTopMostYForBot(thresHoldedY2Side);
                thresHoldedY2Side = drawBoxes(thresHoldedY2Side);
//                IPUtils.display(imageView2,thresHoldedY2Side);
//****************************************END FIND BOTTOM Y

//*****************************************FIND TOPMOST Y

                Mat  mat_topMostYSide = mat_cloneForTopYSide;
                mat_topMostYSide = cropImageForTopY(mat_topMostYSide,referenceObject,twentyPercent);
                mat_topMostYSide = IPUtils.basicThresholdAlgo(mat_topMostYSide);
                Rect topMostYSide = findTopMostY(mat_topMostYSide);

                double topMostTlPointsSide[] = {topMostYSide.tl().x+referenceObject.br().x+twentyPercent,topMostYSide.tl().y};
                double topMostBrPointsSide[] = {topMostYSide.br().x+referenceObject.br().x+twentyPercent,topMostYSide.br().y};
                Point pSide = new Point();
                Point p2Side = new Point();
                pSide.set(topMostTlPointsSide);
                p2Side.set(topMostBrPointsSide);


                double referenceTLPointsSide[] = {referenceObject.tl().x+twentyPercent,referenceObject.tl().y};
                double referenceBRPointsSide[] = {referenceObject.br().x+twentyPercent,referenceObject.br().y};

                Point p3Side = new Point(referenceTLPointsSide);
                Point p4Side = new Point(referenceBRPointsSide);
try {
    double referenceTLBottomPointsSide[] = {bottomYSide.tl().x + referenceObject.br().x + twentyPercent, bottomY2Side.tl().y + referenceBRPoints[1]};
    double referenceBRBottomPointsSide[] = {bottomYSide.br().x + referenceObject.br().x + twentyPercent, bottomY2Side.br().y + referenceBRPoints[1]};

    Point p5Side = new Point(referenceTLBottomPointsSide);
    Point p6Side = new Point(referenceBRBottomPointsSide);
    Bitmap bm = IPUtils.basicTresholdedForView(mat_startImageSide);
    IPUtils.display(imageView1, bm);
//    newMat = IPUtils.basicThresholdAlgo(newMat);
//    Imgproc.rectangle(newMat, p5Side, p6Side, new Scalar(255, 125, 152), 3, 8, 0);

//                bottomYSide

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
                    foundBottomy = imageResolution2;
                    subjectHeightInPixels3 = imageResolution2-topMostY.tl().y;
                }else {
                    foundBottomy = bottomYSide.tl().y;
                    subjectHeightInPixels3 = (bottomYSide.tl().y)+referenceObject.br().y-topMostYSide.tl().y;
                    sideFeetFound =true;

                }
                if(bottomY2Side==null){
                    foundBottomy2 = imageResolution2;
                    subjectHeightInPixels4 = imageResolution2-topMostY.tl().y;
                }else {
                    foundBottomy2 = bottomY2Side.tl().y;
                    subjectHeightInPixels4 = (bottomY2Side.tl().y)+referenceObject.br().y-topMostYSide.tl().y;
                    sideFeetFound2 =true;
                }
//                System.out.println("bottom y 1 for side"+ bottomYSide);
//                System.out.println("bottom y 2 for side"+ bottomY2Side);
                if(sideFeetFound || sideFeetFound2){
                    if(!sideFeetFound){
                        subjectHeightInPixels3 =99999999.0;
                    }else if(!sideFeetFound2){
                        subjectHeightInPixels4 =99999999.0;
                    }
                }
                Double pixelsPerMetricSide = referenceObject.height/30.48;
                Double trueHeightSide = subjectHeightInPixels3/pixelsPerMetricSide;
                Double trueHeight2Side = subjectHeightInPixels4/pixelsPerMetricSide;


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


                Double oldtrueHeightSide = (imageResolution2-topMostYSide.tl().y)/pixelsPerMetricSide;
                System.out.println("FOR front*********************************");
                System.out.println("pixels per metric: "+pixelsPerMetricSide);
                System.out.println("OLD ALGO TRUE HEIGHT"+oldtrueHeightSide);
                System.out.println("NEW ALGO TRUE HEIGHT USING TL "+trueHeightSide);
                System.out.println("NEW ALGO TRUE HEIGHT USING BR "+trueHeight2Side);




                Double finalHeight = findSmallest(trueHeight,trueHeight2,trueHeightSide,trueHeight2Side);

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

        });





    }

    private Rect findBottomY(Mat currentImage) {
        Mat newMat = currentImage.clone();
        List<MatOfPoint> frontContours = new ArrayList<>();
        Imgproc.findContours(newMat, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);

        Rect highestY = Imgproc.boundingRect(frontContours.get(0));


        for (int j = 0; j < frontContours.size(); j++) {

//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
            Rect r = Imgproc.boundingRect(frontContours.get(j));
            if(r.area()>200) {
//                System.out.println("area is large enough");
                //takes the heighest y coordinate, thus the top of the head of the person.
                if (highestY.tl().y < r.tl().y) {
                    highestY = r;
                }


            }
        }

//        System.out.println("lowest y is: "+highestY.tl().y);
        return highestY;
    }
    public static Mat cropImageForReference(Mat mat_currentImage,int twentyPercent,int tenPercent) {

        int rightSet = (int) (mat_currentImage.cols() - twentyPercent);


        Mat roi = mat_currentImage.submat(0, mat_currentImage.rows(), twentyPercent, rightSet);
        mat_currentImage = roi;

        double uped = mat_currentImage.cols();
        mat_currentImage.submat(mat_currentImage.rows() - tenPercent, mat_currentImage.rows(), 0, (int) uped).setTo(new Scalar(255, 255, 255));


        double fivePercent = mat_currentImage .rows()*0.05;
        roi = mat_currentImage.submat((int) fivePercent,mat_currentImage.rows(),0,mat_currentImage.cols());

        mat_currentImage =roi;

        return mat_currentImage;
//        Bitmap bm = Bitmap.createBitmap(mat_thresHoldedImage.cols(), mat_thresHoldedImage.rows(),Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat_thresHoldedImage, bm);
//        imageToCalculate = mat_thresHoldedImage;
    }
    private Mat cropImageForBottomY(Mat imageMat,Rect reference,int twentyCropped) {
        Mat newMat = imageMat.clone();

        int twentyPercent = (int) (newMat.cols() * .20);
        int rightSet = (int) (newMat.cols() - reference.br().x);


//        System.out.println("twenty percent is "+twentyPercent);
//        System.out.println("reference object coordinate: "+reference.br().x+twentyCropped);


        //FOR COLUMN
        Mat roi = newMat.submat((int) reference.br().y,newMat.rows(), (int) (reference.tl().x+twentyCropped), (int) (reference.br().x+twentyCropped));
        newMat = roi;

        return  newMat;

    }

    private Mat cropImageForBottomY2(Mat imageMat,Rect reference,int twentyCropped) {
        Mat newMat = imageMat.clone();

        int twentyPercent = (int) (newMat.cols() * .20);
        int tenPercent = (int) (newMat.cols()*.10);
        int rightSet = (int) (newMat.cols() -twentyPercent);
        int twentyFivePercent = newMat.cols()/4;

//        System.out.println("twenty percent is "+twentyPercent);
//        System.out.println("reference object coordinate: "+reference.br().x+twentyCropped);


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


    private Rect findReferenceObject(Mat imageMat){
        List<MatOfPoint> contours= new ArrayList<>();
        double ref=-1;

        //find left most bounding box.
        Imgproc.findContours(imageMat, contours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        Rect referenceObject = Imgproc.boundingRect(contours.get(0));
        for (int j = 0; j < contours.size(); j++) {

//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
            Rect r = Imgproc.boundingRect(contours.get(j));
            if(r.area()>300) {
                //takes the heighest y coordinate, thus the top of the head of the person.

                //gets the reference object contour.
                if (referenceObject.tl().x > r.tl().x) {
                    referenceObject = r;
                }
            }
        }

        if (Constants.HeightOrWidth.equals(Constants.HEIGHT)) {
//            System.out.println("getting the hieght");
            ref  = referenceObject.height;
        }else{
//            System.out.println("getting the width");
            ref = referenceObject.width;
        }

//            System.out.println("ref got: "+ref);
            return  referenceObject;

    }


    private static Rect findTopMostY(Mat mat_currentImage){
        Rect lowestY = null;
        List<MatOfPoint> frontContours = new ArrayList<>();
        Mat mat_topMostY = mat_currentImage.clone();
        Imgproc.findContours(mat_topMostY, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        Double fivePercent = mat_currentImage.rows()*.1;
//        if(frontContours.size()>3){
//            return lowestY;
//        }
        try {
            lowestY = Imgproc.boundingRect(frontContours.get(0));
        }catch (IndexOutOfBoundsException error){
            System.out.println("no bounding boxes found");
        }
        if(frontContours.size()>0) {
            for (int j = 0; j < frontContours.size(); j++) {

//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
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
//            System.out.println("TOP y is: "+lowestY.tl().y);
        }
        if(lowestY==null){
//            System.out.println("lowest y is null. you must use next lowest coordinate.");
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

//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
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
//            System.out.println("TOP y is: "+lowestY.tl().y);
        }
        if(lowestY==null){
//            System.out.println("lowest y is null. you must use next lowest coordinate.");
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
//        System.out.println("biggest x width:"+biggestX);
return biggestX;
    }

    private Mat drawBoxes(Mat Image){
        Mat contourFrame = Image;

        List<MatOfPoint> frontContours = new ArrayList<>();

        Imgproc.findContours(contourFrame, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        Rect[] boundingBoxes =new Rect[frontContours.size()];
        for(int i=0;i<frontContours.size();i++){
        boundingBoxes[i] = Imgproc.boundingRect(frontContours.get(i));
            Imgproc.rectangle(contourFrame, boundingBoxes[i].tl(), boundingBoxes[i].br(), new Scalar(255, 125, 152), 3, 8, 0);
        }
        return contourFrame;


    }
    private void testShit(Mat image){
        List<MatOfPoint> frontContours = new ArrayList<>();
        Imgproc.findContours(image, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);



        Rect lowestRect = Imgproc.boundingRect(frontContours.get(0));
        double biggestY;


        for (int j = 0; j < frontContours.size(); j++) {

            Rect r = Imgproc.boundingRect(frontContours.get(j));
            if(r.area()<1000){
                //discard
            }
            else{
                if(lowestRect.tl().x>r.tl().x){
                    lowestRect = r;
                }
            }
        }




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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (    requestCode == Constants.SELECT_PHOTO1&& resultCode == Activity.RESULT_OK ||
                requestCode == Constants.SELECT_PHOTO2&& resultCode == Activity.RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                if(requestCode == Constants.SELECT_PHOTO1){
                    //currentImage = 0;
                    imageView1.setImageBitmap(selectedImage);
                 }
                if(requestCode == Constants.SELECT_PHOTO2){
                    //currentImage = 1;
                    imageView2.setImageBitmap(selectedImage);
                  }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }



    private void instantiateVariables(){
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        btn_loadPicture1 = (Button) findViewById(R.id.btn_load_image1);
        btn_loadPicture2 = (Button) findViewById(R.id.btn_load_image2);
        btn_stopCheck = (Button) findViewById(R.id.btn_stopCheck);

        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

    }
}

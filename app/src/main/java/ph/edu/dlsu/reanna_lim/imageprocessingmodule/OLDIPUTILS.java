package ph.edu.dlsu.reanna_lim.imageprocessingmodule;

import android.graphics.Bitmap;
import android.media.Image;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicky on 11/19/2017.
 */

public class OLDIPUTILS {


    //crops the image a certain percentage via cols, rows below are painted white.
    public static Mat cropImage(Mat mat_currentImage) {
        int twentyPercent = (int) (mat_currentImage.cols() * .20);
        int rightSet = (int) (mat_currentImage.cols() - twentyPercent);


        int tenPercent = (int) (mat_currentImage.rows() * .10);
        Mat roi = mat_currentImage.submat(0, mat_currentImage.rows(), twentyPercent, rightSet);
        mat_currentImage = roi;

        double uped = mat_currentImage.cols();
        mat_currentImage.submat(mat_currentImage.rows() - tenPercent, mat_currentImage.rows(), 0, (int) uped).setTo(new Scalar(255, 255, 255));

        return mat_currentImage;
//        Bitmap bm = Bitmap.createBitmap(mat_thresHoldedImage.cols(), mat_thresHoldedImage.rows(),Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat_thresHoldedImage, bm);
//        imageToCalculate = mat_thresHoldedImage;
    }

    public static Bitmap basicTresholdedForView(Mat ImageMat) {
        Mat newMat = new Mat();
        newMat = ImageMat;
        //CONVERT to gray to be saved.
        int thresh_type = Imgproc.THRESH_BINARY;
        Imgproc.cvtColor(newMat, newMat, Imgproc.COLOR_BGR2GRAY);
        Mat display = new Mat();
        display = newMat;
        Imgproc.threshold(display, display, 100, 155, thresh_type);
        Bitmap bm = Bitmap.createBitmap(display.cols(), display.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(display, bm);
        return bm;

    }

    public static Mat basicThresholdAlgo(Mat ImageMat) {
        Mat newMat  = new Mat();
        newMat = ImageMat;
//        Bitmap bm = basicTresholdedForView(newMat);
        Mat imageToThreshold = ImageMat;
        //Utils.bitmapToMat(bm, imageToThreshold);

        //blur slghtly, reapply threshold.
        int thresh_type = Imgproc.THRESH_BINARY;
        org.opencv.core.Size s = new Size(7, 7);
        Imgproc.GaussianBlur(imageToThreshold, imageToThreshold, s, 0);
        Imgproc.threshold(imageToThreshold, imageToThreshold, 20, 155, thresh_type);

//        //perform edge detection, then perform a dilation + erosion to# close gaps in between object edges
        Imgproc.Canny(imageToThreshold, imageToThreshold, 50, 100);
        Imgproc.dilate(imageToThreshold, imageToThreshold, Imgproc.getStructuringElement(Imgproc.CHAIN_APPROX_NONE, new Size(2, 2)));
        Imgproc.erode(imageToThreshold, imageToThreshold, Imgproc.getStructuringElement(Imgproc.CHAIN_APPROX_NONE, new Size(2, 2)));

//        Imgproc.RETR_EXTERNAL
        return imageToThreshold;
    }


    public static HWBiometrics HeightAndWeightAlgorithm(double realWorldHeight,double lowestCoordinate1,double lowestCoordinate2,Mat frontImage,Mat sideImage){
        List<MatOfPoint> frontContours = new ArrayList<>();
        List<MatOfPoint> sideContours = new ArrayList<>();

        //find contours in the edge map
        Imgproc.findContours(frontImage, frontContours , new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        Imgproc.findContours(sideImage, sideContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);


        double pixelsPerMetric;
        double[] numbers =  getReferenceAndSubjectHeightAndBiggestWidth(frontContours);
        double referenceObject = numbers[0];

        pixelsPerMetric = referenceObject / realWorldHeight;
        System.out.println("pixels per metric for front: "+pixelsPerMetric);
        double subjectToCalculate = numbers[1];
        System.out.println("lowest coordinate: "+lowestCoordinate1);

        double frontWidth = numbers[2];
        System.out.println("front width: "+frontWidth);
        double trueHeight = (lowestCoordinate1 - subjectToCalculate) / pixelsPerMetric;
        System.out.println("true height for front: "+trueHeight);
        double final_height = trueHeight;
        pixelsPerMetric = -1;
        numbers =  getReferenceAndSubjectHeightAndBiggestWidth(sideContours);
        referenceObject = numbers[0];
        pixelsPerMetric = referenceObject /realWorldHeight;
        System.out.println("pixels per metric for side: "+pixelsPerMetric);
        subjectToCalculate = numbers[1];
        double sideWidth = numbers[2];
        System.out.println("side width: "+sideWidth);
        trueHeight = (lowestCoordinate2 - subjectToCalculate) / pixelsPerMetric;
        System.out.println("true height for side: "+trueHeight);




        double calculatedFrontWidth = frontWidth/pixelsPerMetric;
        double calculatedSideWidth = sideWidth/pixelsPerMetric;
        double trueWeight = (int) ((int) calculatedFrontWidth* 0.0618 + (int) calculatedSideWidth* 0.1497 + final_height * 0.3132 - 22.1014);
        HWBiometrics hwBiometrics= new HWBiometrics();
        final_height-=10;
        hwBiometrics.setHeight(final_height);
        hwBiometrics.setWeight(trueWeight);

        return  hwBiometrics;
    }
    //
    /*
    0- reference object
    1- subject 'height'
    2- width
     */
    private static double[] getReferenceAndSubjectHeightAndBiggestWidth(List<MatOfPoint> contours){

        Rect[] boundingBoxes = new Rect[contours.size()];
//        System.out.println("counter numbers" + contours.size());
        double lowestY = 99999;
        Rect referenceObject = Imgproc.boundingRect(contours.get(0));
        double ref=-1;
        double biggestWidth = referenceObject.width;
        for (int j = 0; j < contours.size(); j++) {

//            if(Imgproc.contourArea(contours.get(j))>modifier){//if area is not large enough, ignore it.
            Rect r = Imgproc.boundingRect(contours.get(j));

            //takes the heighest y coordinate, thus the top of the head of the person.
            if(lowestY > r.tl().y){
                lowestY= r.tl().y;
            }

            //gets the reference object contour.
            if(referenceObject.tl().x>r.tl().x){
                referenceObject = r;
            }
            if(biggestWidth<r.width){
                biggestWidth=r.width;
            }
        }

        if (Constants.HeightOrWidth.equals(Constants.HEIGHT)) {
//            System.out.println("getting the hieght");
            ref  = referenceObject.height;
        }else{
//            System.out.println("getting the width");
            ref = referenceObject.width;
        }
        System.out.println("biggest width: "+biggestWidth);
        double[] numbers = {ref,lowestY,biggestWidth};
        return numbers;
    }




//    private Rect[] getReferenceAndSubjectBoundingBox(List<MatOfPoint> contours){
//        Rect referenceObject;
//        double lowestY =contours
//
//
//
//
//        for (int i = 0; i < contours.size(); i++) {
//            if (lowestY > boundingBoxes[i].tl().y) {
//
//                highestBoundBox = boundingBoxes[i];
//                lowestY = highestBoundBox.tl().y;
//            }
//        }
//        return referenceObject;
//    }

}

//
// Created by chenxiaoxuan1 on 17/3/30.
//

#include "com_aaron_indoorpanorama_util_opencv_ImagePro.h"
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/stitching.hpp>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;
using namespace std;

JNIEXPORT jint JNICALL Java_com_aaron_indoorpanorama_util_opencv_ImagePro_stitch
        (JNIEnv *env, jobject, jstring left_, jstring right_) {
    double fScale = 0.5;
    const char *left = env->GetStringUTFChars(left_, 0);
    const char *right = env->GetStringUTFChars(right_, 0);
    String result_name("/sdcard/result/result.jpg");

    int length = 2;
    String imageArr[length];
    imageArr[0] = String(left);
    imageArr[1] = String(right);
    env->ReleaseStringUTFChars(left_, left);
    env->ReleaseStringUTFChars(right_, right);

    vector<Mat> imgs;
    Mat pic;
    for (int i = 0; i < length; i++) {
        pic = imread(imageArr[i]);
        Size dsize = Size(pic.cols * fScale, pic.rows * fScale);
        Mat pic2(dsize, CV_32S);
        resize(pic, pic2, dsize);
        imgs.push_back(pic2);
    }

    Mat pano;
    Stitcher stitcher = Stitcher::createDefault(true);
//    detail::OrbFeaturesFinder orbFeaturesFinder;
//    Ptr<detail::OrbFeaturesFinder> ptr(&orbFeaturesFinder);
//    stitcher.setFeaturesFinder(ptr);
    stitcher.setRegistrationResol(0.5);
    Stitcher::Status status = stitcher.stitch(imgs, pano);
    if (status != Stitcher::OK) {
        const char *statusStr[4];
        statusStr[0] = "OK";
        statusStr[1] = "ERR_NEED_MORE_IMGS";
        statusStr[2] = "ERR_HOMOGRAPHY_EST_FAIL";
        statusStr[3] = "ERR_CAMERA_PARAMS_ADJUST_FAIL";
        int sInt = status;
//        const char *s = "Can't stitch images, error code = " + *statusStr[sInt];
        __android_log_print(ANDROID_LOG_ERROR, "ImagePro_stitch", statusStr[sInt]);
        return 2;
    }
    __android_log_print(ANDROID_LOG_ERROR, "ImagePro_stitch", "success !! ");
    remove(result_name.c_str());
    imwrite(result_name, pano);

    return 1;
}
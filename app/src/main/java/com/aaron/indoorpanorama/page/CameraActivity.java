package com.aaron.indoorpanorama.page;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.aaron.indoorpanorama.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by chenxiaoxuan1 on 17/3/21.
 */

public class CameraActivity extends Activity {
    private static final int TAKE_PHOTO_LOOP = 1001;
    private static final int TAKE_PHOTO_DELAY = 500;

    private View layout;
    private Camera mCamera;
    private Camera.Parameters parameters = null;
    private boolean mIsLoop;

    Bundle bundle = null; // 声明一个Bundle对象，用来存储数据

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示界面
        setContentView(R.layout.activity_camera);

        layout = this.findViewById(R.id.buttonLayout);

        SurfaceView surfaceView = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        surfaceView.getHolder()
                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(176, 144);    //设置Surface分辨率
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数
    }

    /**
     * 按钮被点击触发的事件
     *
     * @param v
     */
    public void btnOnclick(View v) {
        if (mCamera != null) {
            switch (v.getId()) {
                case R.id.takepicture:
                    // 拍照
                    mCamera.takePicture(null, null, new MyPictureCallback());
                    break;
                case R.id.btn_start_work:
                    mIsLoop = !mIsLoop;
                    if (mIsLoop) {
                        mHandler.sendEmptyMessage(TAKE_PHOTO_LOOP);
                    }
                    break;
            }
        }
    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
//                bundle = new Bundle();
//                bundle.putByteArray("bytes", data);	//将图片字节数据保存在bundle当中，实现数据交换
                saveToSDCard(getPreviewDegree(CameraActivity.this), data); // 保存图片到sd卡中
//                Toast.makeText(getApplicationContext(), R.string.success,
//                        Toast.LENGTH_SHORT).show();
                camera.startPreview(); // 拍完照后，重新开始预览

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param data
     * @throws IOException
     */
    public static void saveToSDCard(int degree, byte[] data) throws IOException {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        String filename = format.format(date) + ".jpg";
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + "/finger/");
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
            fileFolder.mkdir();
        }
        File jpgFile = new File(fileFolder, filename);
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流

        Bitmap bm = BitmapFactory.decodeByteArray
                (data, 0, data.length);
        Bitmap newBitmap = rotaingImageView(degree, bm);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);

         /* 采用压缩转档方法 */
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.write(data); // 写入sd卡中
        bos.close(); // 关闭输出流
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }


    private final class SurfaceCallback implements Callback {

        // 拍照状态变化时调用该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            parameters = mCamera.getParameters(); // 获取各项参数
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
            parameters.setPreviewSize(width, height); // 设置预览大小
            parameters.setPreviewFrameRate(5);    //设置每秒显示4帧
            parameters.setPictureSize(width, height); // 设置保存的图片尺寸
            parameters.setJpegQuality(80); // 设置照片质量
        }

        // 开始拍照时调用该方法
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera = Camera.open(); // 打开摄像头
                mCamera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                mCamera.setDisplayOrientation(getPreviewDegree(CameraActivity.this));
                mCamera.startPreview(); // 开始预览
                setPictureSize();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 停止拍照时调用该方法
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.release(); // 释放照相机
                mCamera = null;
            }
        }
    }

    /**
     * 点击手机屏幕是，显示两个按钮
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                layout.setVisibility(ViewGroup.VISIBLE); // 设置视图可见
                break;
        }
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
                if (mCamera != null && event.getRepeatCount() == 0) {
                    // 拍照
                    //注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
                    //，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
                    mCamera.takePicture(null, null, new MyPictureCallback());
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    /**
     * 设置拍照的图片为支持尺寸的最小项
     */
    private void setPictureSize() {
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        Camera.Size size = list.get(0);
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
    }

    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case TAKE_PHOTO_LOOP:
                    if (mIsLoop) {
                        mCamera.takePicture(null, null, new MyPictureCallback());
                        mHandler.sendEmptyMessageDelayed(TAKE_PHOTO_LOOP, TAKE_PHOTO_DELAY);
                    }
                    break;
            }
        }
    };
}
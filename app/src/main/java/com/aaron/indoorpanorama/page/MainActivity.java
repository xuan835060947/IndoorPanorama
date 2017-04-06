package com.aaron.indoorpanorama.page;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aaron.indoorpanorama.R;
import com.aaron.indoorpanorama.util.opencv.ImagePro;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements View.OnClickListener {
    private String TAG = MainActivity.class.getSimpleName();
    private static final int OK = 1;

    static {
        System.loadLibrary("image-proc");
    }

    private EditText mEtLeft;
    private EditText mEtRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start_work).setOnClickListener(this);
        findViewById(R.id.btn_open_result).setOnClickListener(this);
        mEtLeft = (EditText) findViewById(R.id.et_left);
        mEtRight = (EditText) findViewById(R.id.et_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_work:
                final String left = mEtLeft.getText().toString();
                final String right = mEtRight.getText().toString();
                Log.e(TAG, left + "  " + right);

                Observable.create(new Observable.OnSubscribe<List<String>>() {

                    @Override
                    public void call(Subscriber<? super List<String>> subscriber) {
                        List<String> list = new ArrayList<String>();
                        list.add(left);
                        list.add(right);
                        subscriber.onNext(list);
                    }
                })
                        .observeOn(Schedulers.computation())
                        .map(new Func1<List<String>, Integer>() {
                            @Override
                            public Integer call(List<String> strings) {
                                Log.e(TAG, " stitch start ! !");
                                return new ImagePro().stitch(strings.get(0), strings.get(1));
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                Log.e(TAG, "  status " + integer);
                                if (integer != null && integer == OK) {
                                    Toast.makeText(MainActivity.this, "成功 !! ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                break;
            case R.id.btn_open_result:
                openImgResult();
                break;
        }
    }

    private void openImgResult() {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/result/result.jpg";
        Log.e(TAG, " " + filePath);
        File file = new File(filePath);
        if (file != null && file.isFile() == true) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            this.startActivity(intent);
        }
    }
}

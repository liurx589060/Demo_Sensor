package com.example.liurunxiong.demo_sensor;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private MySurfaceView mySurfaceView;
    private Button mStartBtn;
    private ScrollView mVerticalScrollView;
    private HorizontalScrollView mHorizontalScrollView;

    private TextView mResultTextView;
    private Button mGpsBtn;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long preTime = -1;

    private long vec_x = 0;
    private long vec_y = 0;
    private long vec_z = 0;
//    private float[] XY = {0,0};
    private Boolean mIsMove = true;

    private float preAngleX = -1;
    private float currentAngleX;
    private float initAngleX = -1;
    private float deltaAngleX;
    private float tempAngleX;
    private final float TRANS_ANGLE = 70;

    private final int INIT_SPEED = 200;
    private boolean isDrawing;

    private ArrayList<float[]> mTransPoint;
    private ArrayList<ArrayList<float[]>> mAllSectionList;
    private ArrayList<float[]> mAllPoint;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTransPoint = new ArrayList<>();
        mAllSectionList = new ArrayList<>();
        mAllPoint = new ArrayList<>();
        float[] xy = {0,0};
        mAllPoint.add(xy);
        mTransPoint.add(xy);

        mGpsBtn = (Button) findViewById(R.id.gps_btn);
        mGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyGpsLocationActivity.class);
                startActivity(intent);
            }
        });

        mStartBtn = (Button) findViewById(R.id.start);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDrawing = !isDrawing;
                mySurfaceView.setIsDrawing(isDrawing);
                if(isDrawing) {
                    mStartBtn.setText("Pause");
                    if(mAllSectionList.size() == 0) {
                        ArrayList<float[]> list = new ArrayList<float[]>();
                        list.add(mAllPoint.get(0));
                        mAllSectionList.add(list);
                    }
                }else {
                    mStartBtn.setText("Start");
                }
            }
        });
        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scrollView);
        mVerticalScrollView = (ScrollView) findViewById(R.id.vertical_scrollView);

        mResultTextView = (TextView) findViewById(R.id.result);
        mySurfaceView = (MySurfaceView) findViewById(R.id.surfaceView);
//        mySurfaceView.setZOrderOnTop(true);//设置画布  背景透明
//        mySurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mySurfaceView.setTopScrollView(mVerticalScrollView,mHorizontalScrollView);
        mySurfaceView.setData(mAllPoint,mTransPoint,mAllSectionList);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);// TYPE_GRAVITY
        if (null == mSensorManager) {
            Log.e("yy", "deveice not support SensorManager");
        }
        // 参数三，检测的精准度
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME

        //注册陀螺仪
        Sensor mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscope,
                SensorManager.SENSOR_DELAY_NORMAL);

        //注册陀螺仪
        Sensor mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mOrientation,
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mySurfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(!isDrawing) {
            preTime = System.currentTimeMillis();
            return;
        }

        mIsMove = true;

        if(preTime == -1) {
            preTime = System.currentTimeMillis();
        }

        long timeDelata = System.currentTimeMillis() - preTime;

        StringBuilder builder = new StringBuilder();
        float limit = 0.5f;
        float acc_x = Math.abs(event.values[0]) < limit?0:event.values[0];
        float acc_y = Math.abs(event.values[1]) < limit?0:event.values[1];
        float acc_z = Math.abs(event.values[2]) < limit?0:event.values[2];
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            builder.append("x=" + acc_x);
            builder.append("\n");

            builder.append("y=" + acc_y);
            builder.append("\n");

            builder.append("z=" + acc_z);
            builder.append("\n");

            builder.append("时间差=" + timeDelata);
            builder.append("\n");


            builder.append("速度--X=" + vec_x );
            builder.append("\n");

            builder.append("速度--Y=" + vec_y );
            builder.append("\n");

            builder.append("速度--Z=" + vec_z );
            builder.append("\n");
            mResultTextView.setText(builder.toString());

        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float limit2 = 0.1f;
            if(Math.abs(event.values[0]) < limit2 && Math.abs(event.values[1]) < limit2 && Math.abs(event.values[2]) < limit2) {
                mIsMove = false;
            }

            mResultTextView.setText(mResultTextView.getText().toString() + "" + mIsMove + "\n");
        }

        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            if(preAngleX == -1) {
                preAngleX = event.values[0];
            }

            if(initAngleX == -1) {
                initAngleX = event.values[0];
                tempAngleX = initAngleX;
            }

            currentAngleX = event.values[0];
            deltaAngleX = currentAngleX - initAngleX;
            preAngleX = currentAngleX;
            mResultTextView.setText(mResultTextView.getText().toString() + "角度改变=" + deltaAngleX + "\n"
                              + "初始角度=" + initAngleX + "\n" + "目前角度=" + currentAngleX + "\n");
        }

        preTime = System.currentTimeMillis();

        float dis = INIT_SPEED * timeDelata * 1.5f / 10000.0f;
        float[] xy = mAllPoint.get(index);
        float[] XY = {0,0};
        XY[0] = xy[0] + (float) (dis * Math.cos(Math.toRadians(90 - deltaAngleX)));
        XY[1] = xy[1] + (float) (dis * Math.sin(Math.toRadians(90 - deltaAngleX)));
        mAllPoint.add(XY);
        mResultTextView.setText(mResultTextView.getText().toString() + "位移X=" + XY[0] + "\n"
                + "位移Y=" + XY[1] + "\n");

        float tempDeltaAngleX = currentAngleX - tempAngleX;
        if(Math.abs(tempDeltaAngleX) > TRANS_ANGLE) {
            tempAngleX = currentAngleX;
            mTransPoint.add(XY);

            ArrayList<float[]> list = new ArrayList<float[]>();
            mAllSectionList.add(list);
        }

        if(!mAllSectionList.isEmpty()) {
            ArrayList<float[]> list = mAllSectionList.get(mAllSectionList.size() - 1);
            list.add(XY);
        }
        mySurfaceView.setData(mAllPoint,mTransPoint,mAllSectionList);

        index ++;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySurfaceView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
}

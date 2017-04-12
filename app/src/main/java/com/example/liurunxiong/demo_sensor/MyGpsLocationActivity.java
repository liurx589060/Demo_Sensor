package com.example.liurunxiong.demo_sensor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * Created by liurunxiong on 2017/4/10.
 */

public class MyGpsLocationActivity extends AppCompatActivity implements AMapLocationListener{
    private MySurfaceView mySurfaceView;

    private TextView mResultTextView;

//    private LocationManager locationManager;
//    private LocationListener mListener;

    public AMapLocationClientOption mLocationOption = null;
    public AMapLocationClient mlocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        init();
    }

    private void init() {
        mySurfaceView = (MySurfaceView) findViewById(R.id.surfaceView);
        mySurfaceView.setZOrderOnTop(true);//设置画布  背景透明
        mySurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mResultTextView = (TextView) findViewById(R.id.result);

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        // 从GPS获取最近的定位信息
//
//        mListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                updatetext(location);
//                Log.e("yy","" + location.getLatitude());
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//        };

        mlocationClient = new AMapLocationClient(this);
         //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
       //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
       //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
       //启动定位
        mlocationClient.startLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 8, mListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        locationManager.removeUpdates(mListener);
    }

    private void updatetext(AMapLocation location) {
        if(location == null) {
            mResultTextView.setText("location = null");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("纬度=" + location.getLatitude())
                .append("\n")
                .append("经度=" + location.getLongitude())
                .append("\n")
                .append("速度=" + location.getSpeed())
                .append("\n")
                .append("当前时间=" + location.getTime())
                .append("\n");
        mResultTextView.setText(builder.toString());
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        updatetext(aMapLocation);
    }
}

package com.example.liurunxiong.demo_sensor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by liurunxiong on 2017/4/10.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private ScrollView mTopScrollView;
    private HorizontalScrollView mHorizontalScrollView;

    public final int TRANS_POINT_RADIUS = 18;
    public final int NORMAL_POINT_RADIUS = 6;

    private int originOffsetX;
    private int originOffsetY;

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private MyDrawThread mDrawThread;

    private Paint mPaint;
    private Paint mTransPaint;
    private Paint mSelectedSecPaint;
    private float mX;
    private float mY;

    private ArrayList<float[]> mAllPoint;

    private boolean isCreate;
    private boolean isDrawing;

    private ArrayList<float[]> mTransPoint;
    private ArrayList<ArrayList<float[]>> mAllSectionList;

    private boolean isTransPoint;
    private int mCurrentSection;
    private int mSelectedSection = -1;
    private int mTempSelectedSection = -1;

    private Canvas bakCanvas;
    private Bitmap bak;

    private final int BORDER_SIZE = 50;

    private Matrix mMatrix;

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setData(ArrayList<float[]> mAllPoint,ArrayList<float[]> mTransPoint,ArrayList<ArrayList<float[]>> mAllSectionList) {
        this.mAllPoint = mAllPoint;
        this.mTransPoint = mTransPoint;
        this.mAllSectionList = mAllSectionList;

//        for (int i = 0 ; i < mTransPoint.size() ; i ++) {
//            Log.e("yy","" + i + "=" + mTransPoint.get(i)[0] + "------" + mTransPoint.get(i)[1]);
//        }
    }

    public void setTopScrollView(ScrollView scrollView, HorizontalScrollView horizontalScroll) {
        this.mTopScrollView = scrollView;
        this.mHorizontalScrollView = horizontalScroll;
        this.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = MySurfaceView.this.getLayoutParams();
                params.width = mTopScrollView.getWidth();
                params.height = mTopScrollView.getHeight();
                MySurfaceView.this.setLayoutParams(params);

                mX = mTopScrollView.getWidth() / 2;
                mY = mTopScrollView.getHeight() / 2;
            }
        });
    }

    public void listenViewRefresh() {
        MySurfaceView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                isDrawing = true;
                isCreate = true;
                MySurfaceView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                bak = Bitmap.createBitmap(MySurfaceView.this.getWidth(), MySurfaceView.this.getHeight(), Bitmap.Config.ARGB_4444);
                bakCanvas = new Canvas(bak);

                int maxRadius = Math.max(NORMAL_POINT_RADIUS,TRANS_POINT_RADIUS);
                mX += originOffsetX > 0 ? originOffsetX - maxRadius : 0;
                mY += originOffsetY > 0 ? originOffsetY - maxRadius : 0;;

                if(originOffsetX < 0) {
                    mHorizontalScrollView.scrollBy(Math.abs(originOffsetX),0);
                }

                if(originOffsetY < 0) {
                    mTopScrollView.scrollBy(0,Math.abs(originOffsetY));
                }
            }
        });
    }

    public void setIsDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(null == mDrawThread) {
            mDrawThread = new MyDrawThread();
            mDrawThread.start();
        }
        isCreate = true;
        mTempSelectedSection = mSelectedSection;
    }

    private void init() {
        mHolder = this.getHolder();
        mHolder.addCallback(this);

        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mTransPaint = new Paint();
        mTransPaint.setColor(Color.RED);
        mTransPaint.setStrokeWidth(5);
        mTransPaint.setStyle(Paint.Style.FILL);
        mTransPaint.setAntiAlias(true);

        mSelectedSecPaint = new Paint();
        mSelectedSecPaint.setColor(Color.GRAY);
        mSelectedSecPaint.setStrokeWidth(5);
        mSelectedSecPaint.setStyle(Paint.Style.FILL);
        mSelectedSecPaint.setAntiAlias(true);

        this.post(new Runnable() {
            @Override
            public void run() {
                mX = getWidth() / 2;
                mY = getHeight() / 2;
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(null != mDrawThread) {
            mDrawThread.stopThread();
            mDrawThread = null;
        }

        isCreate = false;
    }

    private void drawPath(final Canvas canvas) {
        if(canvas == null) {
            return;
        }

        float[] xy = {0,0};
        float[] realPosition = {0,0};
        if(isCreate || mTempSelectedSection != -1) {
//        if(true) {
            //清屏
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            for (int i = 0 ; i < mAllSectionList.size(); i++) {
                ArrayList<float[]> list = mAllSectionList.get(i);
                for (int j = 0 ; j < list.size() ; j++) {
                    xy = list.get(j);
                    if(mTempSelectedSection == i) {
                        drawPoint(canvas,xy,true);
                    }else {
                        drawPoint(canvas,xy,false);
                    }
                }
            }
            isCreate =false;
            mTempSelectedSection = -1;
        }else {
            xy = mAllPoint.get(mAllPoint.size() - 1);
            drawPoint(canvas,xy,false);
        }


        realPosition[0] = mX + xy[0];
        realPosition[1] = mY - xy[1];

        originOffsetX = 0;
        originOffsetY = 0;
        if(realPosition[0] < BORDER_SIZE) {
            originOffsetX = mTopScrollView.getWidth() / 2;
        }

        if (realPosition[0] > this.getWidth() - BORDER_SIZE) {
            originOffsetX = -mTopScrollView.getWidth() / 2;
        }

        if (realPosition[1] < BORDER_SIZE) {
            originOffsetY = mTopScrollView.getHeight() / 2;
        }

        if(realPosition[1] > this.getHeight() - BORDER_SIZE) {
            originOffsetY = -mTopScrollView.getHeight() / 2;
        }

        final int extraX = Math.abs(originOffsetX);
        final int extraY = Math.abs(originOffsetY);

        if(originOffsetX != 0 || originOffsetY != 0) {
            isDrawing = false;
            MySurfaceView.this.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) MySurfaceView.this.getLayoutParams();
                    params.width += extraX;
                    params.height += extraY;
                    MySurfaceView.this.setLayoutParams(params);

                    listenViewRefresh();
//                    isCreate = true;
//                    isDrawing = true;
//                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }
            });
        }
    }

    public void drawPoint(Canvas canvas,float[] xy,boolean isDrawSelect) {
        isTransPoint =false;
        for (int i = 0 ; i < mTransPoint.size() ; i++) {
            if(xy == mTransPoint.get(i)) {
                isTransPoint = true;
                mCurrentSection = i;
                break;
            }
        }

        if(isTransPoint) {
            canvas.drawCircle(mX + xy[0],mY - xy[1],TRANS_POINT_RADIUS,mTransPaint);
        }else if (isDrawSelect) {
            canvas.drawCircle(mX + xy[0],mY - xy[1],NORMAL_POINT_RADIUS,mSelectedSecPaint);
        }else {
            canvas.drawCircle(mX + xy[0],mY - xy[1],NORMAL_POINT_RADIUS,mPaint);
        }
    }

    private class MyDrawThread extends Thread {

        public boolean isRunning = false;

        public MyDrawThread() {
            isRunning = true;
            bak = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
            bakCanvas = new Canvas(bak);
        }

        @Override
        public void run() {
            super.run();
            while (isRunning) {
                if(!isDrawing && !isCreate) {
                    continue;
                }

                try {
                    synchronized (mHolder) {
                        if(isRunning) {
                            mCanvas = mHolder.lockCanvas();
                            if(mCanvas != null) {
                                drawPath(bakCanvas);
                                mCanvas.drawBitmap(bak,0,0,null);
                            }
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(mHolder != null && isRunning && mCanvas != null) {
                        mHolder.unlockCanvasAndPost(mCanvas);
                    }
                }

//                try {
//                    Thread.sleep(50);
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }

        public void stopThread() {
            isRunning = false;
//            boolean workIsNotFinish = true;
//            while (workIsNotFinish) {
//                try {
//                    this.join();// 保证run方法执行完毕
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                workIsNotFinish = false;
//            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if(mTransPoint == null) {
            return super.onTouchEvent(event);
        }

        for (int i = 0 ; i < mTransPoint.size() ; i++) {
            float[] xy = mTransPoint.get(i);
            int[] point = {0,0};
            point[0] = (int) (mX + xy[0]);
            point[1] = (int) (mY - xy[1]);
            int extra = 5 * TRANS_POINT_RADIUS / 2;
            Rect rect = new Rect(point[0] - extra ,point[1] - extra,point[0] + extra,point[1] + extra);

            if(rect.contains(x,y)) {
                mSelectedSection = i;
                mTempSelectedSection = i;

                try {
                    synchronized (mHolder) {
                        mCanvas = mHolder.lockCanvas();
                        drawPath(bakCanvas);
                        mCanvas.drawBitmap(bak,0,0,null);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(mHolder != null &&  mCanvas != null) {
                        mHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public void drag(int dx,int dy) {
        mMatrix = new Matrix();
        mMatrix.postTranslate(dx,dy);
        bak = Bitmap.createBitmap(bak,0,0,bak.getWidth(),bak.getHeight(),mMatrix,false);
    }
}

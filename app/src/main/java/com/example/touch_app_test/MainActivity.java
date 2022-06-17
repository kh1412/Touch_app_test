package com.example.touch_app_test;

import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.touch_app_test.databinding.ActivityMainBinding;


public class MainActivity extends Activity {
    //layout
    private TextView text_sensorval;
    private TextView text_motioneve;
    private TextView text_gesture;
    private ActivityMainBinding binding;
    //Gesture(touch)
    private GestureDetector mGestureDetector;
    //Display size ----- Galaxy Watch
    int watch_width = 396;
    int watch_height = 396;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.root);

        text_sensorval = binding.text01;
        text_motioneve = binding.text02;
        text_gesture = binding.text03;

        mGestureDetector = new GestureDetector(this, mGestureListener);

    }

    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        float max_width = watch_width/2;
        float max_height = watch_height/2;
        //X座標の取得
        float x_coor = event.getX() - max_width;
        //Y座標の取得
        float y_coor = (event.getY() - max_height) * -1;
        //極座標
        double r = Math.sqrt(Math.pow(x_coor,2) + Math.pow(y_coor,2));
        double theta = Math.atan2(x_coor,y_coor);
        double degree = toDegrees(theta);

        if(r > max_width - 50){
            text_motioneve.setText("Input Area");
        }else{
            text_motioneve.setText("Not Area");
        }

        text_sensorval.setText(String.valueOf(degree));

        for(int i=0; i<12; i++){
            if(i == 0){
                if(degree <= 15 || degree > -15){
                    text_sensorval.setText(String.valueOf(i));
                }
            }else if(i<6){
                if(degree > i*30-15 && degree <= i*30+15){
                    text_sensorval.setText(String.valueOf(i));
                }
            }else if(i == 6){
                if(degree >= 165 || degree < -165){
                    text_sensorval.setText(String.valueOf(i));
                }
            }else{
                if(degree > (i-12)*30-15 && degree <= (i-12)*30+15){
                    text_sensorval.setText(String.valueOf(i));
                }
            }

        }

        //TextViewに表示
        //text_sensorval.setText(String.valueOf(x_coor) + "\n" + String.valueOf(y_coor));

        /*
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                text_motioneve.setText("Tap Down");
                break;
            case MotionEvent.ACTION_MOVE:
                text_motioneve.setText("Moving");
                break;
            case MotionEvent.ACTION_UP:
                text_motioneve.setText("Tap Up");
                break;
            case MotionEvent.ACTION_CANCEL:
                text_motioneve.setText("Cancel");
                break;
        }
        */
        return super.onTouchEvent(event);
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            text_gesture.setText("Double Tap");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            //text_gesture.setText("Moving -> Double Tap");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            text_gesture.setText("Single Tap");
            return true;
        }

        // 押下時に呼ばれる
        @Override
        public boolean onDown(MotionEvent e) {
            text_gesture.setText("onDown");
            return false;
        }

        // プレス時に呼ばれる(onDownが先に呼ばれ、意味が異なる)
        @Override
        public void onShowPress(MotionEvent e) {
            text_gesture.setText("onShowPress");
        }

        // 長押し時に呼ばれる
        @Override
        public void onLongPress(MotionEvent e) {
            text_gesture.setText("onLongPress");
        }

        // フリック時に呼ばれる
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float x, float y) {
            text_gesture.setText("onFling");
            return false;
        }

        // スクロール時に呼ばれる
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float x, float y) {
            text_gesture.setText("onScroll");
            return false;
        }
    };
}
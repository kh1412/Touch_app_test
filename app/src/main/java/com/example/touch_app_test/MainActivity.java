package com.example.touch_app_test;

import static java.lang.Math.toDegrees;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.touch_app_test.databinding.ActivityMainBinding;

import java.util.ArrayList;


public class MainActivity extends Activity {
    //layout
    private TextView text_selectNum;
    private TextView text_inputArea;
    private TextView text_gesture;
    private TextView text_gesture2;
    private ArrayList<TextView> dispCharacter = new ArrayList<TextView>();
    private ActivityMainBinding binding;
    //Gesture(touch)
    private GestureDetector mGestureDetector;

    private int state;
    int down_position = -1;
    /*
    //Display size ----- Galaxy Watch
    int watch_width = 396;
    int watch_height = 396;
    */
    //Display size ----- virtual
    int watch_width = 384;
    int watch_height = 384;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.root);

        //state = 0;
        text_selectNum = binding.text01;
        text_inputArea = binding.text02;
        text_gesture = binding.text03;
        text_gesture2 = binding.text04;
        Drawable drawable_defo = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_white_icon, null);
        Drawable drawable_selected = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_selected, null);
        mGestureDetector = new GestureDetector(this, mGestureListener);

        //円周上に表示
        int num = 12;
        int radius = watch_width/2 - 20;
        Kana_Character kana = new Kana_Character();
        for (int i = 0; i < num; i++) {
            TextView textView = new TextView(this);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.circleConstraint = binding.root.getId(); // 基点になるViewを指定
            layoutParams.circleAngle = computeAngle(num, i); // 角度を指定
            layoutParams.circleRadius = radius; // 半径を指定(dp)
            textView.setLayoutParams(layoutParams);
            textView.setText(String.valueOf(i)); //数字(i番目)表示
            textView.setText(kana.set(i,0));
            textView.setTextColor(Color.BLACK);
            binding.root.addView(textView);
            dispCharacter.add(textView);
        }
    }

    //num of circle
    private static final int CIRCLE_RADIUS = 360;

    private float computeAngle(int num, int index) {
        float angleUnit = (float) CIRCLE_RADIUS / num;
        return angleUnit * index;
    }

    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        Drawable drawable_defo = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_white_icon, null);
        Drawable drawable_selected = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_selected, null);
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
            text_inputArea.setText("Input Area");
        }else{
            text_inputArea.setText("Not Area");
        }

        //degree表示(角度)
        //text_sensorval.setText(String.valueOf(degree));
        int selected_num = -1;
        if(r > max_width - 50){
            for(int i=0; i<12; i++){
                if(i == 0){
                    if(degree <= 15 || degree > -15){
                        selected_num = i;
                    }
                }else if(i<6){
                    if(degree > i*30-15 && degree <= i*30+15){
                        selected_num = i;
                    }
                }else if(i == 6){
                    if(degree >= 165 || degree < -165){
                        selected_num = i;
                    }
                }else{
                    if(degree > (i-12)*30-15 && degree <= (i-12)*30+15){
                        selected_num = i;
                    }
                }
            }
        }

        text_selectNum.setText(String.valueOf(selected_num));

        binding.circleView00.setBackground(drawable_defo);
        binding.circleView01.setBackground(drawable_defo);
        binding.circleView02.setBackground(drawable_defo);
        binding.circleView03.setBackground(drawable_defo);
        binding.circleView04.setBackground(drawable_defo);
        binding.circleView05.setBackground(drawable_defo);
        binding.circleView06.setBackground(drawable_defo);
        binding.circleView07.setBackground(drawable_defo);
        binding.circleView08.setBackground(drawable_defo);
        binding.circleView09.setBackground(drawable_defo);
        binding.circleView10.setBackground(drawable_defo);
        binding.circleView11.setBackground(drawable_defo);


        //選択Viewの色変更
        if (event.getAction() != MotionEvent.ACTION_UP){
            switch(selected_num){
                case -1:
                    break;
                case 0:
                    binding.circleView00.setBackground(drawable_selected);
                    break;
                case 1:
                    binding.circleView01.setBackground(drawable_selected);
                    break;
                case 2:
                    binding.circleView02.setBackground(drawable_selected);
                    break;
                case 3:
                    binding.circleView03.setBackground(drawable_selected);
                    break;
                case 4:
                    binding.circleView04.setBackground(drawable_selected);
                    break;
                case 5:
                    binding.circleView05.setBackground(drawable_selected);
                    break;
                case 6:
                    binding.circleView06.setBackground(drawable_selected);
                    break;
                case 7:
                    binding.circleView07.setBackground(drawable_selected);
                    break;
                case 8:
                    binding.circleView08.setBackground(drawable_selected);
                    break;
                case 9:
                    binding.circleView09.setBackground(drawable_selected);
                    break;
                case 10:
                    binding.circleView10.setBackground(drawable_selected);
                    break;
                case 11:
                    binding.circleView11.setBackground(drawable_selected);
                    break;
            }
        }

        //TextViewに表示
        //text_sensorval.setText(String.valueOf(x_coor) + "\n" + String.valueOf(y_coor));

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                text_gesture2.setText("Tap Down");
                if(r > max_width - 50){
                    for(int i=0; i<12; i++){
                        if(i == 0){
                            if(degree <= 15 || degree > -15){
                                down_position = i;
                            }
                        }else if(i<6){
                            if(degree > i*30-15 && degree <= i*30+15){
                                down_position = i;
                            }
                        }else if(i == 6){
                            if(degree >= 165 || degree < -165){
                                down_position = i;
                            }
                        }else{
                            if(degree > (i-12)*30-15 && degree <= (i-12)*30+15){
                                down_position = i;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                text_gesture2.setText("Moving");
                break;
            case MotionEvent.ACTION_UP:
                text_gesture2.setText("Tap Up");
                state = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                text_gesture2.setText("Cancel");
                break;
        }

        //円周上に表示
        int num = 12;
        int radius = watch_width/2 - 20;
        Kana_Character kana = new Kana_Character();
        TextView textView;
        if (state == 1){
            for (int i = 0; i < num; i++) {
                if(i > num){
                    textView = dispCharacter.get(i-12);//数字(i番目)表示
                }else{
                    textView = dispCharacter.get(i);//数字(i番目)表示
                }

                if(i-down_position < -12+5){
                    textView.setText(kana.set(down_position,i-down_position+12));
                }else if(i-down_position < 0){
                    textView.setText("");
                }else if(i-down_position < kana.kana[0].length){
                    textView.setText(kana.set(down_position,i-down_position));
                }else{
                    textView.setText("");
                }
                textView.setTextColor(Color.BLACK);
            }
        }else{
            for (int i = 0; i < num; i++) {
                textView = dispCharacter.get(i);//数字(i番目)表示
                if(i < kana.kana.length){
                    textView.setText(kana.set(i,0));
                }else{
                    textView.setText("");
                }
            }
        }



        return super.onTouchEvent(event);
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //text_gesture.setText("Double Tap");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            text_gesture.setText("Moving -> Double Tap");
            //binding.circleView00.setBackground(drawable_selected);
            state = 1;
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
            //text_gesture.setText("onShowPress");
        }

        // 長押し時に呼ばれる
        @Override
        public void onLongPress(MotionEvent e) {
            //text_gesture.setText("onLongPress");
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
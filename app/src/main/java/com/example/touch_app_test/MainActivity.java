package com.example.touch_app_test;

import static java.lang.Math.toDegrees;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.touch_app_test.databinding.ActivityMainBinding;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends Activity {
    //layout
    private TextView text_selectNum;
    private TextView text_inputArea;
    private TextView text_gesture;
    private TextView text_gesture2;
    private TextView text_text;
    private ArrayList<TextView> dispCharacter = new ArrayList<TextView>();
    private ActivityMainBinding binding;
    //Gesture(touch)
    private GestureDetector mGestureDetector;

    private int state;
    int selected_num = -1;
    int selected_num_prev = 0;
    int down_position = -1;
    int inring = 0;
    String log_text = "";
    ArrayList<Log_detail> log_details = new ArrayList<Log_detail>();
    Log_detail log_detail_tmp = new Log_detail();
    String set_character = "";
    private String filename = "Test.csv";
    /*
    //Display size ----- Galaxy Watch
    int watch_width = 396;
    int watch_height = 396;
    //Display size ----- virtual
    int watch_width = 384;
    int watch_height = 384;
    */
    //Display size ----- Sunto 7
    int watch_width = 454;
    int watch_height = 454;


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
        text_text = binding.text05;
        Button button = binding.button;
        Drawable drawable_defo = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_white_icon, null);
        Drawable drawable_selected = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_selected, null);
        mGestureDetector = new GestureDetector(this, mGestureListener);

        //円周上に表示
        int num = 12;
        int radius = watch_width/2 - 25;
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
            textView.setText(kana.set(i,0)); //文字表示
            textView.setTextColor(Color.BLACK); //文字の色
            binding.root.addView(textView);
            dispCharacter.add(textView);
        }

        //buttonを押したときの処理
        button.setOnClickListener( v -> {
            try {
                FileOutputStream fos = openFileOutput(filename, Context.MODE_APPEND);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);

                bw.write(String.format("Log_Input: "));
                bw.write(String.format(String.valueOf(log_text)) + "\n");
                bw.close();
                //text_text.setText("");

                FileOutputStream fos2 = openFileOutput("Log_"+filename, Context.MODE_APPEND);
                OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "windows-31j");
                BufferedWriter bw2 = new BufferedWriter(osw2);
                bw2.write(String.format(String.valueOf(log_text)) + "\n");
                bw2.write(String.format("text,char,InputDuration,FirstDown,LastUp,TouchDuration,DownTime,UpTime,DownPosition,r,theta,UpPosition,r,theta\n"));
                if(log_details.size() == 0){
                    bw2.write(String.format("Empty"));
                }else{
                    log_details.get(log_details.size()-1).lastup = log_details.get(log_details.size()-1).uptime;
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
                    Date begin = df.parse(log_details.get(log_details.size()-1).firstdown);
                    Date end = df.parse(log_details.get(log_details.size()-1).lastup);
                    log_details.get(log_details.size()-1).input_duration = end.getTime() - begin.getTime();
                    log_details.get(log_details.size()-1).text = log_text;
                    for(int i=0;i<log_details.size();i++){
                        bw2.write(String.format("%s,%s,", log_details.get(i).text, log_details.get(i).select_char));
                        bw2.write(String.format("%d,%s,%s,", log_details.get(i).input_duration, log_details.get(i).firstdown, log_details.get(i).lastup));
                        bw2.write(String.format("%d,%s,%s,", log_details.get(i).touch_duration, log_details.get(i).downtime, log_details.get(i).uptime));
                        bw2.write(String.format("%d,%s,%s,", log_details.get(i).down_position, log_details.get(i).down_pc.r, log_details.get(i).down_pc.degree));
                        bw2.write(String.format("%d,%s,%s", log_details.get(i).up_position, log_details.get(i).up_pc.r, log_details.get(i).up_pc.degree));
                        bw2.write("\n");
                    }
                }

                bw2.close();
                log_details.clear();
                log_text = "";
                text_text.setText("");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } ;
        });
    }

    //num of circle
    private static final int CIRCLE_RADIUS = 360;

    private float computeAngle(int num, int index) {
        float angleUnit = (float) CIRCLE_RADIUS / num;
        return angleUnit * index;
    }

    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Drawable drawable_defo = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_white_icon, null);
        Drawable drawable_selected = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_selected, null);
        TextView textView;
        Kana_Character kana = new Kana_Character();
        int num = 12;
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

        //タッチ領域
        double ring_thikness = max_height - 80;

        if(r > ring_thikness){
            text_inputArea.setText("Input Area");
        }else{
            text_inputArea.setText("Not Area");
        }

        //degree表示(角度)
        //text_sensorval.setText(String.valueOf(degree));

        if(r > ring_thikness){
            inring = 1;
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
        }else{
            inring = 0;
        }
        if(selected_num != selected_num_prev){
            selected_num_prev = selected_num;
            vibrator.vibrate(50);
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
                if(inring == 1){
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
                }else{
                    down_position = -1;
                }
                selected_num = down_position;

                if(log_detail_tmp.downtime == null){
                    if(log_details.size() == 0){
                        log_detail_tmp.firstdown = getDate();
                    }else{
                        log_detail_tmp.firstdown = log_details.get(0).firstdown;
                    }
                    log_detail_tmp.downtime = getDate();
                    log_detail_tmp.down_position = down_position;
                    log_detail_tmp.down_pc.r = r;
                    log_detail_tmp.down_pc.degree = degree;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                text_gesture2.setText("Moving");
                break;
            case MotionEvent.ACTION_UP:
                text_gesture2.setText("Tap Up");
                if(inring == 1 && down_position != -1 && selected_num != -1){
                    if(selected_num >= 0 && selected_num <= 11){
                        if(selected_num - down_position >= 0){
                            if(selected_num - down_position < kana.kana[down_position].length){
                                set_character = kana.set(down_position,selected_num-down_position);
                            }
                        }else{
                            if(selected_num - down_position + num < kana.kana[down_position].length){
                                set_character = kana.set(down_position,selected_num-down_position + num);
                            }
                        }
                    }


                    String tmp_text;
                    tmp_text = text_text.getText().toString();
                    if(set_character == "削除"){ //削除
                        if(tmp_text.length()-1 >= 0){
                            text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                        }
                    }else if(set_character == kana.set(10,0)){ //小
                        if(tmp_text.length()-1 >= 0){
                            if(tmp_text.endsWith("つ") == true){ //つ
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("っ");
                            }else if(tmp_text.endsWith("や") == true){ //や
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ゃ");
                            }else if(tmp_text.endsWith("ゆ") == true){
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ゅ");
                            }else if(tmp_text.endsWith("よ") == true){
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ょ");
                            }else if(tmp_text.endsWith("あ") == true){ //あ
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ぁ");
                            }else if(tmp_text.endsWith("い") == true){
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ぃ");
                            }else if(tmp_text.endsWith("う") == true){
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ぅ");
                            }else if(tmp_text.endsWith("え") == true){
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ぇ");
                            }else if(tmp_text.endsWith("お") == true){
                                text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                text_text.append("ぉ");
                            }
                        }
                    }else{
                        text_text.append(set_character);
                    }
                    log_text = text_text.getText().toString();
                }

                if(log_detail_tmp.uptime == null){
                    log_detail_tmp.uptime = getDate();
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
                    try {
                        Date begin = df.parse(log_detail_tmp.downtime);
                        Date end = df.parse(log_detail_tmp.uptime);
                        log_detail_tmp.touch_duration = end.getTime() - begin.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    log_detail_tmp.up_position = selected_num;
                    log_detail_tmp.up_pc.r = r;
                    log_detail_tmp.up_pc.degree = degree;
                    log_detail_tmp.select_char = set_character;
                    log_detail_tmp.text = text_text.getText().toString();;
                    log_details.add(log_detail_tmp);
                    log_detail_tmp = new Log_detail();
                }

                //layout初期化
                inring = 0;
                down_position = -1;
                selected_num = -1;
                selected_num_prev = -1;
                for (int i = 0; i < num; i++) {
                    textView = dispCharacter.get(i);//数字(i番目)表示
                    if(i < kana.kana.length){
                        textView.setText(kana.set(i,0));
                    }else{
                        textView.setText("");
                    }
                    textView.setTextColor(Color.BLACK);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                text_gesture2.setText("Cancel");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getAction());
        }

        //円周上に表示
        if (down_position != -1){
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
            //state = 1;
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

    //時間取得
    public static String getDate(){

        //取得する日時のフォーマットを指定
        final DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

        //時刻をミリ秒で取得
        final Date date = new Date(System.currentTimeMillis());

        //日時を指定したフォーマットで取得
        return df.format(date);
    }
}
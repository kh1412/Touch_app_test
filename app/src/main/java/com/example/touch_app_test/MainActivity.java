package com.example.touch_app_test;

import static com.google.android.gms.wearable.DataMap.TAG;
import static java.lang.Math.toDegrees;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MainActivity extends Activity  implements  SensorEventListener{
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
    //acc & gyr sensor
    private SensorManager mSensorManager;
    private Sensor acc_sensor;
    private Sensor gyr_sensor;
    private Sensor gravity_sensor;
    private int button_start_flag = 0;
    static ArrayList<Values> acc_save = new ArrayList<Values>(); //acc値保存
    static ArrayList<Values> gyr_save = new ArrayList<Values>(); //gyr値保存
    float t1,t2; //時間の一時保存用
    float tx1,ty1,tz1,tx2,ty2,tz2; //センサ値の一時保存用
    long starttime; //計測開始時間(基準時間)
    int i = 0;

    float runningtime = 0;
    float diffaccx = 0;
    float diffaccy = 0;
    float accx_prev = 0;
    float accy_prev = 0;

    private  String gestureResult = "";

    private boolean useGesture = false ;
    private String person_name = "hino";
    private String filename = (useGesture ? person_name + "_useG": person_name);


    public Flag flag = new Flag();
    public Count count = new Count();

    private int state;
    int selected_num = -1;
    int selected_num_prev = -1;
    int down_position = -1;
    int inring = 0;
    String log_text = "";
    ArrayList<Log_detail> log_details = new ArrayList<Log_detail>();
    Log_detail log_detail_tmp = new Log_detail();
    String set_character = "";
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

    class getValueThread extends Thread {
        @Override
        public void run() {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            int sec=0;
            int ss=0;
            int prev_ss = 0;
            float motion_end_time = 0;
            String result = "";
            float prev=0;
            flag.InitializeFlag();
            count.InitializeCount();

            //閾値設定
            Threshold threshold = new Threshold();

            //時間
            cal.setTime(new Date());
            sec = cal.get(Calendar.SECOND);
            ss = cal.get(Calendar.MILLISECOND);
            prev = (float)sec+((float)ss/1000);

            //Sensor値
            Values acc_val = new Values();
            Values gyr_val = new Values();

            TmpValue motion_val = new TmpValue();


            while(button_start_flag == 1){
                //現在時刻取得
                cal.setTime(new Date());
                sec = cal.get(Calendar.SECOND);
                ss = cal.get(Calendar.MILLISECOND);

                if(ss % 10 == 0 && prev_ss != ss){
                    //稼働時間算出
                    if((float)sec+((float)ss/1000) - prev < 0){
                        runningtime += 60 - prev + (float)sec+((float)ss/1000);
                    }else{
                        runningtime += (float)sec+((float)ss/1000) - prev;
                    }
                    prev = (float)sec+((float)ss/1000);
                    prev_ss = ss;

                    diffaccx = tx1 - accx_prev;
                    diffaccy = ty1 - accy_prev;

                    acc_val.set(runningtime, diffaccx, diffaccy, tz1);
                    gyr_val.set(runningtime, tx2, ty2, tz2);

                    //センサ値保存(書き込み用)
                    acc_save.add(acc_val);
                    gyr_save.add(gyr_val);

                    Detector_motion_start motion_start = new Detector_motion_start(acc_val, gyr_val, threshold, flag);
                    Detector_motion_end motion_end = new Detector_motion_end(motion_val, flag, threshold);

                    //判別アルゴリズム_start
                    if(useGesture){
                        if(flag.motion.motion == 0){
                            if(motion_start.DetectMotion() == 1){
                                Log.d(TAG, "start");
                                motion_val.addValues(acc_val, gyr_val);
                                //flag.motion.motion = 1;
                                flag.count++;
                            }
                        }else if(flag.motion.motion == 1){
                            motion_val.addValues(acc_val, gyr_val);
                            flag.count++;
                            //判別結果
                            result = motion_end.DetectMotionEnd();
                            if(result != ""){
                                if(result == "Motion_long"){
                                    ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(10);
                                }else{
                                    flag.motion.motion = 2;
                                    motion_end_time = runningtime;
                                }
                            }
                        }else if(flag.motion.motion == 2){
                            if(runningtime - motion_end_time < threshold.interval){
                                if(result=="LB"){
                                }else if(result=="RB"){
                                }else if(result=="RT"){
                                }else if(result=="Ltw"){
                                }else if(result=="Rtw"){
                                }else if(result=="Up"){
                                }else if(result=="Ltw_long"){
                                }else if(result=="Rtw_long"){
                                }else if(result=="Up_long"){
                                }else if(result == "error"){
                                }
                            }else{
                                Log.d(TAG, "end");
                                motion_val.initValue();
                                flag.InitializeFlag();
                                gestureResult = result;
                                text_gesture2.setText(gestureResult);

                                if(result=="LB"){
                                    count.lb++;
                                }else if(result=="RB"){
                                    count.rb++;
                                }else if(result=="RT"){
                                    count.rt++;
                                }else if(result=="Ltw"){
                                    count.ltw++;
                                }else if(result=="Rtw"){
                                    count.rtw++;
                                }else if(result=="Up"){
                                    count.up++;
                                }else if(result=="Ltw_long"){
                                    count.ltw_l++;
                                }else if(result=="Rtw_long"){
                                    count.rtw_l++;
                                }else if(result=="Up_long"){
                                    count.up_l++;
                                }

                                result = "";
                            }
                        }
                    }

                    accx_prev = tx1;
                    accy_prev = ty1;

                    acc_val = new Values();
                    gyr_val = new Values();


                    //ジェスチャ入力
                    if(gestureResult != ""){
                        if(gestureResult != "error"){
                            if(gestureResult == "Ltw" || gestureResult == "Ltw_long"){ //削除
                                if(text_text.getText().toString().length() != 0){
                                    text_text.setText(text_text.getText().toString().substring(0, text_text.getText().toString().length()-1));
                                }
                            }else if(gestureResult == "Rtw" || gestureResult == "Rtw_long"){ //スペース
                                text_text.append("␣");
                            }else if(gestureResult == "Up" || gestureResult == "Up_long"){ //バジリスクタイム確定
                                text_text.append("⏎");
                            }else{
                                //text_text.append(gestureResult);
                            }
                            log_text = text_text.getText().toString();
                            if(log_detail_tmp.uptime == -1){
                                if(log_details.size() == 0){
                                    log_detail_tmp.firstdown = runningtime;
                                }else{
                                    log_detail_tmp.firstdown = log_details.get(0).firstdown;
                                }
                                log_detail_tmp.uptime = runningtime;
                                log_detail_tmp.select_char = gestureResult;
                                log_detail_tmp.text = text_text.getText().toString();
                                log_details.add(log_detail_tmp);
                                log_detail_tmp = new Log_detail();
                            }
                        }
                        gestureResult = "";
                    }
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.root);

        //ジェスチャ
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //starttime = System.currentTimeMillis();

        //state = 0;
        text_selectNum = binding.text01;
        text_inputArea = binding.text02;
        text_gesture = binding.text03;
        text_gesture2 = binding.text04;
        text_text = binding.text05;
        Button button_start = binding.buttonStart;
        Button button_save = binding.buttonSave;
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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
            textView.setText(kana.set(i,0));
            textView.setTextColor(Color.BLACK);
            binding.root.addView(textView);
            dispCharacter.add(textView);
        }

        //buttonを押したときの処理
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(button_start_flag == 0){
                    button_start_flag = 1;
                    button_start.setText("測定中");
                    button_start.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN).withAlpha(200));

                    getValueThread thread1 = new getValueThread();
                    thread1.setPriority(8);
                    thread1.start();
                }
            }
        });
        button_start.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                if(button_start_flag == 1){
                    button_start_flag = 0;
                    button_start.setText("計測開始");
                    button_start.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY).withAlpha(200));
                    //vibrator.vibrate(1000);
                }
                return true;
            }
        });

        button_save.setOnClickListener( v -> {
            if(button_start_flag == 1){
                try {
                    FileOutputStream fos = openFileOutput(filename + ".csv", Context.MODE_APPEND);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    bw.write(String.format("Log_Input: "));
                    bw.write(String.format(String.valueOf(log_text)) + "\n");
                    bw.close();
                    //text_text.setText("");

                    FileOutputStream fos2 = openFileOutput(filename + "_Log.csv", Context.MODE_APPEND);
                    OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "UTF-8");
                    BufferedWriter bw2 = new BufferedWriter(osw2);
                    bw2.write(String.format(String.valueOf(log_text)) + "\n");
                    bw2.write(String.format("text,char,InputDuration,FirstDown,LastUp,TouchDuration,DownTime,UpTime,DownPosition,r,theta,UpPosition,r,theta\n"));
                    if(log_details.size() == 0){
                        bw2.write(String.format("Empty"));
                    }else{
                        log_details.get(log_details.size()-1).lastup = log_details.get(log_details.size()-1).uptime;
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
                        log_details.get(log_details.size()-1).input_duration = log_details.get(log_details.size()-1).lastup - log_details.get(log_details.size()-1).firstdown;
                        log_details.get(log_details.size()-1).text = log_text;
                        for(int i=0;i<log_details.size();i++){
                            bw2.write(String.format("%s,%s,", log_details.get(i).text, log_details.get(i).select_char));
                            bw2.write(String.format("%f,%f,%f,", log_details.get(i).input_duration, log_details.get(i).firstdown, log_details.get(i).lastup));
                            bw2.write(String.format("%f,%f,%f,", log_details.get(i).touch_duration, log_details.get(i).downtime, log_details.get(i).uptime));
                            bw2.write(String.format("%d,%s,%s,", log_details.get(i).down_position, log_details.get(i).down_pc.r, log_details.get(i).down_pc.degree));
                            bw2.write(String.format("%d,%s,%s", log_details.get(i).up_position, log_details.get(i).up_pc.r, log_details.get(i).up_pc.degree));
                            bw2.write("\n");
                        }
                    }
                    bw2.close();

                    //ジェスチャ
                    int tmp_acc = 1;
                    int tmp_gyr = 1;
                    float time_diff = 0;
                    float time_tmp = 0;
                    float time_total = 0;

                    flag.InitializeFlag();
                    FileOutputStream fos3 = openFileOutput(filename + "_SensorVal.csv", Context.MODE_APPEND);
                    OutputStreamWriter osw3 = new OutputStreamWriter(fos3, "UTF-8");
                    BufferedWriter bw3 = new BufferedWriter(osw3);

                    bw3.write(String.format("size : %d, Input Word: %s\n", acc_save.size(), log_text));
                    bw3.write(String.format("LB,RB,RT,Ltw,Rtw,Up,Ltw_Long,Rtw_Long,Up_Long\n"));
                    bw3.write(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                            count.lb, count.rb, count.rt, count.ltw, count.rtw, count.up, count.ltw_l, count.rtw_l, count.up_l));
                    bw3.write("time_origin,timediff,time,acc_x,acc_y,acc_z,gyr_x,gyr_y,gyr_z,diff_accx,diff_accy,xy_acc,integral_x,integral_y,integral_xy,integral_xy2" +
                            ",integral_gyrx,integral_gyrz,tmp_acc,tmp_gyr,LB,RT,RB,R_tw,L_tw,Up\n");

                    count.InitializeCount();
                    for (i = 0; i < acc_save.size(); i++){
                        t1 = acc_save.get(i).time;
                        tx1 = acc_save.get(i).x;
                        ty1 = acc_save.get(i).y;
                        tz1 = acc_save.get(i).z;
                        t2 = gyr_save.get(i).time;
                        tx2 = gyr_save.get(i).x;
                        ty2 = gyr_save.get(i).y;
                        tz2 = gyr_save.get(i).z;

                        //経過時間
                        time_diff = t1 - time_tmp;
                        if(time_diff < 0){
                            time_diff += 60;
                        }
                        time_total += time_diff;
                        if(i == 0){
                            time_total = 0;
                        }
                        time_tmp = t1;

                        bw3.write(String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f \n",t1,time_diff,time_total,tx1,ty1,tz1,tx2,ty2,tz2));
                    }

                    log_details.clear();
                    log_text = "";
                    text_text.setText("");
                    acc_save.clear();
                    gyr_save.clear();
                    bw3.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        acc_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyr_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener((SensorEventListener) this, acc_sensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener((SensorEventListener) this, gyr_sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this, this.acc_sensor);
        mSensorManager.unregisterListener((SensorEventListener) this, this.gyr_sensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //スタートしていたら加速度記録
            tx1 = event.values[0];
            ty1 = event.values[1];
            tz1 = event.values[2];

        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //スタートしていたら加速度記録
            tx2 = event.values[0];
            ty2 = event.values[1];
            tz2 = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
        if(selected_num != selected_num_prev && down_position != -1){
            if(selected_num >= 0 && selected_num <= 11){
                if(selected_num - down_position >= 0){
                    if(selected_num - down_position < kana.kana[down_position].length){
                        if(kana.set(down_position,selected_num-down_position) != ""){
                            vibrator.vibrate(50);
                        }
                    }
                }else{
                    if(selected_num - down_position + num < kana.kana[down_position].length){
                        if(kana.set(down_position,selected_num-down_position + num) != ""){
                            vibrator.vibrate(50);
                        }
                    }
                }
            }
            selected_num_prev = selected_num;
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

                if(log_detail_tmp.downtime == -1){
                    if(log_details.size() == 0){
                        log_detail_tmp.firstdown = runningtime;
                    }else{
                        log_detail_tmp.firstdown = log_details.get(0).firstdown;
                    }
                    log_detail_tmp.downtime = runningtime;
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
                    int i = -1;
                    int j = -1;
                    if(selected_num >= 0 && selected_num <= 11){
                        if(selected_num - down_position >= 0){
                            if(selected_num - down_position < kana.kana[down_position].length){
                                i = down_position;
                                j = selected_num-down_position;
                                set_character = kana.set(i,j);
                            }
                        }else{
                            if(selected_num - down_position + num < kana.kana[down_position].length){
                                i = down_position;
                                j = selected_num-down_position+num;
                                set_character = kana.set(i,j);
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
                    }else if(set_character == kana.set(10,1)){//濁点
                        int row = -1;
                        int index = 0;
                        for(int k = 0; k < kana.kana.length; k++){
                            Arrays.sort(kana.kana[k]);
                            index = Arrays.binarySearch(kana.kana[k], tmp_text.substring(tmp_text.length()-1));
                            if(index >= 0){
                                row = k;
                                if(!kana.kana2[row][index].isEmpty()){
                                    text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                    text_text.append(kana.kana2[row][index]);
                                }
                                break;
                            }
                        }
                    }else if(set_character == kana.set(10,2)){//半濁点
                        int row = -1;
                        int index = 0;
                        for(int k = 0; k < kana.kana.length; k++){
                            Arrays.sort(kana.kana[k]);
                            index = Arrays.binarySearch(kana.kana[k], tmp_text.substring(tmp_text.length()-1));
                            if(index >= 0){
                                row = k;
                                if(!kana.kana3[row][index].isEmpty()){
                                    text_text.setText(tmp_text.substring(0, tmp_text.length()-1));
                                    text_text.append(kana.kana3[row][index]);
                                }
                                break;
                            }
                        }
                    }else{
                        text_text.append(set_character);
                    }
                    log_text = text_text.getText().toString();
                }

                if(log_detail_tmp.uptime == -1){
                    log_detail_tmp.uptime = runningtime;
                    log_detail_tmp.touch_duration = log_detail_tmp.uptime - log_detail_tmp.downtime;

                    log_detail_tmp.up_position = selected_num;
                    log_detail_tmp.up_pc.r = r;
                    log_detail_tmp.up_pc.degree = degree;
                    log_detail_tmp.select_char = set_character;
                    log_detail_tmp.text = text_text.getText().toString();
                    log_details.add(log_detail_tmp);
                    log_detail_tmp = new Log_detail();
                }

                //layout初期化
                inring = 0;
                down_position = -1;
                selected_num = -1;
                selected_num_prev = -1;
                set_character = "";
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
package com.example.touch_app_test;

public class Log_detail {
    //time
//    String downtime;
//    String uptime;
//    long touch_duration; //タッチしてから離すまでの時間
//    String firstdown;
//    String lastup;
//    long input_duration; //1単語の時間
    float downtime = -1;
    float uptime = -1;
    float touch_duration = -1; //タッチしてから離すまでの時間
    float firstdown = -1;
    float lastup = -1;
    float input_duration = -1; //1単語の時間

    class PolarCoordinate{
        double r;
        double degree;
    }
    //タッチ位置
    int down_position;
    PolarCoordinate down_pc = new PolarCoordinate();
    int up_position;
    PolarCoordinate up_pc = new PolarCoordinate();

    //入力文字
    String select_char;
    String text;
}

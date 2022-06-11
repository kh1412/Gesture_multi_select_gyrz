package com.example.gesture_multi_select_gyrz;
//import androidx.appcompat.app.AppCompatActivity;

import static com.example.gesture_multi_select_gyrz.R.dimen.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.shapes.Shape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Vibrator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.*;

import static java.sql.DriverManager.println;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.example.gesture_multi_select_gyrz.databinding.ActivityMainBinding;

//import androidx.databinding.DataBindingUtil;

public class MainActivity extends Activity implements SensorEventListener {
    private final String TAG = MainActivity.class.getName();
    private TextView mTextView;
    private TextView mTextView2;
    private TextView textView3;
    private View lt_Box, rt_Box, lb_Box, rb_Box, ltw_Box, rtw_Box;
    private SensorManager mSensorManager;
    private Sensor acc_sensor;
    private Sensor gyr_sensor;
    private Sensor gravity_sensor;
    private int button_flag = 0; //1でスタート,2で終わり
    static ArrayList<Values> acc_save = new ArrayList<Values>(); //acc値保存
    static ArrayList<Values> gyr_save = new ArrayList<Values>(); //gyr値保存
    float t1,t2; //時間の一時保存用
    float tx1,ty1,tz1,tx2,ty2,tz2; //センサ値の一時保存用
    long starttime; //計測開始時間(基準時間)
    int i = 0;

    float diffaccx = 0;
    float diffaccy = 0;
    float accx_prev = 0;
    float accy_prev = 0;

    private String person_name = "test";
    private String filename = "Test.csv";

    public Flag flag = new Flag();
    public Count count = new Count();


    String[] spinnerItems = {
            "Sample_adm",
            "Free",
            "LB",
            "RB",
            "RT",
            "Ltw",
            "Rtw",
            "Up",
            "Ltw_Long",
            "Rtw_Long",
            "Up_Long",
    };

    class getValueThread extends Thread {
        @Override
        public void run() {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            int sec=0;
            int ss=0;
            int prev_ss = 0;
            float runningtime = 0;
            float motion_end_time = 0;
            String result = "";
            float prev=0;
            flag.InitializeFlag();
            count.InitializeCount();

            //閾値設定
            Threshold threshold = new Threshold();
            /*
            float threshold_acc_diff_x = (float) 1.2; //#hino:1.2;
            float threshold_acc_diff_y = (float) 1.2; //#hino:1.2;
            float threshold_gyr_x = (float)5; //#hino:5;
            float threshold_gyr_z = (float)1.5; //#hino:1.5;

            //threshold_acc_integral_xy = 2 #hino:2
            float threshold_gyr_integral_x = threshold_gyr_x * 5; //#hino:5;
            float threshold_gyr_integral_z = threshold_gyr_z * 5; //#hino:5;
            */

            //時間
            cal.setTime(new Date());
            sec = cal.get(Calendar.SECOND);
            ss = cal.get(Calendar.MILLISECOND);
            prev = (float)sec+((float)ss/1000);

            //Sensor値
            Values acc_val = new Values();
            Values gyr_val = new Values();

            TmpValue motion_val = new TmpValue();


            while(button_flag == 1){
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
                    //acc_save.add(acc_val);
                    //gyr_save.add(gyr_val);

                    Detector_motion_start motion_start = new Detector_motion_start(acc_val, gyr_val, threshold, flag);
                    Detector_motion_end motion_end = new Detector_motion_end(motion_val, flag, threshold);

                    //判別アルゴリズム_start
                    if(flag.motion.motion == 0){
                        rt_Box.setBackgroundColor(Color.WHITE);
                        lb_Box.setBackgroundColor(Color.BLUE);
                        ltw_Box.setBackgroundColor(Color.YELLOW);
                        rtw_Box.setBackgroundColor(Color.RED);
                        rb_Box.setBackgroundColor(Color.GREEN);

                        if(motion_start.DetectMotion() == 1){
                            Log.d(TAG, "start");
                            motion_val.addValues(acc_val, gyr_val);
                            //flag.motion.motion = 1;
                            flag.count++;
                        }
                    }else if(flag.motion.motion == 1){
                        motion_val.addValues(acc_val, gyr_val);
                        flag.count++;
                        //画面変化
                        rt_Box.setBackgroundColor(Color.BLACK);
                        lb_Box.setBackgroundColor(Color.BLACK);
                        ltw_Box.setBackgroundColor(Color.BLACK);
                        rtw_Box.setBackgroundColor(Color.BLACK);
                        rb_Box.setBackgroundColor(Color.BLACK);

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
                                rt_Box.setBackgroundColor(Color.BLACK);
                                lb_Box.setBackgroundColor(Color.BLUE);
                                ltw_Box.setBackgroundColor(Color.BLACK);
                                rtw_Box.setBackgroundColor(Color.BLACK);
                                rb_Box.setBackgroundColor(Color.BLACK);
                            }else if(result=="RB"){
                                rt_Box.setBackgroundColor(Color.BLACK);
                                lb_Box.setBackgroundColor(Color.BLACK);
                                ltw_Box.setBackgroundColor(Color.BLACK);
                                rtw_Box.setBackgroundColor(Color.BLACK);
                                rb_Box.setBackgroundColor(Color.GREEN);
                            }else if(result=="RT"){
                                rt_Box.setBackgroundColor(Color.WHITE);
                                lb_Box.setBackgroundColor(Color.BLACK);
                                ltw_Box.setBackgroundColor(Color.BLACK);
                                rtw_Box.setBackgroundColor(Color.BLACK);
                                rb_Box.setBackgroundColor(Color.BLACK);
                            }else if(result=="Ltw"){
                                rt_Box.setBackgroundColor(Color.BLACK);
                                lb_Box.setBackgroundColor(Color.BLACK);
                                ltw_Box.setBackgroundColor(Color.YELLOW);
                                rtw_Box.setBackgroundColor(Color.BLACK);
                                rb_Box.setBackgroundColor(Color.BLACK);
                            }else if(result=="Rtw"){
                                rt_Box.setBackgroundColor(Color.BLACK);
                                lb_Box.setBackgroundColor(Color.BLACK);
                                ltw_Box.setBackgroundColor(Color.BLACK);
                                rtw_Box.setBackgroundColor(Color.RED);
                                rb_Box.setBackgroundColor(Color.BLACK);
                            }else if(result=="Up"){
                                rt_Box.setBackgroundColor(Color.GREEN);
                                lb_Box.setBackgroundColor(Color.GREEN);
                                ltw_Box.setBackgroundColor(Color.GREEN);
                                rtw_Box.setBackgroundColor(Color.GREEN);
                                rb_Box.setBackgroundColor(Color.GREEN);
                            }else if(result=="Ltw_long"){
                                rt_Box.setBackgroundColor(Color.BLACK);
                                lb_Box.setBackgroundColor(Color.BLACK);
                                ltw_Box.setBackgroundColor(Color.YELLOW);
                                rtw_Box.setBackgroundColor(Color.BLACK);
                                rb_Box.setBackgroundColor(Color.BLACK);
                            }else if(result=="Rtw_long"){
                                rt_Box.setBackgroundColor(Color.BLACK);
                                lb_Box.setBackgroundColor(Color.BLACK);
                                ltw_Box.setBackgroundColor(Color.BLACK);
                                rtw_Box.setBackgroundColor(Color.RED);
                                rb_Box.setBackgroundColor(Color.BLACK);
                            }else if(result=="Up_long"){
                                rt_Box.setBackgroundColor(Color.GREEN);
                                lb_Box.setBackgroundColor(Color.GREEN);
                                ltw_Box.setBackgroundColor(Color.GREEN);
                                rtw_Box.setBackgroundColor(Color.GREEN);
                                rb_Box.setBackgroundColor(Color.GREEN);
                            }else if(result == "error"){
                                rt_Box.setBackgroundColor(Color.RED);
                                lb_Box.setBackgroundColor(Color.RED);
                                ltw_Box.setBackgroundColor(Color.RED);
                                rtw_Box.setBackgroundColor(Color.RED);
                                rb_Box.setBackgroundColor(Color.RED);
                            }
                        }else{
                            Log.d(TAG, "end");
                            motion_val.initValue();
                            flag.InitializeFlag();
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
                    accx_prev = tx1;
                    accy_prev = ty1;

                    acc_val = new Values();
                    gyr_val = new Values();
                }
            }
        }
    }

    View position_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // recovering the instance state
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);


        /*
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        position_view = (View)findViewById(R.id.pink_circle_view);
        position_view.;
         */

        //num of circle
        int num = 12;
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ConstraintLayout root = binding.root;
        View centralView = binding.centerView;
        int radius = getResources().getDimensionPixelSize(circle_radius);
        // 入力された数だけ等間隔に配置する
        for (int i = 0; i < num; i++) {
            TextView textView = new TextView(this);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.circleConstraint = centralView.getId(); // 基点になるViewを指定
            layoutParams.circleAngle = computeAngle(num, i); // 角度を指定
            layoutParams.circleRadius = radius; // 半径を指定(dp)
            textView.setLayoutParams(layoutParams);
            textView.setText(String.valueOf(i));
            textView.setTextColor(Color.BLACK);
            root.addView(textView);
        }
    }

    //num of circle
    private static final int CIRCLE_RADIUS = 360;

    private float computeAngle(int num, int index) {
        float angleUnit = (float) CIRCLE_RADIUS / num;
        return angleUnit * index;
    }

    @Override
    protected void onResume() {
        super.onResume();
        acc_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyr_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, acc_sensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyr_sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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

        if(button_flag == 1) {
            //mTextView.setText(String.format("LB:%d, LT:%d, RT:%d, RB:%d",count.lbtap, count.lttap, count.rttap, count.rbtap));
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
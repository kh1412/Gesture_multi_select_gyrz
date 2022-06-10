package com.example.gesture_multi_select_gyrz;
//import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView2 = findViewById(R.id.text2);
        mTextView2.setText(person_name);
        //mTextView2.setText(String.format("%s", person_name));

        rt_Box = (TextView) findViewById(R.id.rt_box);
        lb_Box = (TextView) findViewById(R.id.lb_box);
        rb_Box = (TextView) findViewById(R.id.rb_box);
        ltw_Box = (TextView) findViewById(R.id.ltw_box);
        rtw_Box = (TextView) findViewById(R.id.rtw_box);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);


        //Spinner設定
        //super.onCreate(savedInstanceState);
        textView3 = findViewById(R.id.text3);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner に adapter をセット
        spinner.setAdapter(adapter);
        // リスナーを登録
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                textView3.setText(item);
                filename = String.format("%s_%s.csv",person_name, item);
                //start & stop リセット
                button_flag = 0;
                TextView start_text = findViewById(R.id.button1);
                start_text.setText("START");
                TextView stop_text = findViewById(R.id.button2);
                stop_text.setText("STOP");

                mTextView.setText("STARTを押してください");
                //mTextView.setText(String.format("Ltap:%d, Ctap:%d, Rtap:%d",count.ltap, count.ctap, count.rtap));

                count.InitializeCount();
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
        //Spiner設定終了



        Button start_btn = findViewById(R.id.button1);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starttime = System.currentTimeMillis();
                button_flag = 1;
                TextView start_text = findViewById(R.id.button1);
                start_text.setText("計測中");
                mTextView = (TextView) findViewById(R.id.text);
                //mTextView.setTextSize(15.0f);
                mTextView.setText("計測中");
                rt_Box.setBackgroundColor(Color.WHITE);
                lb_Box.setBackgroundColor(Color.BLUE);
                ltw_Box.setBackgroundColor(Color.YELLOW);
                rtw_Box.setBackgroundColor(Color.RED);
                rb_Box.setBackgroundColor(Color.GREEN);

                getValueThread thread1 = new getValueThread();
                thread1.setPriority(8);
                thread1.start();
            }
        });

        Button stop_btn = findViewById(R.id.button2);
        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView stop_text = findViewById(R.id.button2);
                button_flag = 2;
                int tmp_acc = 1;
                int tmp_gyr = 1;
                float time_diff = 0;
                float time_tmp = 0;
                float time_total = 0;

                mTextView.setText(String.format("LB:%d RT:%d RB:%d\nLtw:%d Rtw:%d Up:%d\nLtw:%d Rtw:%d Up:%d",
                        count.lb, count.rb, count.rt, count.ltw, count.rtw, count.up, count.ltw_l, count.rtw_l, count.up_l));

                //　ファイルに出力

                try {
                    flag.InitializeFlag();
                    FileOutputStream fos = openFileOutput(filename, Context.MODE_APPEND);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    bw.write(String.format("size : %d\n", acc_save.size()));
                    bw.write(String.format("LB,RB,RT,Ltw,Rtw,Up,Ltw_Long,Rtw_Long,Up_Long\n"));
                    bw.write(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                            count.lb, count.rb, count.rt, count.ltw, count.rtw, count.up, count.ltw_l, count.rtw_l, count.up_l));
                    bw.write("time_origin,timediff,time,acc_x,acc_y,acc_z,gyr_x,gyr_y,gyr_z,diff_accx,diff_accy,xy_acc,integral_x,integral_y,integral_xy,integral_xy2" +
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

                        /*
                        bw.write(String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%d,%d,%d,%d,%d,%d,%d,%d\n"
                                ,t1,time_diff,time_total,tx1,ty1,tz1,tx2,ty2,tz2,diffaccx,diffaccy,xy_acc,flag.integral_x_acc,flag.integral_y_acc,flag.integral_xy_acc,flag.integral_xy2_acc,
                                flag.integral_x_gyr,flag.integral_z_gyr,tmp_acc,tmp_gyr,count.touch_position[1],count.touch_position[2],count.touch_position[3],count.touch_position[5],count.touch_position[6],count.touch_position[7]));

                         */
                        bw.write(String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f \n",t1,time_diff,time_total,tx1,ty1,tz1,tx2,ty2,tz2));

                    }

                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                acc_save.clear();
                gyr_save.clear();

                stop_text.setText("計測完了");
            }
        });
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
package com.example.gesture_multi_select_gyrz;
public class Threshold {
    float acc_diff_x = (float) 1.2; //#hino:1.2;
    float acc_diff_y = (float) 1.2; //#hino:1.2;
    float gyr_x = (float)5; //#hino:5;
    float gyr_z = (float)1.2; //#hino:1.5;

    //threshold_acc_integral_xy = 2 #hino:2
    float gyr_integral_x = gyr_x * 5; //#hino:5;
    float gyr_integral_z = gyr_z * 5; //#hino:5;

    float peakinterval_up = (float) 0.1;
    float peakinterval_twist = (float) 0.1;
    float peakinterval_tap = (float) 0.1;

    //判別後のinterval
    float interval = (float)0.5;
}

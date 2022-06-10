package com.example.gesture_multi_select_gyrz;

public class Detector_motion_start {
    Values acc;
    Values gyr;
    Threshold threshold;
    Flag flag;

    Detector_motion_start(Values acc, Values gyr, Threshold threshold, Flag flag){
        this.acc = acc;
        this.gyr = gyr;
        this.threshold = threshold;
        this.flag = flag;
    }

    int DetectMotion(){
        if(gyr.x < threshold.gyr_x * -1){
            flag.motion.motion = 1;
            if (flag.motion.gyr.x == 0){
                flag.motion.gyr.x = 1;
                flag.peaktime_gyr_x.first = acc.time;
            }
        }
        if(gyr.z > threshold.gyr_z){
            flag.motion.motion = 1;
            if(flag.motion.gyr.z == 0){
                flag.motion.gyr.z = 1;
                flag.peaktime_gyr_z.first = acc.time;
            }
        }else if(gyr.z < threshold.gyr_z * -1){
            flag.motion.motion = 1;
            if(flag.motion.gyr.z == 0){
                flag.motion.gyr.z = -1;
                flag.peaktime_gyr_z.first = acc.time;
            }
        }
        if(acc.x > threshold.acc_diff_x){
            flag.motion.motion = 1;
            if(flag.motion.acc.x == 0){
                flag.motion.acc.x = 1;
                flag.peaktime_acc_x .first= acc.time;
            }
        }else if (acc.x < threshold.acc_diff_x * -1){
            flag.motion.motion = 1;
            if(flag.motion.acc.x == 0){
                flag.motion.acc.x = -1;
                flag.peaktime_acc_x .first= acc.time;
            }
        }
        if(acc.y > threshold.acc_diff_y){
            flag.motion.motion = 1;
            if(flag.motion.acc.y == 0){
                flag.motion.acc.y = 1;
                //flag.motion.tap = 1;
                flag.peaktime_acc_y .first= acc.time;
            }
        }else if (acc.y < threshold.acc_diff_y * -1){
            flag.motion.motion = 1;
            if(flag.motion.acc.y == 0){
                flag.motion.acc.y = -1;
                flag.peaktime_acc_y .first= acc.time;
            }
        }

        if((acc.x > threshold.acc_diff_x || acc.x < threshold.acc_diff_x * -1)
                || (acc.y > threshold.acc_diff_y || acc.y < threshold.acc_diff_y * -1)){
            //flag.motion.motion = 1;
            flag.motion.tap = 1;
        }

        if(flag.motion.motion == 0){
            return 0;
        }else{
            return 1;
        }
    }
}

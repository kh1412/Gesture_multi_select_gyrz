package com.example.gesture_multi_select_gyrz;

import android.os.Vibrator;

public class Detector_motion_end {
    TmpValue motion_val;
    Flag flag;
    Threshold threshold;
    int long_motion = 0;

    Detector_motion_end(TmpValue motion_val, Flag flag, Threshold threshold){
        this.motion_val = motion_val;
        this.flag = flag;
        this.threshold = threshold;
    }

    String DetectMotionEnd(){
        //first_peak, second_peak取得
        //gyr_x
        if(motion_val.gyr.get(motion_val.gyr.size()-1).x < threshold.gyr_x * -1){
            if (flag.motion.gyr.x == 0){
                flag.motion.gyr.x = 1;
                flag.peaktime_gyr_x.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }if(motion_val.gyr.get(motion_val.gyr.size()-1).x > threshold.gyr_x){
            if(flag.motion.gyr.x == 1){
                flag.motion.gyr.x = 2;
                flag.peaktime_gyr_x.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }
        //gyr_y
        if(motion_val.gyr.get(motion_val.gyr.size()-1).z > threshold.gyr_z){
            if(flag.motion.gyr.z == 0){
                flag.motion.gyr.z = 1;
                flag.peaktime_gyr_z.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }else if(flag.motion.gyr.z == -1){
                flag.motion.gyr.z = -2;
                flag.peaktime_gyr_z.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }else if(motion_val.gyr.get(motion_val.gyr.size()-1).z < threshold.gyr_z * -1){
            if(flag.motion.gyr.z == 0){
                flag.motion.gyr.z = 1;
                flag.peaktime_gyr_z.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }else if(flag.motion.gyr.z == 1){
                flag.motion.gyr.z = 2;
                flag.peaktime_gyr_z.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }
        //acc_x
        if( motion_val.acc.get(motion_val.acc.size()-1).x > threshold.acc_diff_x){
            if(flag.motion.acc.x == 0){
                flag.motion.acc.x = 1;
                flag.peaktime_acc_x.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }else if(flag.motion.acc.x == -1){
                flag.motion.acc.x = -2;
                flag.peaktime_acc_x.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }else if ( motion_val.acc.get(motion_val.acc.size()-1).x < threshold.acc_diff_x * -1){
            if(flag.motion.acc.x == 0){
                flag.motion.acc.x = -1;
                flag.peaktime_acc_x.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }else if(flag.motion.acc.x == 1){
                flag.motion.acc.x = 2;
                flag.peaktime_acc_x.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }
        //acc_y
        if( motion_val.acc.get(motion_val.acc.size()-1).y > threshold.acc_diff_y){
            if(flag.motion.acc.y == 0){
                flag.motion.acc.y = 1;
                flag.peaktime_acc_y.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }else if(flag.motion.acc.y == -1){
                flag.motion.acc.y = -2;
                flag.peaktime_acc_y.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }else if ( motion_val.acc.get(motion_val.acc.size()-1).y < threshold.acc_diff_y * -1){
            if(flag.motion.acc.y == 0){
                flag.motion.acc.y = -1;
                flag.peaktime_acc_y.setFirstPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }else if(flag.motion.acc.y == 1){
                flag.motion.acc.y = 2;
                flag.peaktime_acc_y.setSecondPeak(motion_val.acc.get(motion_val.acc.size()-1).time);
            }
        }

        //Flag_Motion(first peak)
        if(flag.motion.gyr.x == 1){
            flag.motion.up = 1;
        }
        if(flag.motion.gyr.z == 1){
            flag.motion.twist = 1;
        }else if(flag.motion.gyr.z == -1){
            flag.motion.twist = -1;
        }
        if(flag.motion.acc.x != 0 && flag.motion.acc.y != 0){
            flag.motion.tap = 1;
        }

        //motion_lim変更
        if(flag.motion.up != 0 || flag.motion.twist != 0){
            flag.motion_lim = (float)0.5;
        }

        //Flag_Motion(バンド回転, 文字盤回転 gyr_integralの閾値)
        //Up
        if(flag.motion.up == 1){
            if(motion_val.gyr_integral.get(motion_val.gyr_integral.size()-1).x < threshold.gyr_integral_x * -1){
                flag.motion.up = 2;
            }
        }
        //R_tw
        if(flag.motion.twist == 1){
            if(motion_val.gyr_integral.get(motion_val.gyr_integral.size()-1).z > threshold.gyr_integral_z * 1){
                flag.motion.twist = 2;
            }
        }
        //L_tw
        if(flag.motion.twist == -1){
            if(motion_val.gyr_integral.get(motion_val.gyr_integral.size()-1).z < threshold.gyr_integral_z * -1){
                flag.motion.twist = -2;
            }
        }

        //Flag_Motion(second peak)
        //Up
        if(flag.motion.up == 2){
            if(flag.motion.gyr.x == 2){
                if(flag.peaktime_gyr_x.peakinterval > threshold.peakinterval_up){
                    if(flag.peaktime_gyr_x.peakinterval < flag.motion_lim){
                        return "Up";
                    }else{
                        return "Up_long";
                    }
                }else{ //例外処理
                    return "error";
                }
            }else if(motion_val.gyr.get(motion_val.gyr.size()-1).time - flag.peaktime_gyr_x.first > flag.motion_lim){
                return "Motion_long";
            }
        }
        //R_tw
        if(flag.motion.twist == 2){
            if(flag.motion.gyr.z == 2){
                if (flag.peaktime_gyr_z.peakinterval > threshold.peakinterval_twist){
                    if(flag.peaktime_gyr_z.peakinterval < flag.motion_lim){
                        return "Rtw";
                    }else{
                        return "Rtw_long";
                    }
                }else{ //例外処理
                    return "error";
                }
            }else if(motion_val.gyr.get(motion_val.gyr.size()-1).time - flag.peaktime_gyr_z.first > flag.motion_lim){
                return "Motion_long";
            }
        }
        //L_tw
        if(flag.motion.twist == -2){
            if(flag.motion.gyr.z == -2){
                if (flag.peaktime_gyr_z.peakinterval > threshold.peakinterval_twist){
                    if(flag.peaktime_gyr_z.peakinterval < flag.motion_lim){
                        return "Ltw";
                    }else{
                        return "Ltw_long";
                    }
                }else{
                    return "error";
                }
            }else if(motion_val.gyr.get(motion_val.gyr.size()-1).time - flag.peaktime_gyr_z.first > flag.motion_lim){
                return "Motion_long";
            }
        }
        //Tap
        if(motion_val.acc.get(motion_val.acc.size()-1).time - motion_val.acc.get(0).time > flag.motion_lim){
            if(flag.peaktime_acc_x.peakinterval < threshold.peakinterval_tap && flag.peaktime_acc_y.peakinterval < threshold.peakinterval_tap){
                if(flag.motion.up < 2 && flag.motion.twist * flag.motion.twist < 4){
                    if(motion_val.acc_xy_integral.get(motion_val.acc_xy_integral.size()-1) > 0){
                        if(flag.motion.acc.x > 0 && flag.motion.acc.y > 0){ //LB
                            return "LB";
                        }else if(flag.motion.acc.x < 0 && flag.motion.acc.y < 0){ //RT
                            return "RT";
                        }
                    }else if(motion_val.acc_xy_integral.get(motion_val.acc_xy_integral.size()-1) < 0){
                        if(flag.motion.acc.x < 0 && flag.motion.acc.y > 0){ //RB
                            return "RB";
                        }else{
                            return "error";
                        }
                    }
                }
            }else{
                return "error";
            }
        }

        return ""; // tmp
    }
}

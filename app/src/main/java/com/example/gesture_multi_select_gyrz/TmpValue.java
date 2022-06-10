package com.example.gesture_multi_select_gyrz;
import java.util.ArrayList;

public class TmpValue {
    ArrayList<Values> acc = new ArrayList<Values>();
    //ArrayList<Values> acc_integral = new ArrayList<Values>();
    ArrayList<Values> gyr = new ArrayList<Values>();
    ArrayList<Values> gyr_integral = new ArrayList<Values>();
    ArrayList<Float> acc_xy = new ArrayList<Float>();
    ArrayList<Float> acc_xy_integral = new ArrayList<Float>();

    void addValues(Values acc_vals, Values gyr_vals){
        Values tmp = new Values();
        acc.add(acc_vals);
        //acc_integral.add(acc_tmp.addElement(acc_integral.get(acc_integral.size()), acc_vals));
        gyr.add(gyr_vals);

        if(gyr_integral.size() == 0){
            gyr_integral.add(gyr_vals);
        }else{
            gyr_integral.add(tmp.addElement(gyr_integral.get(gyr_integral.size()-1), gyr_vals));
        }

        acc_xy.add(acc_vals.x * acc_vals.y);
        if(acc_xy_integral.size() == 0){
            acc_xy_integral.add(acc_xy.get(acc_xy.size()-1));
        }else{
            acc_xy_integral.add(acc_xy_integral.get(acc_xy_integral.size()-1) + acc_xy.get(acc_xy.size()-1));
        }
    }

    void initValue(){
        acc.clear();
        gyr.clear();
        gyr_integral.clear();
        acc_xy.clear();
        acc_xy_integral.clear();
    }
}

package com.example.gesture_multi_select_gyrz;
public class Values{
    float time;
    float x;
    float y;
    float z;

    public Values set(float st, float sx, float sy, float sz){ // onSensorChangedで取得した値をｔｍｐに代入
        this.time = st;
        this.x = sx;
        this.y = sy;
        this.z = sz;
        return this;
    }

    Values addElement(Values a, Values b){
        Values sum = new Values();
        sum.time = a.time;
        sum.x = a.x + b.x;
        sum.y = a.y + b.y;
        sum.z = a.z + b.z;
        return sum;
    }

    void clearValues(){
        time = 0;
        x = 0;
        y = 0;
        z = 0;
    }

    Values compareValues(Values a, Values b){
        Values tmp = new Values();
        tmp.clearValues();
        if(a.x > b.x){
            tmp.x = 1;
        }
        if(a.y > b.y){
            tmp.y = 1;
        }
        if(a.z > b.z){
            tmp.z = 1;
        }

        return tmp; //a>b:1, a<=b:0
    }
}
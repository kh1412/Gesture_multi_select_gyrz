package com.example.gesture_multi_select_gyrz;
public class Flag {
    class Peaktime{
        float first = 0;
        float second = 0;
        float peakinterval = 0;

        void setFirstPeak(float t){ // onSensorChangedで取得した値をｔｍｐに代入
            this.first = t;
        }

        void setSecondPeak(float t){ // onSensorChangedで取得した値をｔｍｐに代入
            this.second = t;
            this.peakinterval = second - first;
        }

        void clearPeaktime(){
            first = 0;
            second = 0;
            peakinterval = 0;
        }
    }
    class Motion{
        int motion;//(0:無, 1:動作中)
        int tap;//#(0:初期状態, 1:, 2:, 3:)
        int twist;//#(0:初期状態, 1:第1ピークが正, -1:第1ピークが負, [2,-2]:判別後, [3,-3]:第2ピーク)
        int up;//(0:初期状態, 1:first_peak_gyr, 2:first_peak_gyr_integral, 3:)

        class Sensor{
            int x; //(0:none, 1or-1:first peak, 2or-2:second peak)
            int y; //(0:none, 1or-1:first peak, 2or-2:second peak)
            int z; //(0:none, 1or-1:first peak, 2or-2:second peak)

            void initMotion(){
                x = 0;
                y = 0;
                z = 0;
            }
        }

        Sensor acc = new Sensor();
        Sensor gyr = new Sensor();

        void clearMotion(){
            motion = 0;
            tap = 0;
            twist = 0;
            up = 0;
            acc.initMotion();
            gyr.initMotion();
        }
    }

    double motion_starttime = 0; //動作開始時間(second)
    float motion_lim = (float)0.1; //#(手首動作&捻り動作:50, 画面外タップ動作:10)
    int count = 0 ;//#入力からの回数
    //int after_count = 0; //#動作終了後カウント
    int after_lim = 20; //#動作終了後インターバル

    //ピーク時間関係
    Peaktime peaktime_acc_x = new Peaktime();
    Peaktime peaktime_acc_y = new Peaktime();
    //Peaktime acc_z = new Peaktime();
    Peaktime peaktime_gyr_x = new Peaktime();
    //Peaktime gyr_y = new Peaktime();
    Peaktime peaktime_gyr_z = new Peaktime();

    //動作の段階
    Motion motion = new Motion();


    public void InitializeFlag(){
        motion_starttime = 0; //動作開始時間
        motion_lim = (float)0.1; //#(手首動作&捻り動作:50, 画面外タップ動作:10)
        count = 0 ;//#入力からの回数
        //after_count = 0; //#動作終了後カウント
        after_lim = 20; //#動作終了後インターバル

        //ピーク時間関係
        peaktime_acc_x.clearPeaktime();
        peaktime_acc_y.clearPeaktime();
        //acc_z.clearValues();
        peaktime_gyr_x.clearPeaktime();
        //gyr_y.clearValues();
        peaktime_gyr_z.clearPeaktime();

        //動作の段階
        motion.clearMotion();
    }
}

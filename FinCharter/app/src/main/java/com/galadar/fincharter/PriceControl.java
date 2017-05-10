package com.galadar.fincharter;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Galadar on 22/1/2017.
 *
 */

public class PriceControl {

    public static final float MinRectWidth = 70.0f;

    float RectWidth, lineDiff;
    private int HORIZONTAL_BORDER, VERTICAL_BORDER;

    //private Context con;
    private SharedPreferences prefs;
    private int height, width, length;
    private int X_Bottom, X_Top, Y_Bottom, Y_Top;
    private boolean set;

    public PriceControl(Context context){
        //con = context;
        prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        height = prefs.getInt("height", 720);
        width = prefs.getInt("width", 720);
        HORIZONTAL_BORDER = Math.round(width*0.05f);
        VERTICAL_BORDER = Math.round(height*0.1f);
        set = false;
    }

    public int getHORIZONTAL_BORDER() {
        return HORIZONTAL_BORDER;
    }

    public int getVERTICAL_BORDER() {
        return VERTICAL_BORDER;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setObj(int maxX, int minX, int maxY, int minY, int len){
        X_Bottom = minX;
        X_Top = maxX;
        Y_Bottom = minY;
        Y_Top = maxY;
        length = len;
        RectWidth = getAvailWidth()/length;
        if(RectWidth<MinRectWidth) RectWidth=MinRectWidth;
        lineDiff = (float)getAvailWidth()/(float)length;
        set=true;
    }

    public int getX_Bottom() {
        if(set) return X_Bottom;
        else return 1;
    }

    public int getX_Top() {
        if(set) return X_Top;
        else return 1;
    }

    public int getY_Bottom() {
        if(set) return Y_Bottom;
        else return 1;
    }

    public int getY_Top() {
        if(set) return Y_Top;
        else return 1;
    }

    //USE THIS ONLY TO PRINT DATES, ALL CALCULATIONS ARE DONE AS INT
    public String intDateToDate(int totalDays){
        String k = "";
        int year, month, day;
        year=1970;
        month=1;
        day=1;
        while(totalDays>365){
            totalDays-=365;
            year++;
        }

        if(isLeap(year)){
            if(totalDays<=31){
                day = totalDays;
            } else if(totalDays<=60){
                month=2;
                day = totalDays-31;
            } else if(totalDays<=91){
                month=3;
                day = totalDays-60;
            } else if(totalDays<121){
                month=4;
                day = totalDays-91;
            } else if(totalDays<=152){
                month=5;
                day = totalDays-121;
            } else if(totalDays<=182){
                month=6;
                day = totalDays-152;
            } else if(totalDays<=213){
                month=7;
                day = totalDays-182;
            } else if(totalDays<=244){
                month=8;
                day = totalDays-213;
            } else if(totalDays<=274){
                month=9;
                day = totalDays-244;
            } else if(totalDays<=305){
                month=10;
                day = totalDays-274;
            } else if(totalDays<=335){
                month=11;
                day = totalDays-305;
            } else {
                month=12;
                day = totalDays-335;
            }
        } else {
            if(totalDays<=31){
                day = totalDays;
            } else if(totalDays<=59){
                month=2;
                day = totalDays-31;
            } else if(totalDays<=90){
                month=3;
                day = totalDays-59;
            } else if(totalDays<120){
                month=4;
                day = totalDays-90;
            } else if(totalDays<=151){
                month=5;
                day = totalDays-120;
            } else if(totalDays<=181){
                month=6;
                day = totalDays-151;
            } else if(totalDays<=212){
                month=7;
                day = totalDays-181;
            } else if(totalDays<=243){
                month=8;
                day = totalDays-212;
            } else if(totalDays<=273){
                month=9;
                day = totalDays-243;
            } else if(totalDays<=304){
                month=10;
                day = totalDays-273;
            } else if(totalDays<=334){
                month=11;
                day = totalDays-304;
            } else {
                month=12;
                day = totalDays-334;
            }
        }

        switch (prefs.getInt("dateformat", 0)){
            case 0:
                k += day+"/"+month+"/"+year;
                break;
            case 1:
                k += month+"/"+day+"/"+year;
                break;
            case 2:
                k += year+"/"+month+"/"+day;
                break;
            default:
                k += day+"/"+month+"/"+year;
                break;
        }

        return k;
    }

    private boolean isLeap(int year){
        if(year%4==0){
            if(year%100==0){
                if(year%400==0){
                    return true;
                } else return false;
            } else return true;
        } else return false;
    }

    public int getAvailHeight() {
        return height-(2*VERTICAL_BORDER);
    }

    public int getAvailWidth(){
        return width-HORIZONTAL_BORDER;
    }

    public float PriceToYCoord(int price){
        if(set) {
            //float p = (float)price/100.0f;
            float k = height - Math.round( VERTICAL_BORDER + getAvailHeight() * (float)(price - Y_Bottom)/(float)(Y_Top - Y_Bottom) );
            //System.out.println("PriceCoords: "+k);
            return k;
        } else return 1;
    }

    public int LineXCoordToPos(float x){
        if(set) {
            x=x-HORIZONTAL_BORDER;
            int total = Math.round(x/lineDiff);
            if(total<0)total=0;
            else if(total>=length)total=length-1;
            return total;
        } else return 1;
    }

    public int CandleXCoordToPos(float x, int CurrFirst){
        if(set) {
            int k = 0;
            k+= Math.round((x-HORIZONTAL_BORDER-0.5*RectWidth)/RectWidth);
            return k+CurrFirst;
        } else return 1;
    }

    public float LinePosToXCoord(int pos) {
        if(set) return HORIZONTAL_BORDER + pos*lineDiff;
        else return 0;
    }

    public float CandlePosToXAxisCoord(int pos, int firstPrintedCandle) {
        return getHORIZONTAL_BORDER() + (pos-firstPrintedCandle+0.5f)*RectWidth;
    }

    public float CandlePostoXCoord(int pos, int firstPrintedCandle){
        return getHORIZONTAL_BORDER() + (pos-firstPrintedCandle)*RectWidth;
    }

    public float getRectWidth(){
        return RectWidth;
    }

    public float getLineDiff() {
        return lineDiff;
    }
}

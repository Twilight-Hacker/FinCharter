package com.galadar.fincharter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Galadar on 5/1/2017.
 *
 * Candle object for Japanese Candle Chart
 */

class Candle extends Canvas{

    private String tooltip;
    private int high, low, open, close;
    private Bitmap CandleBmp;

    Candle(int open, int high, int low, int close){
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
    }

    void setTooltip(String date) { //Tooltip will be the date as String
        String toolp = "";

        toolp = toolp + "Date: " + date + "\n";
        toolp = toolp + "Open: " + PrintPrice( this.getOpen() ) + "\n";
        toolp = toolp + "High: " + PrintPrice( this.getHigh() ) + "\n";
        toolp = toolp + "Low: " + PrintPrice( this.getLow() ) + "\n";
        toolp = toolp + "Close: " + PrintPrice( this.getClose() );

        this.tooltip = toolp;
    }

    String getTooltip() {
        return tooltip;
    }

    private String PrintPrice(int price){
        String s = Float.toString(price/100.0f);
        if(price%100==0) s+=".00";
        else if (price%10==0) s+="0";
        return s;
    }

    public int getHigh() {
        return high;
    }

    public int getLow() {
        return low;
    }

    public int getOpen() {
        return open;
    }

    public int getClose() {
        return close;
    }
}

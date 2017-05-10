package com.galadar.fincharter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Galadar on 15/1/2017.
 *
 * Use this to transmit the data request to the server (based on share name) and get the results back as two arrays
 *
 * The prices is an int array, that will be divided by 100 to get the price.
 *
 * The dates array is days since epoch (1/1/70)
 */

class ModelControl extends AsyncTask<String, Void, Object[]>{

    private static final long MillisInDay = 86400000L;

    private Context ActivityCon;
    ProgressDialog pbar;
    SharedPreferences prefs;
    PriceControl pc;

    public ModelControl(Context context){
        ActivityCon = context;
        pbar = new ProgressDialog(context);
        pbar.setIndeterminate(true);
        pbar.setMessage("Retrieving Data...");
        pc = new PriceControl(context);
        prefs = context.getSharedPreferences("prefs", context.MODE_PRIVATE);
    }

    private static int[] getDatesData(String name, int days){
        int[] points = new int[days];

        points[0] = getNextWorkDay(15000+new Random().nextInt(3000));

        for (int i = 1; i < points.length; i++) {
            points[i]=getNextWorkDay(points[i-1]);

        }

        return points;
    }

    private static int getNextWorkDay(int prev) {
        long k = prev*MillisInDay;
        Date p = new Date(k);
        p.setTime(p.getTime()+MillisInDay);
        while(!isWorkday(p)){
            p.setTime(p.getTime()+MillisInDay);
        }
        //System.out.println("DayCreate: "+p.getTime());
        //System.out.println("DayCreate: "+(p.getTime()/(MillisInDay)));
        return (int)(p.getTime()/MillisInDay);
    }

    private static boolean isWorkday(Date day){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E", Locale.getDefault());
        String string = simpleDateFormat.format(day);
        if(string.equalsIgnoreCase("Sat")) return false;
        if(string.equalsIgnoreCase("Sun")) return false;

        return true;
    }

    private static int[][] getCandleData(String name, int days){
        Random r = new Random();
        int[][] candles = new int[days][4];

        candles[0][0] = Math.abs(r.nextInt(5000))+12354;
        candles[0][3] = candles[0][0] + Math.abs(r.nextInt(1000)) - Math.abs(r.nextInt(2000));
        candles[0][1] = Math.max(candles[0][0],candles[0][3]) + Math.abs(r.nextInt(1300));
        candles[0][2] = Math.min(candles[0][0],candles[0][3]) - Math.abs(r.nextInt(1300));


        int ranrd;
        for (int i = 1; i < candles.length; i++) {
            do {
                if(r.nextInt()%7==0) ranrd = Math.abs(r.nextInt(300)) - Math.abs(r.nextInt(300));
                else ranrd = 0;
                candles[i][0] = candles[i - 1][3] + ranrd;
            } while (candles[i][0]<2000);

            do {
                candles[i][3] = candles[i][0] + Math.abs(r.nextInt(1000)) - Math.abs(r.nextInt(1000));
            } while (candles[i][3]<2000);

            if(r.nextInt()%5==0) ranrd = 0;
            else ranrd = Math.abs(r.nextInt(1300));
            candles[i][1] = Math.max(candles[i][0],candles[i][3]) + ranrd;

            if(r.nextInt()%5==0) ranrd = 0;
            else ranrd = Math.abs(r.nextInt(1300));
            candles[i][2] = Math.min(candles[i][0],candles[i][3]) - ranrd;
        }

/*        for (int i = 0; i < candles.length; i++) {
            System.out.println("Price Data: "+candles[i][0]+", "+candles[i][1]+", "+candles[i][2]+", "+candles[i][3]);
        }
*/
        return candles;
    }



    //USE THIS FUNCTION TO RETRIEVE DATA FROM THE SERVER AND STORE IT AS DISCUSSED
    @Override
    protected Object[] doInBackground(String... n) {
        String name = n[0];
        int ret = prefs.getInt("current", prefs.getInt("default", 100));

        //REPLACE THESE 2 LINES WITH DATA RETRIEVAL FOR DATES AND CANDLE DATA FROM YOUR SERVER
        int[] dates = ModelControl.getDatesData(name, ret);
        int[][] candledata = ModelControl.getCandleData(name, ret);

        PricesPath prp = new PricesPath();
        int fieldselector = 3;
        int max = candledata[0][1];
        int min = candledata[0][2];
        for (int i = 1; i < candledata.length; i++) {
            if(candledata[i][1]>max)max=candledata[i][1];
            if(candledata[i][2]<min)min=candledata[i][2];
        }
        pc.setObj(dates[0], dates[dates.length-1], max, min, candledata.length);
        float pri, loc;
        prp.moveTo(pc.getHORIZONTAL_BORDER(), candledata[0][fieldselector]);
        for (int i = 1; i < candledata.length; i++) {
            loc = pc.LinePosToXCoord(i);
            pri = pc.PriceToYCoord(candledata[i][fieldselector]);
            prp.lineTo(loc, pri);
        }
        prp.close();


        Object[] o = new Object[4];
        o[0] = name;
        o[1] = dates;
        o[2] = candledata;
        o[3] = prp;

        return o;
    }

    @Override
    protected void onPreExecute() {
        pbar.show();
    }

    @Override
    protected void onPostExecute(Object[] o) {
        String name = (String)o[0];
        int[] dates = (int[])o[1];
        int[][] candleData = (int[][])o[2];
        PricesPath prp = (PricesPath)o[3];
        Intent intent = new Intent(ActivityCon, ChartActivity.class);
        Bundle data = new Bundle();
        data.putString("Name", name);
        data.putIntArray("Dates", dates);
        data.putSerializable("Candles", candleData);
        data.putSerializable("LinePath", prp);
        intent.putExtras(data);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pbar.dismiss();
        ActivityCon.startActivity(intent);
    }
}

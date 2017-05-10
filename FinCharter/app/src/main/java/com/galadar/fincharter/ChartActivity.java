package com.galadar.fincharter;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ChartActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnTouchListener, ScreenShotCallback {

    private static final int MOVE_LIMIT = 50;

    private enum Modes{
        ViewLine, ViewCandles, Select, Draw, Drag
    };

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    boolean candlesChart, hidden;
    private Drawable candleIcon, lineIcon, drawIcon, saveIcon;
    private SharedPreferences prefs;
    String name;
    public Modes mode;
    private int[] Dates;
    private int[][] CandlePrices;
    private PricesPath prp;
    private Candle[] candles;
    private float prevX;
    private ChartSurface mContentView;
    private Handler HideHandler;
    private String toastText = "";
    float rem;
    int pos1, pos2;
    boolean SecLineReady;

    FloatingActionButton swit, draw, save;

    private static int LAYOUT_FLAGS= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    private static int HIDE_FLAGS=LAYOUT_FLAGS | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
             | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = Modes.ViewLine;
        candlesChart = false;
        Bundle data = getIntent().getExtras();
        name = data.getString("Name");
        Dates = data.getIntArray("Dates");
        prp = (PricesPath) data.getSerializable("LinePath");
        CandlePrices = (int[][])data.getSerializable("Candles");
        prevX=0;
        rem=0;
        candles = new Candle[CandlePrices.length];
        SecLineReady = false;

        PriceControl pc = new PriceControl(this);

        for(int i=0;i<CandlePrices.length;i++){
            candles[i] = new Candle(CandlePrices[i][0],CandlePrices[i][1],CandlePrices[i][2],CandlePrices[i][3]);
            candles[i].setTooltip(pc.intDateToDate(Dates[i]));
        }

        setContentView(R.layout.activity_chart);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        //int widthPixels = prefs.getInt("width", 720);
        //int heightPixels = prefs.getInt("height", 720);

        mContentView = (ChartSurface)findViewById(R.id.surfaceView);
        mContentView.generate(this, name, candles, Dates, prp, this);
        mContentView.getHolder().addCallback(this);
        //CoordinatorLayout master = (CoordinatorLayout) findViewById(R.id.Master_Layout);

        HideHandler = new Handler();

        /*
        Drawable dr = getDrawable(R.drawable.candle);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        candleIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 150, true));
        dr = getDrawable(R.drawable.line);
        bitmap = ((BitmapDrawable) dr).getBitmap();
        lineIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 150, true));
        dr = getDrawable(R.drawable.draw);
        bitmap = ((BitmapDrawable) dr).getBitmap();
        drawIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 150, true));
        dr = getDrawable(android.R.drawable.ic_menu_save);
        bitmap = ((BitmapDrawable) dr).getBitmap();
        saveIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 150, true));
        */

        //master.addView(mContentView);

        mContentView.setOnTouchListener(this);
        hidden=true;

        swit = (FloatingActionButton)findViewById(R.id.swit);
        draw = (FloatingActionButton)findViewById(R.id.draw);
        save = (FloatingActionButton)findViewById(R.id.save);

        swit.setOnClickListener(switchClick);
        draw.setOnClickListener(drawClick);
        save.setOnClickListener(saveClick);

        //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(100,100);

        //DisplayMetrics metrics = new DisplayMetrics();
        //float density = metrics.density;

        //swit.setSize(FloatingActionButton.SIZE_NORMAL);
        //swit.setX(widthPixels*0.9f);
        //swit.setY(heightPixels*0.1f);
        //swit.setElevation(10*density);
        //master.addView(swit);
        //swit.setImageDrawable(candleIcon);

        //draw.setSize(FloatingActionButton.SIZE_NORMAL);
        //draw.setX(widthPixels*0.6f);
        //draw.setY(heightPixels*0.1f);
        //draw.setElevation(10*density);
        //master.addView(draw);
        //draw.setImageDrawable(drawIcon);


        //save.setSize(FloatingActionButton.SIZE_NORMAL);
        //save.setX(widthPixels*0.75f);
        //save.setY(heightPixels*0.1f);
        //save.setElevation(10*density);
        //master.addView(save);
        //save.setImageDrawable(saveIcon);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mContentView.finalizeSurface(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mContentView.setSystemUiVisibility(HIDE_FLAGS);
        if(candlesChart) mContentView.createCandleChart();
        else {
            if(SecLineReady){
                mContentView.createSecLineChart(pos1, pos2);
                mode=Modes.ViewLine;
            }
            else mContentView.createLineChart();
        }
    }

    View.OnClickListener switchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            candlesChart = !candlesChart;

            if(candlesChart) {
                SecLineReady=false;
                mContentView.createCandleChart();
                swit.setImageDrawable(getDrawable(R.drawable.line));
                draw.hide();
                mode=Modes.ViewCandles;
            }
            else {
                mContentView.createLineChart();
                swit.setImageDrawable(getDrawable(R.drawable.candle));
                draw.show();
                mode=Modes.ViewLine;
            }
        }
    };

    View.OnClickListener saveClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int permission = ActivityCompat.checkSelfPermission(ChartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        ChartActivity.this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
            mContentView.setUpViewShot();
        }
    };

    View.OnClickListener drawClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mode==Modes.ViewLine) {
                mode = Modes.Draw;
                Intent intent = new Intent(ChartActivity.this, DateSelector.class);
                Bundle extras = new Bundle();
                extras.putIntArray("Dates", Dates);
                extras.putSerializable("Candles", CandlePrices);
                intent.putExtras(extras);
                startActivityForResult(intent, 2); //0 and 1 are sometimes use by Android
            } else draw.hide();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case 2:
                    int[] d = resultIntent.getIntArrayExtra("Positions");
                    pos1=d[0];
                    pos2=d[1];
                    SecLineReady=true;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    private Runnable hide = new Runnable() {
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(HIDE_FLAGS);
            swit.hide();
            draw.hide();
            save.hide();
            hidden = true;
        }
    };

    private Runnable show = new Runnable() {
        @Override
        public void run() {
            swit.show();
            save.show();
            if(mode==Modes.ViewLine)draw.show();
            hidden=false;
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        HideHandler.post(hide);
        final int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if(candlesChart) {
                    prevX=event.getX();
                    mode=Modes.Select;
                    int i = mContentView.getCandleDataPos(event.getX());
                    if(i>=candles.length) i=candles.length-1;
                    else if(i<0) i=0;
                    toastText = candles[i].getTooltip();
                } else {
                    mode=Modes.Select;
                    mContentView.drawVertLine(event.getX());
                    int i = mContentView.getLineDataPos(event.getX());
                    if(i>=candles.length) i=candles.length-1;
                    else if(i<0) i=0;
                    toastText = candles[i].getTooltip();
                }
                return true;

            case MotionEvent.ACTION_UP:
                if(mode==Modes.Select) {
                    if (!toastText.isEmpty()) {
                        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
                        toastText = "";
                    }
                    if (candlesChart) {
                        mode = Modes.ViewCandles;
                        mContentView.createCandleChart();
                    } else {
                        mode = Modes.ViewLine;
                        mContentView.createLineChart();
                    }
                } else if (mode==Modes.Drag) {
                    rem = 0;
                    prevX = 0;
                    if (candlesChart) {
                        mode = Modes.ViewCandles;
                        mContentView.createCandleChart();
                    } else {
                        mode = Modes.ViewLine;
                    }
                }
                HideHandler.post(show);
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() -prevX +rem;
                if(Math.abs(dx)>MOVE_LIMIT) {
                    if (!candlesChart) {
                        mContentView.drawVertLine(event.getX());
                        int i = mContentView.getLineDataPos(event.getX());
                        if (i >= candles.length) i = candles.length - 1;
                        else if (i < 0) i = 0;
                        toastText = candles[i].getTooltip();
                        return true;
                    } else {
                        mode = Modes.Drag;
                        prevX = event.getX();
                        rem = mContentView.dragChart(dx);
                    }
                }

            default:
                return false;
        }
    }

    @Override
    public void ScreenshotCallBack(Bitmap bitmap) {
        File myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        int n = (int)Math.round(Math.random()*3500000);
        String fname = "/Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            boolean d = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            if(d) Toast.makeText(this, "ViewShot Saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri contentUri = Uri.fromFile(file);
        scanIntent.setData(contentUri);
        sendBroadcast(scanIntent);
    }



/*
    @Override
    protected void onResume() {
        super.onResume();
        try{
            if(candlesChart) mContentView.createCandleChart();
            else mContentView.createLineChart();
        } catch (NullPointerException f){
            finish();
        }
    }
*/
}



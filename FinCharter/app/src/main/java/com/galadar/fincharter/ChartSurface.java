package com.galadar.fincharter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Galadar on 4/1/2017.
 *
 */

public class ChartSurface extends SurfaceView {

    public static final int TickSize = 7;
    public ScreenShotCallback screenShotCallback;

    private Point p1;
    private Point p2;

    Color shadowed;
    PriceControl pc;
    PricesPath prp;
    boolean lineChart, SecLineDrawn;
    float SecLineEdgeBorder, SecLineEdgeWidth;
    int FirstPrintedCandle;
    String name;
    Candle[] candles;
    int[] Dates;
    boolean screenShot;
    Canvas ScreenShotCanvas;
    Bitmap bmp;
    SurfaceHolder h;

    public ChartSurface(Context context) {
        super(context);
    }

    public ChartSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChartSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void finalizeSurface(SurfaceHolder holder){
        h = holder;
    }

    public void setUpViewShot(){
        screenShot=true;
        if(lineChart) createLineChart();
        else createCandleChart();
    }

    public void generate(Context context, String n, Candle[] ca, int[] da, PricesPath pricesPath, ScreenShotCallback ssc) {
        screenShotCallback = ssc;



        candles = ca;
        Dates = da;
        prp = pricesPath;
        name=n;
        SecLineDrawn = false;

        int AxisX_Bottom = Dates[0];
        int AxisX_Top = Dates[Dates.length-1];
        int AxisY_Bottom = candles[0].getLow();
        int AxisY_Top = candles[0].getHigh();
        for(int i=0;i<candles.length;i++){
            if(AxisY_Top<candles[i].getHigh()){
                AxisY_Top=candles[i].getHigh();
            }
            if(AxisY_Bottom>candles[i].getLow()){
                AxisY_Bottom=candles[i].getLow();
            }
        }

        AxisY_Top = Math.round(AxisY_Top * 1.2f);
        AxisY_Bottom = Math.round(AxisY_Bottom*0.8f);
        AxisX_Top = Math.round(AxisX_Top * 1.2f);
        AxisX_Bottom = Math.round(AxisX_Bottom*0.8f);

        pc = new PriceControl(context);
        pc.setObj(AxisX_Top, AxisX_Bottom, AxisY_Top, AxisY_Bottom, ca.length);

        bmp = Bitmap.createBitmap(pc.getWidth(), pc.getHeight(), Bitmap.Config.ARGB_8888);
        ScreenShotCanvas = new Canvas(bmp);
        screenShot=false;

        FirstPrintedCandle = candles.length - getCurrentCandleAmount();

    }

    public void createCandleChart(){ //Price data HOCL, in that column order per lineChart
        lineChart = false;
        SecLineDrawn = false;
        Canvas c = h.lockCanvas();

        c.drawColor(Color.WHITE);
        c = DrawAxis(c);
        Paint p = new Paint();
        p.setARGB(100, 100, 100, 100);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(300.0f);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(name, pc.getWidth()*0.5f, pc.getHeight()*0.5f, p);
        c = drawCandleChart(c);
        h.unlockCanvasAndPost(c);

        if(screenShot){
            ScreenShotCanvas.drawColor(Color.WHITE);
            ScreenShotCanvas = DrawAxis(ScreenShotCanvas);
            ScreenShotCanvas.drawText(name, pc.getWidth()*0.5f, pc.getHeight()*0.5f, p);
            ScreenShotCanvas = drawCandleChart(ScreenShotCanvas);
            screenShotCallback.ScreenshotCallBack(bmp);
            screenShot=false;
        }

        //Top right of screen to Top price (AxisY_Top) and first date / data point (
    }

    public void createLineChart(){ //Price data per tick
        lineChart = true;
        Canvas c = h.lockCanvas();

        c.drawColor(Color.WHITE);
        c = DrawAxis(c);
        Paint p = new Paint();
        p.setARGB(100, 100, 100, 100);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(300.0f);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(name, pc.getWidth()*0.5f, pc.getHeight()*0.5f, p);

        c = drawLineChart(c);
        if(SecLineDrawn) c = drawSecLine(c);

        if(screenShot){
            ScreenShotCanvas.drawColor(Color.WHITE);
            ScreenShotCanvas = DrawAxis(ScreenShotCanvas);
            ScreenShotCanvas.drawText(name, pc.getWidth()*0.5f, pc.getHeight()*0.5f, p);
            ScreenShotCanvas = drawLineChart(ScreenShotCanvas);
            if(SecLineDrawn) ScreenShotCanvas = drawSecLine(ScreenShotCanvas);
            screenShotCallback.ScreenshotCallBack(bmp);
            screenShot=false;
        }

        h.unlockCanvasAndPost(c);
    }

    private Canvas DrawAxis(Canvas c) {
        Paint p = new Paint();
        p.setStrokeWidth(2.0f);
        p.setColor(Color.BLACK);
        int XPos = pc.getHORIZONTAL_BORDER()/2;
        int YPos = pc.getHeight()-pc.getVERTICAL_BORDER()/2;
        c.drawLine(-pc.getWidth(), YPos, 2*pc.getWidth(), YPos, p);     //X Axis for dates, with set Y position across all width
        c.drawLine(XPos, -pc.getHeight(), XPos, 2*pc.getHeight(),p);    //Y Axis for prices, with set X position, across all height

        int tickValue;
        float tickPos;
        Paint tickPaint = new Paint();
        tickPaint.setColor(Color.BLACK);
        tickPaint.setStrokeWidth(3.0f);
        tickPaint.setTextSize(16.0f);
        tickPaint.setTextAlign(Paint.Align.LEFT);

        int max = (int)Math.ceil(pc.getY_Top()/100);
        int min = (int)Math.floor(pc.getY_Bottom()/100);
        int diff = max-min;

        if(diff>400)diff=50;
        else if(diff>150)diff=25;
        else if(diff>50)diff=5;
        else diff=1;

        tickValue=0;

        while (tickValue<min){
            tickValue+=diff;
        }

        while (tickValue<=max){     //Y Axis Ticks
            tickPos = pc.PriceToYCoord(tickValue*100);
            c.drawLine(XPos-TickSize, tickPos, XPos+TickSize, tickPos, tickPaint);
            c.drawText(("$"+Integer.toString(tickValue)+" "), XPos-(pc.getHORIZONTAL_BORDER()/2), tickPos, tickPaint);
            tickValue+=diff;
        }

        //AXIS X Ticks (Dates)

        tickPaint.setTextAlign(Paint.Align.CENTER);

        if(lineChart){
            diff=Dates.length/5;
            int count=0;
            while (count<Dates.length){
                tickPos = pc.getHORIZONTAL_BORDER() + ((count*pc.getAvailWidth()) / Dates.length);
                c.drawLine(tickPos, YPos-TickSize, tickPos, YPos+TickSize, tickPaint);
                c.drawText(pc.intDateToDate(Dates[count]), tickPos, YPos+2*TickSize, tickPaint);
                count+=diff;
                count--;
            }
        }
        else {
            min = Dates[FirstPrintedCandle];
            max = Dates[FirstPrintedCandle+getCurrentCandleAmount()-1];
            diff = 3;
            tickValue=min;
            int count=FirstPrintedCandle;
            while (tickValue<=max){
                tickPos = pc.CandlePosToXAxisCoord(count, FirstPrintedCandle);
                c.drawLine(tickPos, YPos-TickSize, tickPos, YPos+TickSize, tickPaint);
                c.drawText(pc.intDateToDate(Dates[count]), tickPos, YPos+2*TickSize, tickPaint);
                count+=diff;
                if(count>=Dates.length){
                    tickValue=max+1;
                    continue;
                }
                tickValue=Dates[count];
            }
        }

        return c;
    }

    private Canvas drawCandleChart(Canvas c) {
        for(int i=0;i<getCurrentCandleAmount();i++){
            c.drawBitmap(drawCandle(candles[FirstPrintedCandle+i]), i*pc.getRectWidth()+pc.getHORIZONTAL_BORDER(), pc.getVERTICAL_BORDER(), null);
        }
        return c;
    }

    private Canvas drawCandleChart(Canvas c, float dx) {
        for(int i=0;i<getCurrentCandleAmount();i++){
            c.drawBitmap(drawCandle(candles[FirstPrintedCandle+i]), i*pc.getRectWidth()+pc.getHORIZONTAL_BORDER()+dx, pc.getVERTICAL_BORDER(), null);
        }
        return c;
    }

    public float dragChart(float dx) {
        Canvas c = h.lockCanvas();
        c.drawColor(Color.WHITE);
        if(dx>0) {
            if(FirstPrintedCandle>0) {
                while (dx > pc.getRectWidth()) {
                    dx -= pc.getRectWidth();
                    FirstPrintedCandle--;
                }
            }
            else dx=pc.getRectWidth()/2;
        } else {
            if(FirstPrintedCandle<(candles.length-getCurrentCandleAmount())) {
                while (-dx > pc.getRectWidth()) {
                    dx += pc.getRectWidth();
                    FirstPrintedCandle++;
                }
            } else dx=pc.getRectWidth()/2;
        }
        if(FirstPrintedCandle<0)FirstPrintedCandle=0;
        else if(FirstPrintedCandle>=(candles.length-getCurrentCandleAmount()))FirstPrintedCandle=candles.length-getCurrentCandleAmount()-1;
        c = DrawAxis(c);
        c = drawCandleChart(c, dx);

        h.unlockCanvasAndPost(c);
        return dx;
    }

    private Canvas drawLineChart(Canvas c){

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        float lastX, lastY, pri, loc;

        lastX = pc.getHORIZONTAL_BORDER()-1;
        lastY = pc.PriceToYCoord(candles[0].getClose());
        for (int i=0;i<candles.length;i++){
            pri = pc.PriceToYCoord(candles[i].getClose());
            loc = pc.LinePosToXCoord(i);
            c.drawLine(lastX,lastY,loc,pri,paint);
            lastX = loc;
            lastY = pri;
        }

        return c;
    }

    public void drawVertLine(float x){
        if(lineChart) {
            int pos = pc.LineXCoordToPos(x);
            float y = pc.PriceToYCoord(candles[pos].getClose());

            Canvas c = h.lockCanvas();
            c.drawColor(Color.WHITE);
            c = drawLineChart(c);

            Paint p = new Paint();
            p.setStrokeWidth(2.0f);
            p.setColor(Color.BLACK);
            c.drawLine(pc.LinePosToXCoord(pos), pc.getHeight()-1, pc.LinePosToXCoord(pos), y, p);

            p.setColor(Color.BLUE);
            c.drawCircle(pc.LinePosToXCoord(pos), y, 5.0f, p);

            p.setTextSize(16.0f);
            p.setColor(Color.RED);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText(pc.intDateToDate(Dates[pos]), pc.LinePosToXCoord(pos), pc.PriceToYCoord(candles[pos].getClose()/2), p);

            h.unlockCanvasAndPost(c);
        }

    }

    public void createSecLineChart(int pos1, int pos2){
        float x1 = pc.LinePosToXCoord(pos1);
        float x2 = pc.LinePosToXCoord(pos2);
        float y1 = pc.PriceToYCoord(candles[pos1].getClose());
        float y2 = pc.PriceToYCoord(candles[pos2].getClose());
        p1 = new Point(Math.round(x1), Math.round(y1));
        p2 = new Point(Math.round(x2), Math.round(y2));
        float lineA, lineB;

        lineA = (y2 - y1) / (x2 - x1);
        lineB = y1 - lineA * x1;

        SecLineEdgeBorder = lineA * pc.getHORIZONTAL_BORDER() + lineB;
        SecLineEdgeWidth = lineA * (pc.getWidth()-1) + lineB;
        SecLineDrawn = true;
        createLineChart();
    }

    private Canvas drawSecLine(Canvas c) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);

        c.drawLine(pc.getHORIZONTAL_BORDER(), SecLineEdgeBorder, pc.getWidth()-1, SecLineEdgeWidth, paint);
        c.drawCircle(p1.x, p1.y, 5.0f, paint);
        c.drawCircle(p2.x,p2.y, 5.0f, paint);

        return c;
    }

    private int getCurrentCandleAmount(){
        return (int)Math.round(Math.floor(pc.getAvailWidth()/pc.getRectWidth()));
    }

    public int getLineDataPos(float x) {
        return pc.LineXCoordToPos(x);
    }

    public int getCandleDataPos(float x){
        return pc.CandleXCoordToPos(x, FirstPrintedCandle);
    }

    private Bitmap drawCandle(Candle candle){
        Canvas c = new Canvas();
        int RTop = Math.round(pc.PriceToYCoord(Math.max(candle.getOpen(),candle.getClose()))); //pc.getAvailHeight()*(Math.max(candle.getOpen(),candle.getClose())-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());
        int RBottom = Math.round(pc.PriceToYCoord(Math.min(candle.getOpen(),candle.getClose()))); //pc.getAvailHeight()*(Math.min(candle.getOpen(),candle.getClose())-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());

        Bitmap CandleBmp = Bitmap.createBitmap(1+Math.round(pc.getRectWidth()), pc.getHeight(), Bitmap.Config.ALPHA_8);
        c.setBitmap(CandleBmp);

        c.drawARGB(0,0,0,0);

        Rect body = new Rect(Math.round(pc.getRectWidth()*0.15f), RTop, Math.round(pc.getRectWidth()*0.85f), RBottom);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3.0f);
        int ht = Math.round( pc.PriceToYCoord(candle.getHigh())); //pc.getAvailHeight()*(candle.getHigh()-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());
        c.drawLine(pc.getRectWidth()*0.5f, RTop, pc.getRectWidth()*0.5f, ht, paint);

        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.BLACK);
        fillPaint.setStrokeWidth(2.0f);
        if(candle.getOpen()>=candle.getClose())fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        else fillPaint.setStyle(Paint.Style.STROKE);
        c.drawRect(body, fillPaint);

        ht = Math.round( pc.PriceToYCoord(candle.getLow())); //pc.getAvailHeight()*(candle.getLow()-pc.getY_Bottom())/(pc.getY_Top()-pc.getY_Bottom());
        c.drawLine(pc.getRectWidth()*0.5f, RBottom, pc.getRectWidth()*0.5f, ht, paint);

        return CandleBmp;
    }



}

package com.galadar.fincharter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;


public class DateSelector extends AppCompatActivity {
    Spinner spinner1, spinner2;
    Button GetMax, GetMin, Done;
    ArrayList<Integer> LocalMaximaPos, LocalMinimaPos;

    ArrayList<String> DatesList;
    ArrayAdapter<String> DateAdapter;
    Candle[] candles;

    int pos1, pos2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_date_selector);

        PriceControl pc = new PriceControl(this);

        int[][] CandlePrices = (int[][]) getIntent().getExtras().getSerializable("Candles");
        final int[] Dates = getIntent().getExtras().getIntArray("Dates");

        candles = new Candle[CandlePrices.length];
        LocalMinimaPos = new ArrayList<>();
        LocalMaximaPos = new ArrayList<>();

        for(int i=0;i<CandlePrices.length;i++){
            candles[i] = new Candle(CandlePrices[i][0],CandlePrices[i][1],CandlePrices[i][2],CandlePrices[i][3]);
            candles[i].setTooltip(pc.intDateToDate(Dates[i]));
        }

        for (int i = 1; i < candles.length-1; i++) {
            if(candles[i].getClose()>getPrev(i) && candles[i].getClose()>getNext(i))LocalMaximaPos.add(i);
            if(candles[i].getClose()<getPrev(i) && candles[i].getClose()<getNext(i))LocalMinimaPos.add(i);
        }

        for (int LC : LocalMaximaPos) {
            System.out.println("Maxima: "+pc.intDateToDate(Dates[LC]));
        }
        for (int LC : LocalMinimaPos) {
            System.out.println("Minima: "+pc.intDateToDate(Dates[LC]));
        }

        DatesList = new ArrayList<>();

        for (int i = 0; i < Dates.length; i++) {
            DatesList.add(pc.intDateToDate(Dates[i]));
        }

        DateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, DatesList);

        spinner1 = (Spinner)findViewById(R.id.Date1);
        spinner1.setAdapter(DateAdapter);

        spinner2 = (Spinner)findViewById(R.id.Date2);
        spinner2.setAdapter(DateAdapter);

        pos1=0;
        spinner1.setSelection(pos1);
        pos2=1;
        spinner2.setSelection(pos2);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos1 = position;
                Done.setEnabled(pos1!=pos2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pos1=0;
                spinner1.setSelection(pos1);
                Done.setEnabled(pos1!=pos2);
            }

        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos2 = position;
                Done.setEnabled(pos1!=pos2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pos2=1;
                spinner1.setSelection(pos2);
                Done.setEnabled(pos1!=pos2);
            }
        });


        GetMax = (Button)findViewById(R.id.getMax);
        GetMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int first = Integer.MIN_VALUE;
                int second = Integer.MIN_VALUE;

                for (int i = 0; i < LocalMaximaPos.size()-1; i++) {
                    if(candles[LocalMaximaPos.get(i)].getClose()>=first){
                        pos2=pos1;
                        second=first;
                        pos1=LocalMaximaPos.get(i);
                        first = candles[pos1].getClose();
                    } else if(candles[LocalMaximaPos.get(i)].getClose()>second){
                        pos2=LocalMaximaPos.get(i);
                        second=candles[pos2].getClose();
                    }
                }
                spinner1.setSelection(pos1);
                spinner2.setSelection(pos2);
                Done.setEnabled(pos1!=pos2);
            }
        });

        GetMin = (Button)findViewById(R.id.getMin);
        GetMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int first = Integer.MAX_VALUE;
                int second = Integer.MAX_VALUE;

                for (int i = 0; i < LocalMinimaPos.size()-1; i++) {
                    if(candles[LocalMinimaPos.get(i)].getClose()<=first){
                        pos2=pos1;
                        second=first;
                        pos1=LocalMinimaPos.get(i);
                        first = candles[pos1].getClose();
                    } else if(candles[LocalMinimaPos.get(i)].getClose()<second){
                        pos2=LocalMinimaPos.get(i);
                        second=candles[pos2].getClose();
                    }
                }
                spinner1.setSelection(pos1);
                spinner2.setSelection(pos2);
                Done.setEnabled(pos1!=pos2);
            }
        });

        Done = (Button)findViewById(R.id.Done);
        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.isEnabled()) {
                    Intent intent = new Intent();
                    int[] results = {pos1, pos2};
                    intent.putExtra("Positions", results);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }

    private int getNext(int pos) {
        if(pos==candles.length-1) return candles[candles.length-1].getClose();
        if(candles[pos].getClose()==candles[pos+1].getClose()) return getNext(pos+1);
        return candles[pos+1].getClose();
    }

    private int getPrev(int pos) {
        if(pos==0) return candles[0].getClose();
        if(candles[pos].getClose()==candles[pos-1].getClose()) return getPrev(pos-1);
        return candles[pos-1].getClose();
    }

}

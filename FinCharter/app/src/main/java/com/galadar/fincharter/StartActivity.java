package com.galadar.fincharter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.WindowManager;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;


        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if(!prefs.contains("limit")){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("limit", 1000); //This is the upper limit for what the user cat set how many dates to retrieve
            editor.putInt("current", 500); //This is how many values to retrieve by default
            editor.putInt("default", 500); //use this if user enters negative value
            editor.putInt("width", width);
            editor.putInt("height", height);
            editor.putInt("dateformat",0);
            editor.commit();
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("width", width);
            editor.putInt("height", height);
            editor.commit();
        }

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ModelControl_List MDL = new ModelControl_List(this);
        System.out.println("On Post Create");
        MDL.execute();
    }

    void AllDone(){
        finish();
    }

}

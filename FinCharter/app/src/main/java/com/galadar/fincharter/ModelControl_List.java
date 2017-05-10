package com.galadar.fincharter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Galadar on 15/1/2017.
 *
 * Use this to transmit the data request to the server to get an ArrayList<String> with Share Names
 */

class ModelControl_List extends AsyncTask<Void, Void, ArrayList<String>>{

    private Context ActivityCon;
    ProgressBar pbar;

    public ModelControl_List(Context con){
        ActivityCon = con;
        pbar = new ProgressBar(con);
        pbar.setIndeterminate(true);
    }


    private static ArrayList<String> getSharesList(){
        //Random r = new Random();
        int size = 15;
        ArrayList<String> names = new ArrayList<>();

        for(int i=0;i<size; i++){
            names.add(getShareName());
        }

        return names;
    }

    private static String getShareName(){

        Random r = new Random();
        String str = "";

        char c = (char) (r.nextInt(26) + 'A');
        str += c;
        c = (char) (r.nextInt(26) + 'A');
        str += c;
        c = (char) (r.nextInt(26) + 'A');
        str += c;
        c = (char) (r.nextInt(26) + 'A');
        str += c;

        return str;

    }

    @Override
    protected void onPreExecute() {
        pbar.setVisibility(View.VISIBLE);
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        return getSharesList();
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        Intent intent = new Intent(ActivityCon, ShareList.class);
        Bundle data = new Bundle();
        data.putStringArrayList("names", strings);
        intent.putExtras(data);
        pbar.setVisibility(View.GONE);
        ActivityCon.startActivity(intent);
    }
}

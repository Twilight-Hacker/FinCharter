package com.galadar.fincharter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    int limit, current, deflt, sel;
    EditText editText;
    ArrayList<String> formats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        limit = prefs.getInt("limit", 1000);
        deflt = prefs.getInt("default", 500);
        current = prefs.getInt("current", deflt);
        sel = prefs.getInt("dateformat", 0);

        editText = (EditText)findViewById(R.id.lim);

        Button ok = (Button)findViewById(R.id.ok);
        ok.setOnClickListener(this);

        editText.setText(Integer.toString(current));

        Spinner spinner = (Spinner)findViewById(R.id.dateformat);
        formats = new ArrayList<>();
        formats.add("DD/MM/YYYY");
        formats.add("MM/DD/YYYY");
        formats.add("YYYY/MM/DD");

        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formats));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sel=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(sel);
            }
        });

    }

    private void editLimitPref(int a){
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("current", a);
        editor.apply();
    }

    private void editDateFormatPref(int a){
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("dateformat", a);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        int a = Integer.parseInt(editText.getText().toString());
        if(a>limit){
            editText.setText(Integer.toString(limit));
            Toast.makeText(this, "Input Value too high. Resetting to maximum.", Toast.LENGTH_SHORT).show();
        } else if (a<100){
            editText.setText(Integer.toString(deflt));
            Toast.makeText(this, "Input Value too low. Resetting to default.", Toast.LENGTH_SHORT).show();
        } else {
            editLimitPref(a);
            editDateFormatPref(sel);
            finish();
        }

    }
}

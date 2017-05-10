package com.galadar.fincharter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ShareList extends AppCompatActivity implements AdapterView.OnItemClickListener{

    ShareListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView sList = (ListView)findViewById(R.id.ShareList);
        ArrayList<String> names = getIntent().getExtras().getStringArrayList("names");
        adapter = new ShareListAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, names);
        sList.setAdapter(adapter);
        sList.setOnItemClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chart, menu);
        return true;    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Settings:
                Intent intent = new Intent(ShareList.this, Settings.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        ShareListAdapter a = (ShareListAdapter) parent.getAdapter();
        String name = a.getShareName(position);
        ModelControl MD = new ModelControl(this);
        String[] nameArray = new String[1];
        nameArray[0] = name;
        MD.execute(nameArray);
    }
}

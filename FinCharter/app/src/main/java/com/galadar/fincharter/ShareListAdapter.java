package com.galadar.fincharter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Galadar on 12/1/2017.
 *
 */

class ShareListAdapter extends ArrayAdapter {
    private ArrayList<String> names;

    public ShareListAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull ArrayList<String> objects) {
        super(context, resource, textViewResourceId, objects);
        names = objects;
    }

    String getShareName(int position){
        return names.get(position);
    }

    int getNoOfShares(){
        return names.size();
    }
}

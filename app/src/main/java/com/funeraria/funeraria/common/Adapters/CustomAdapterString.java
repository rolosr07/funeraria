package com.funeraria.funeraria.common.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.funeraria.funeraria.common.entities.Difunto;

import java.util.List;

/**
 * Created by Rolo on 25/03/2017.
 */
public class CustomAdapterString extends ArrayAdapter {

    private Context context;
    private List<String> itemList;

    public CustomAdapterString(Context context, int textViewResourceId, List<String> itemList) {

        super(context, textViewResourceId, itemList);
        this.context=context;
        this.itemList=itemList;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        String value = itemList.get(position);
        v.setText(value);

        return v;
    }

    public TextView getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        String value = itemList.get(position);
        v.setText(value);
        return v;
    }
}

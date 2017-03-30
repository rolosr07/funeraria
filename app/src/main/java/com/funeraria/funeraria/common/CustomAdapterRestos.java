package com.funeraria.funeraria.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rolo on 25/03/2017.
 */
public class CustomAdapterRestos extends ArrayAdapter {

    private Context context;
    private List<Restos> itemList;

    public CustomAdapterRestos(Context context, int textViewResourceId, List<Restos> itemList) {

        super(context, textViewResourceId, itemList);
        this.context=context;
        this.itemList=itemList;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Restos restos = itemList.get(position);
        v.setText(restos.getNombre());
        return v;
    }

    public TextView getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Restos restos = itemList.get(position);
        v.setText(restos.getNombre());
        return v;
    }
}

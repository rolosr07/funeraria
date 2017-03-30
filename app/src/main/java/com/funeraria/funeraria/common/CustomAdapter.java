package com.funeraria.funeraria.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.funeraria.funeraria.common.Difunto;

import java.util.List;

/**
 * Created by Rolo on 25/03/2017.
 */
public class CustomAdapter extends ArrayAdapter {

    private Context context;
    private List<Difunto> itemList;

    public CustomAdapter(Context context, int textViewResourceId,List<Difunto> itemList) {

        super(context, textViewResourceId, itemList);
        this.context=context;
        this.itemList=itemList;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Difunto difunto = itemList.get(position);
        v.setText(difunto.getNombre() + " " + difunto.getApellidos());

        return v;
    }

    public TextView getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Difunto difunto = itemList.get(position);
        v.setText(difunto.getNombre() + " " + difunto.getApellidos());
        return v;
    }
}

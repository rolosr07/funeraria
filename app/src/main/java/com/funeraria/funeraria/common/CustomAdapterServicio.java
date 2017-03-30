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
public class CustomAdapterServicio extends ArrayAdapter {

    private Context context;
    private List<Servicio> itemList;

    public CustomAdapterServicio(Context context, int textViewResourceId, List<Servicio> itemList) {

        super(context, textViewResourceId, itemList);
        this.context=context;
        this.itemList=itemList;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Servicio servicio = itemList.get(position);
        v.setText(servicio.getNombre());

        return v;
    }

    public TextView getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Servicio servicio = itemList.get(position);
        v.setText(servicio.getNombre());
        return v;
    }
}

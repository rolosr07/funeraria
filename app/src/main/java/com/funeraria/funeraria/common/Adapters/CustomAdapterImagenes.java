package com.funeraria.funeraria.common.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.funeraria.funeraria.common.entities.Imagen;

import java.util.List;

/**
 * Created by Rolo on 25/03/2017.
 */
public class CustomAdapterImagenes extends ArrayAdapter {

    private Context context;
    private List<Imagen> itemList;

    public CustomAdapterImagenes(Context context, int textViewResourceId, List<Imagen> itemList) {

        super(context, textViewResourceId, itemList);
        this.context=context;
        this.itemList=itemList;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Imagen imagen = itemList.get(position);
        v.setText(imagen.getNombre());

        return v;
    }

    public TextView getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Imagen difunto = itemList.get(position);
        v.setText(difunto.getNombre());
        return v;
    }
}

package com.funeraria.funeraria.common.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.funeraria.funeraria.common.entities.Imagen;
import com.funeraria.funeraria.common.entities.Usuario;

import java.util.List;

/**
 * Created by Rolo on 25/03/2017.
 */
public class CustomAdapterUsuarios extends ArrayAdapter {

    private Context context;
    private List<Usuario> itemList;

    public CustomAdapterUsuarios(Context context, int textViewResourceId, List<Usuario> itemList) {

        super(context, textViewResourceId, itemList);
        this.context=context;
        this.itemList=itemList;
    }

    public TextView getView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Usuario usuario = itemList.get(position);
        v.setText(usuario.getNombre()+" "+ usuario.getApellido());

        return v;
    }

    public TextView getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView v = (TextView) super.getView(position, convertView, parent);
        Usuario usuario = itemList.get(position);
        v.setText(usuario.getNombre() +" "+ usuario.getApellido());
        return v;
    }
}

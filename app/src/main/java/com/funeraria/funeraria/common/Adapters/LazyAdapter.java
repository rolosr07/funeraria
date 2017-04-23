package com.funeraria.funeraria.common.Adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.funeraria.funeraria.R;
import com.funeraria.funeraria.common.entities.Servicio;

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private List<Servicio> list;
    private static LayoutInflater inflater=null;


    public LazyAdapter(Activity a, List lt) {
        activity = a;
        this.list = lt;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name
        TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image

        Servicio serv = list.get(position);

        byte[] decodedString = Base64.decode(serv.getImagen(), Base64.DEFAULT);
        Glide.with(activity).load(decodedString).into(thumb_image);

        title.setText("Comprador: " + serv.getNombreUsuario() + " " + serv.getApellidoUsuario());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(serv.getFechaCompra());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        artist.setText("Fecha: " + DateFormat.format("dd-MM-yyyy", convertedDate) + "");
        duration.setText("Precio: €" + serv.getPrecio());

        return vi;
    }
}

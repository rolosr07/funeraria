package com.funeraria.funeraria.common.Adapters;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LazyAdapterComprar extends BaseAdapter {

    private Activity activity;
    private List<Servicio> list;
    private static LayoutInflater inflater=null;


    public LazyAdapterComprar(Activity a, List lt) {
        activity = a;
        this.list = lt;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row_comprar, null);

        TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image

        Servicio serv = list.get(position);

        byte[] decodedString = Base64.decode(serv.getImagen(), Base64.DEFAULT);
        Glide.with(activity).load(decodedString).into(thumb_image);

        duration.setText("Precio: €" + serv.getPrecio());

        return vi;
    }
}

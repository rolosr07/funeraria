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

public class LazyAdapterMensajes extends BaseAdapter {

    private Activity activity;
    private List<Servicio> list;
    private static LayoutInflater inflater=null;


    public LazyAdapterMensajes(Activity a, List lt) {
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
            vi = inflater.inflate(R.layout.list_row_mensajes, null);

        TextView nombre = (TextView)vi.findViewById(R.id.nombre);
        TextView txMensaje = (TextView)vi.findViewById(R.id.txMensaje);
        TextView txNombreUsuario = (TextView)vi.findViewById(R.id.txNombreUsuario);
        TextView txFechaCompra = (TextView)vi.findViewById(R.id.txFechaCompra);
        TextView txAutorizado = (TextView)vi.findViewById(R.id.txAutorizado);
        TextView buttonAutorizar = (TextView)vi.findViewById(R.id.buttonAutorizar);

        Servicio servicio = list.get(position);

        nombre.setText(servicio.getNombre());
        txMensaje.setText(servicio.getTexto());
        txNombreUsuario.setText("Enviado por: "+servicio.getNombreUsuario() + " " + servicio.getApellidoUsuario());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(servicio.getFechaCompra());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txFechaCompra.setText("Fecha: " + DateFormat.format("dd-MM-yyyy", convertedDate) + "");

        if(servicio.getAutorizado().equals("1")){
            txAutorizado.setText("Autorizado: Si");
            buttonAutorizar.setText("Presione para No publicar");

        }else{
            txAutorizado.setText("Autorizado: No");
            buttonAutorizar.setText("Presione para publicar");
        }

        return vi;
    }
}

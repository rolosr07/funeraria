package com.funeraria.funeraria.common.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.funeraria.funeraria.ComprarVelasActivity;
import com.funeraria.funeraria.R;
import com.funeraria.funeraria.common.entities.Imagen;
import com.funeraria.funeraria.common.entities.Servicio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ZeptooUser on 27/03/2017.
 */

public class CustomPagerServicesAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Servicio> arrayList;

    public CustomPagerServicesAdapter(Context context, List<Servicio> arrayList) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        TextView txNombre = (TextView) itemView.findViewById(R.id.txNombre);
        TextView txMensaje = (TextView) itemView.findViewById(R.id.txMensaje);

        byte[] decodedString = Base64.decode(this.arrayList.get(position).getImagen(), Base64.DEFAULT);

        Glide.with(mContext).load(decodedString).into(imageView);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(this.arrayList.get(position).getFechaCompra());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String fecha = DateFormat.format("dd-MM-yyyy", convertedDate).toString();

        if(this.arrayList.get(position).getIdTipoServicio()==4){
            txNombre.setText(this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario()+".");
            txMensaje.setText("Le envia un ramo de flores el \n"+fecha+".");
        }else if(this.arrayList.get(position).getIdTipoServicio()==3){
            txNombre.setText(this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario());
            txMensaje.setText("Le enciende unas velas el \n"+fecha+".");
        }else{
            txNombre.setText("Enviado por: "+this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario());
            txMensaje.setText("Le envia un presente el \n"+fecha+".");
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

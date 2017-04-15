package com.funeraria.funeraria.common.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
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

        if(this.arrayList.get(position).getIdTipoServicio()==4){
            txNombre.setText(this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario()+".");
            txMensaje.setText("Le envia un ramo de flores el "+this.arrayList.get(position).getFechaCompra()+".");
        }else if(this.arrayList.get(position).getIdTipoServicio()==3){
            txNombre.setText(this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario());
            txMensaje.setText("Le enciende unas velas el "+this.arrayList.get(position).getFechaCompra()+".");
        }else{
            txNombre.setText("Enviado por: "+this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario());
            txMensaje.setText("Le envia un presente el "+this.arrayList.get(position).getFechaCompra()+".");
        }

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

package com.funeraria.funeraria.common.Adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.funeraria.funeraria.R;
import com.funeraria.funeraria.common.entities.Servicio;

import java.util.List;

/**
 * Created by ZeptooUser on 27/03/2017.
 */

public class CustomPagerServicesMensajesAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Servicio> arrayList;

    public CustomPagerServicesMensajesAdapter(Context context, List<Servicio> arrayList) {
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item_mensajes, container, false);


        TextView txNombre1 = (TextView) itemView.findViewById(R.id.txNombre1);
        TextView txTexto1 = (TextView) itemView.findViewById(R.id.txTexto1);

        TextView txNombre2 = (TextView) itemView.findViewById(R.id.txNombre2);
        TextView txTexto2 = (TextView) itemView.findViewById(R.id.txTexto2);

        TextView txNombre3 = (TextView) itemView.findViewById(R.id.txNombre3);
        TextView txTexto3 = (TextView) itemView.findViewById(R.id.txTexto3);

        txNombre1.setText(this.arrayList.get(position).getNombreUsuario()+" "+this.arrayList.get(position).getApellidoUsuario()+".");
        txTexto1.setText(this.arrayList.get(position).getTexto());

        if(this.arrayList.size()> (position+1)) {
            txNombre2.setText(this.arrayList.get(position + 1).getNombreUsuario() + " " + this.arrayList.get(position + 1).getApellidoUsuario() + ".");
            txTexto2.setText(this.arrayList.get(position + 1).getTexto());

            if(this.arrayList.size()> (position+2)) {
                txNombre3.setText(this.arrayList.get(position + 2).getNombreUsuario() + " " + this.arrayList.get(position + 2).getApellidoUsuario() + ".");
                txTexto3.setText(this.arrayList.get(position + 2).getTexto());
            }
        }
        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

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

public class CustomPagerServicesPlacaAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<Servicio> arrayList;

    public CustomPagerServicesPlacaAdapter(Context context, List<Servicio> arrayList) {
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item_placa, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        TextView txNombre = (TextView) itemView.findViewById(R.id.txNombre);

        byte[] decodedString = Base64.decode(this.arrayList.get(position).getImagen(), Base64.DEFAULT);

        Glide.with(mContext).load(decodedString).into(imageView);

        txNombre.setText(this.arrayList.get(position).getNombre());

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

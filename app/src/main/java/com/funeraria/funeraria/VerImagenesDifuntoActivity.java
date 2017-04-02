package com.funeraria.funeraria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.Adapters.CustomPagerAdapter;
import com.funeraria.funeraria.common.entities.Imagen;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.util.List;


public class VerImagenesDifuntoActivity extends Base {

    private TextView tvNombre;

    private String webResponseImages = "";

    private Thread thread;
    private Handler handler = new Handler();
    private int page = 0;
    private int delay = 5000; //milliseconds
    private ViewPager pager;
    private CustomPagerAdapter mCustomPagerAdapter;

    private final String METHOD_NAME_GET_IMAGENES_LIST = "getImagenesDifuntoList";

    private int idDifunto = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_imagenes_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvNombre = (TextView)findViewById(R.id.tvNombre);
        pager = (ViewPager) findViewById(R.id.pager);

        showProgress(true);
        String nombreDifunto = "";

        if(getIntent().getExtras().containsKey("nombreDifunto")){
            nombreDifunto = getIntent().getExtras().getString("nombreDifunto");
        }
        tvNombre.setText(nombreDifunto);

        if(getIntent().getExtras().containsKey("idDifunto")){
            idDifunto = getIntent().getExtras().getInt("idDifunto");
        }

        loadImagenesList(idDifunto);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(VerImagenesDifuntoActivity.this, VerPlacaActivity.class);
                i.putExtra("idDifunto", idDifunto);
                finish();
                startActivity(i);
            }
        }, 20000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.registrar_difundo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void loadImagenesList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_IMAGENES_LIST);
                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponseImages = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIImages);
            }
        };

        thread.start();
    }

    final Runnable createUIImages = new Runnable() {

        public void run(){

            if(!webResponseImages.equals("") && !webResponseImages.equals("[]")){
                showProgress(false);

                Type collectionType = new TypeToken<List<Imagen>>(){}.getType();
                List<Imagen> lcs = new Gson().fromJson( webResponseImages , collectionType);

                mCustomPagerAdapter = new CustomPagerAdapter(VerImagenesDifuntoActivity.this, lcs);
                pager.setAdapter(mCustomPagerAdapter);

                handler.postDelayed(runnable, delay);

            }else{
                showProgress(false);
                tvNombre.setText("Cantidad de Imagenes: "+0);
            }
        }
    };

    Runnable runnable = new Runnable() {
        public void run() {
            if (mCustomPagerAdapter.getCount() == page) {
                page = 0;
            } else {
                page++;
            }
            pager.setCurrentItem(page, true);
            handler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        finishAffinity();
        finish();
    }
}

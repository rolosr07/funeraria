package com.funeraria.funeraria;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.funeraria.funeraria.common.Adapters.CustomPagerAdapter;
import com.funeraria.funeraria.common.Adapters.CustomPagerServicesAdapter;
import com.funeraria.funeraria.common.Adapters.CustomPagerServicesMensajesAdapter;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Imagen;
import com.funeraria.funeraria.common.entities.Servicio;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.util.List;


public class VerImagenesYMensajesActivity extends Base {

    private TextView tvNombre;

    private String webResponseImages = "";
    private String webResponseMensajes = "";
    private String webResponseFlores = "";

    private Thread thread;
    private Handler handler = new Handler();
    private int page = 0;
    private int pageFlores = 0;
    private int delay = 5000; //milliseconds
    private ViewPager pager;
    private ViewPager pagerMensajes;
    private CustomPagerAdapter mCustomPagerAdapter;
    private CustomPagerServicesMensajesAdapter mCustomPagerAdapterMensajes;
    private ViewPager pagerFlores;
    private CustomPagerServicesAdapter mCustomPagerAdapterFlores;

    private final String METHOD_NAME_GET_IMAGENES_LIST = "getImagenesDifuntoList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST_MENSAJES = "getServiciosPorIdDifuntoYTipoDeServicioMensajesList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoFloresYVelas";

    private int idDifunto = 0;
    private String nombreDifunto = "";

    private int duration = 30000;

    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_imagenes_y_mensajes);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvNombre = (TextView)findViewById(R.id.tvNombre);
        pager = (ViewPager) findViewById(R.id.pager);
        pagerMensajes = (ViewPager) findViewById(R.id.pagerMensajes);
        pagerFlores = (ViewPager) findViewById(R.id.pagerFlores);

        mPlayer = MediaPlayer.create(VerImagenesYMensajesActivity.this, R.raw.music);
        //mPlayer.start();

        if(getIntent().getExtras().containsKey("nombreDifunto")){
            nombreDifunto = getIntent().getExtras().getString("nombreDifunto");
        }
        tvNombre.setText(nombreDifunto);

        if(getIntent().getExtras().containsKey("idDifunto")){
            idDifunto = getIntent().getExtras().getInt("idDifunto");
        }

        showProgress(true);
        loadImagenesList(idDifunto);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(VerImagenesYMensajesActivity.this, VerPlacaActivity.class);
                i.putExtra("idDifunto", idDifunto);
                i.putExtra("nombreDifunto", nombreDifunto);
                finish();
                startActivity(i);
            }
        }, duration);
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

                if((lcs.size()*delay) > duration){
                    duration = lcs.size()*delay;
                }

                mCustomPagerAdapter = new CustomPagerAdapter(VerImagenesYMensajesActivity.this, lcs);
                pager.setAdapter(mCustomPagerAdapter);
                showProgress(true);
                loadMensajesList(idDifunto);
                handler.postDelayed(runnable, delay);

            }else{
                showProgress(false);
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
        handler.removeCallbacks(runnable);
        finishAffinity();
        finish();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        finishAffinity();
        finish();
        super.onDestroy();
    }

    public void loadMensajesList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_SERVICIOS_LIST_MENSAJES);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SERVICIO);

                    androidHttpTransport.call(SOAP_ACTION_SERVICIO, envelope);
                    Object response = envelope.getResponse();
                    webResponseMensajes = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIMensajes);
            }
        };

        thread.start();
    }

    final Runnable createUIMensajes = new Runnable() {

        public void run(){

            if(!webResponseMensajes.equals("") && !webResponseMensajes.equals("[]")){
                showProgress(false);

                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> lcs = new Gson().fromJson( webResponseMensajes , collectionType);

                if((lcs.size()*delay) > duration){
                    duration = lcs.size()*delay;
                }

                mCustomPagerAdapterMensajes = new CustomPagerServicesMensajesAdapter(VerImagenesYMensajesActivity.this, lcs);
                pagerMensajes.setAdapter(mCustomPagerAdapterMensajes);

                showProgress(true);
                loadFloresList(idDifunto);

                handler.postDelayed(runnable, delay);

            }else{
                showProgress(false);
            }
        }
    };

    public void loadFloresList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_SERVICIOS_LIST);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SERVICIO);

                    androidHttpTransport.call(SOAP_ACTION_SERVICIO, envelope);
                    Object response = envelope.getResponse();
                    webResponseFlores = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIFlores);
            }
        };

        thread.start();
    }

    final Runnable createUIFlores = new Runnable() {

        public void run(){

            if(!webResponseFlores.equals("") && !webResponseFlores.equals("[]")){
                showProgress(false);

                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> lcs = new Gson().fromJson(webResponseFlores, collectionType);

                if((lcs.size()*delay) > duration){
                    duration = lcs.size()*delay;
                }

                mCustomPagerAdapterFlores = new CustomPagerServicesAdapter(VerImagenesYMensajesActivity.this, lcs);
                pagerFlores.setAdapter(mCustomPagerAdapterFlores);

                handler.postDelayed(runnable2, delay);

            }else{
                showProgress(false);
            }
        }
    };

    Runnable runnable2 = new Runnable() {
        public void run() {
            if (mCustomPagerAdapterFlores.getCount() == pageFlores) {
                pageFlores = 0;
            } else {
                pageFlores++;
            }
            pagerFlores.setCurrentItem(pageFlores, true);
            handler.postDelayed(this, delay);
        }
    };
}

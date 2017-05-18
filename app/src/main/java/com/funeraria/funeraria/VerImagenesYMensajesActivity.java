package com.funeraria.funeraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.List;


public class VerImagenesYMensajesActivity extends Base {

    private TextView tvNombre;

    private String webResponseValidarDescarga = "";

    private Thread thread;
    private Handler handler = new Handler();

    private int pageImagenes = 0;
    private int pageMensajes = 0;
    private int pageFlores = 0;
    private int delay = 5000; //milliseconds

    private ViewPager pagerImagenes;
    private ViewPager pagerMensajes;
    private ViewPager pagerFlores;

    private CustomPagerAdapter mCustomPagerAdapterImagenes;
    private CustomPagerServicesMensajesAdapter mCustomPagerAdapterMensajes;
    private CustomPagerServicesAdapter mCustomPagerAdapterFlores;

    private final String METHOD_NAME_GET_IMAGENES_LIST = "getImagenesDifuntoList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST_MENSAJES = "getServiciosPorIdDifuntoYTipoDeServicioMensajesList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoFloresYVelas";
    private final String METHOD_NAME_VALIDAR_DESCARGA = "placaInformationNeedDownload";

    private int duration = 20000;

    private MediaPlayer mPlayer;

    private List<Imagen> imagenList = new ArrayList<Imagen>();
    private List<Servicio> listMensajes = new ArrayList<Servicio>();
    private List<Servicio> listFloresVelas = new ArrayList<Servicio>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_imagenes_y_mensajes);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvNombre = (TextView)findViewById(R.id.tvNombre);

        pagerImagenes = (ViewPager) findViewById(R.id.pager);
        pagerMensajes = (ViewPager) findViewById(R.id.pagerMensajes);
        pagerFlores = (ViewPager) findViewById(R.id.pagerFlores);

        mPlayer = MediaPlayer.create(VerImagenesYMensajesActivity.this, R.raw.music);
        //mPlayer.start();

        tvNombre.setText(getCurrentUser().getNombreDifunto());

        if(getCurrentImagenes() == null){
            showProgress(true);
            loadImagenesList(getCurrentUser().getIdDifunto());
        }else{
            imagenList = getCurrentImagenes();
        }

        if(getCurrentMensajes() == null){
            showProgress(true);
            loadMensajesList(getCurrentUser().getIdDifunto());
        }else{
            listMensajes = getCurrentMensajes();
        }

        if(getCurrentFloresYVelas() == null){
            showProgress(true);
            loadFloresList(getCurrentUser().getIdDifunto());
        }else{
            listFloresVelas = getCurrentFloresYVelas();
        }

        if(getCurrentImagenes() != null || getCurrentMensajes() != null || getCurrentFloresYVelas()!= null){
            cargarInformacionThread();
        }

        validarDescarga(getCurrentUser().getIdDifunto());
    }

    public void validarDescarga(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_VALIDAR_DESCARGA);

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
                    webResponseValidarDescarga = response.toString();

                    if(!webResponseValidarDescarga.equals("") && Boolean.parseBoolean(webResponseValidarDescarga)) {
                        setListImagen(null);
                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString(IMAGENES_DATA, "").apply();
                        setListMensajes(null);
                        prefs.edit().putString(MENSAJES_DATA, "").apply();
                        setListFloresYVelas(null);
                        prefs.edit().putString(FLORES_Y_VELAS_DATA, "").apply();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        thread.start();
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
                    String webResponseImages = response.toString();

                    if(!webResponseImages.equals("") && !webResponseImages.equals("[]")){
                        Type collectionType = new TypeToken<List<Imagen>>(){}.getType();
                        imagenList = new Gson().fromJson( webResponseImages , collectionType);

                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString(IMAGENES_DATA, webResponseImages).apply();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        thread.start();
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
                    String webResponseMensajes = response.toString();

                    if(!webResponseMensajes.equals("") && !webResponseMensajes.equals("[]")){

                        Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                        listMensajes = new Gson().fromJson( webResponseMensajes , collectionType);

                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString(MENSAJES_DATA, webResponseMensajes).apply();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

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
                    String webResponseFlores = response.toString();

                    if(!webResponseFlores.equals("") && !webResponseFlores.equals("[]")) {

                        Type collectionType = new TypeToken<List<Servicio>>() {}.getType();
                        listFloresVelas = new Gson().fromJson(webResponseFlores, collectionType);

                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString(FLORES_Y_VELAS_DATA, webResponseFlores).apply();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                handler.post(createUI);
            }
        };

        thread.start();
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

    @Override
    protected void onPause() {
        handler.removeCallbacksAndMessages(runnableImagenes);
        handler.removeCallbacksAndMessages(runnableMensajes);
        handler.removeCallbacksAndMessages(runnableFlores);
        finishAffinity();
        finish();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        finishAffinity();
        finish();
        super.onDestroy();
    }

    public void cargarInformacionThread(){
        thread = new Thread(){
            public void run(){
                handler.post(createUI);
            }
        };

        thread.start();
    }

    final Runnable createUI = new Runnable() {

        public void run(){

            if(imagenList.size() > 0 ){
                if((imagenList.size()*delay) > duration){
                    duration = (imagenList.size()*delay) + delay;
                }
                mCustomPagerAdapterImagenes = new CustomPagerAdapter(VerImagenesYMensajesActivity.this, imagenList);
                pagerImagenes.setAdapter(mCustomPagerAdapterImagenes);
                handler.postDelayed(runnableImagenes, delay);
            }

            if(listMensajes.size() > 0 ){
                if((listMensajes.size()*delay) > duration){
                    duration = (listMensajes.size() * delay) + delay;
                }
                mCustomPagerAdapterMensajes = new CustomPagerServicesMensajesAdapter(VerImagenesYMensajesActivity.this, listMensajes);
                pagerMensajes.setAdapter(mCustomPagerAdapterMensajes);
                handler.postDelayed(runnableMensajes, delay);
            }

            if(listFloresVelas.size() > 0 ){
                if((listFloresVelas.size()*delay) > duration){
                    duration = (listFloresVelas.size() * delay ) + delay;
                }
                mCustomPagerAdapterFlores = new CustomPagerServicesAdapter(VerImagenesYMensajesActivity.this, listFloresVelas);
                pagerFlores.setAdapter(mCustomPagerAdapterFlores);
                handler.postDelayed(runnableFlores, delay);
            }

            showProgress(false);

            handler.postDelayed(runnableRedirect, duration);
        }
    };

    Runnable runnableImagenes = new Runnable() {
        public void run() {
            if (mCustomPagerAdapterImagenes.getCount() == pageImagenes) {
                pageImagenes = 0;
            } else {
                pageImagenes++;
            }
            pagerImagenes.setCurrentItem(pageImagenes, true);
            handler.postDelayed(this, delay);
        }
    };

    Runnable runnableMensajes = new Runnable() {
        public void run() {
            if (mCustomPagerAdapterMensajes.getCount() == pageMensajes) {
                pageMensajes = 0;
            } else {
                pageMensajes++;
            }
            pagerMensajes.setCurrentItem(pageMensajes, true);
            handler.postDelayed(this, delay);
        }
    };

    Runnable runnableFlores = new Runnable() {
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

    Runnable runnableRedirect = new Runnable() {
        public void run() {
            Intent i = new Intent(VerImagenesYMensajesActivity.this, VerPlacaActivity.class);
            finish();
            finishAffinity();
            startActivity(i);
        }
    };
}

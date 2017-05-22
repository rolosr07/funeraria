package com.funeraria.funeraria;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.funeraria.funeraria.common.Adapters.CustomPagerAdapter;
import com.funeraria.funeraria.common.Adapters.CustomPagerServicesAdapter;
import com.funeraria.funeraria.common.Adapters.CustomPagerServicesMensajesAdapter;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Imagen;
import com.funeraria.funeraria.common.entities.PlacaInformation;
import com.funeraria.funeraria.common.entities.Servicio;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class VerImagenesYMensajesActivity extends Base {

    private TextView tvNombre;

    private String webResponseValidarDescarga = "";

    private Thread thread;
    private Handler handler = new Handler();

    private int pageImagenes = 0;
    private int pageMensajes = 0;
    private int pageFlores = 0;
    private int delay = 10000; //milliseconds

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

    private int duration = 22000;

    //private MediaPlayer mPlayer;

    private List<Imagen> imagenList = new ArrayList<Imagen>();
    private List<Servicio> listMensajes = new ArrayList<Servicio>();
    private List<Servicio> listFloresVelas = new ArrayList<Servicio>();

    private ImageView imageViewImagenSuperior;
    private ImageView imageViewImagenOrla;
    private ImageView imageViewImagenOrlaFinal;

    private TextView tvNombre2;
    private TextView tvFechaNacimiento;
    private TextView tvFechaDeceso;
    private TextView tvEsquela;

    private String webResponseServices = "";
    private String nombreDifunto = "";
    private String  imagenOrla = "";

    public View mLoginFormView2;

    private boolean fr = true;

    private final String METHOD_NAME_GET_PLACA_INFORMATION = "getPlacaInformation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_imagenes_y_mensajes);

        mLoginFormView = findViewById(R.id.login_form);
        mLoginFormView2 = findViewById(R.id.login_form2);
        mProgressView = findViewById(R.id.login_progress);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mLoginFormView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Imagenes();
        placa();

        if(getCurrentImagenes() != null || getCurrentMensajes() != null || getCurrentFloresYVelas()!= null){
            cargarInformacionThread();
        }


        validarDescarga(getCurrentUser().getIdDifunto());
    }

    private void Imagenes(){
        tvNombre = (TextView)findViewById(R.id.tvNombre);

        pagerImagenes = (ViewPager) findViewById(R.id.pager);
        pagerMensajes = (ViewPager) findViewById(R.id.pagerMensajes);
        pagerFlores = (ViewPager) findViewById(R.id.pagerFlores);

        //mPlayer = MediaPlayer.create(VerImagenesYMensajesActivity.this, R.raw.music);
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
    }

    private void placa(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());

        Log.w("MEMORIAL APP", currentDateandTime);

        imageViewImagenSuperior = (ImageView)findViewById(R.id.imageViewImagenSuperior);
        imageViewImagenOrla = (ImageView) findViewById(R.id.imageViewImagenOrla);
        imageViewImagenOrlaFinal = (ImageView) findViewById(R.id.imageViewImagenFinal);

        tvNombre2 = (TextView) findViewById(R.id.tvNombre2);
        tvFechaNacimiento = (TextView) findViewById(R.id.tvFechaNacimiento);
        tvFechaDeceso = (TextView) findViewById(R.id.tvFechaDeceso);
        tvEsquela = (TextView) findViewById(R.id.tvEsquela);


        if (getCurrentPlaca() == null) {
            loadServicesList(getCurrentUser().getIdDifunto());
        }
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
        freeMemory();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        finishAffinity();
        finish();
        freeMemory();
        super.onDestroy();
    }

    public void cargarInformacionThread(){
        thread = new Thread(){
            public void run(){
                handler.post(createUI);
                handler.post(createUIServices);
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

            if(fr){
                fr = false;
            }else{

                if(mLoginFormView.getVisibility() == View.VISIBLE){
                    mLoginFormView.setVisibility(View.GONE);
                    mLoginFormView2.setVisibility(View.VISIBLE);
                }else{
                    mLoginFormView.setVisibility(View.VISIBLE);
                    mLoginFormView2.setVisibility(View.GONE);
                }
                fr = true;
            }
            handler.postDelayed(this, duration);
        }
    };

    public void loadServicesList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_PLACA_INFORMATION);

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
                    webResponseServices = response.toString();

                    if(!webResponseServices.equals("") && !webResponseServices.equals("[]")){

                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString(PLACA_INFORMATION, webResponseServices).apply();

                        handler.post(createUIServices);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    final Runnable createUIServices = new Runnable() {
        public void run(){
            showProgress(false);
            cargarInformacion(getCurrentPlaca());
        }
    };

    private String getMonth(int month) {
        return capitalizeFirstLetter(new DateFormatSymbols().getMonths()[month]);
    }

    private String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private void cargarInformacion(PlacaInformation placaInformation){
        byte[] decodedStringimagenSuperior = Base64.decode(placaInformation.getImagenSuperior(), Base64.DEFAULT);
        Bitmap imagenSuperior = BitmapFactory.decodeByteArray(decodedStringimagenSuperior, 0, decodedStringimagenSuperior.length);

        imageViewImagenSuperior.setImageBitmap(imagenSuperior);

        nombreDifunto = placaInformation.getNombre()+ " " + placaInformation.getApellidos();

        tvNombre2.setText(nombreDifunto);

        SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = format.parse(placaInformation.getFechaNacimiento());

            SimpleDateFormat year = new SimpleDateFormat("yyyy");
            SimpleDateFormat day = new SimpleDateFormat("dd");

            tvFechaNacimiento.setText(day.format(date) +" "+ getMonth(date.getMonth()) +" "+year.format(date) );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            Date date = format.parse(placaInformation.getFechaDefuncion());

            SimpleDateFormat year = new SimpleDateFormat("yyyy");
            SimpleDateFormat day = new SimpleDateFormat("dd");

            tvFechaDeceso.setText(day.format(date) +" "+ getMonth(date.getMonth()) +" "+ year.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imagenOrla = placaInformation.getImagenOrla();
        byte[] decodedStringImagenOrla = Base64.decode(placaInformation.getImagenOrla(), Base64.DEFAULT);
        Bitmap imagenOrla = BitmapFactory.decodeByteArray(decodedStringImagenOrla, 0, decodedStringImagenOrla.length);

        imageViewImagenOrla.setImageBitmap(imagenOrla);
        tvEsquela.setText(placaInformation.getEsquela());
        imageViewImagenOrlaFinal.setImageBitmap(imagenOrla);
        showProgress(false);
    }

}

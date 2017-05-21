package com.funeraria.funeraria;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.PlacaInformation;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VerPlacaActivity extends Base {

    private ImageView imageViewImagenSuperior;
    private ImageView imageViewImagenOrla;
    private ImageView imageViewImagenOrlaFinal;

    private TextView tvNombre;
    private TextView tvFechaNacimiento;
    private TextView tvFechaDeceso;
    private TextView tvEsquela;

    private String webResponseServices = "";
    private String nombreDifunto = "";
    private String  imagenOrla = "";

    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME_GET_PLACA_INFORMATION = "getPlacaInformation";
    private final String METHOD_NAME_VALIDAR_DESCARGA = "placaInformationNeedDownload";

    //private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_placa);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mLoginFormView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        imageViewImagenSuperior = (ImageView)findViewById(R.id.imageViewImagenSuperior);
        imageViewImagenOrla = (ImageView) findViewById(R.id.imageViewImagenOrla);
        imageViewImagenOrlaFinal = (ImageView) findViewById(R.id.imageViewImagenFinal);

        tvNombre = (TextView) findViewById(R.id.tvNombre);
        tvFechaNacimiento = (TextView) findViewById(R.id.tvFechaNacimiento);
        tvFechaDeceso = (TextView) findViewById(R.id.tvFechaDeceso);
        tvEsquela = (TextView) findViewById(R.id.tvEsquela);


        if (getCurrentPlaca() == null) {
            showProgress(true);
            loadServicesList(getCurrentUser().getIdDifunto());
        } else {
            cargarInformacionThread();
        }

        validarDescarga(getCurrentUser().getIdDifunto());

        //mPlayer = MediaPlayer.create(VerPlacaActivity.this, R.raw.music);
        //mPlayer.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(VerPlacaActivity.this, VerImagenesYMensajesActivity.class);
                i.putExtra("imagenOrla", imagenOrla);
                startActivity(i);
            }
        }, 12000);
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
                    webResponseServices = response.toString();

                    if(!webResponseServices.equals("") && Boolean.parseBoolean(webResponseServices)) {
                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString(PLACA_INFORMATION, "").apply();
                        setPlacaInformation(null);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

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
    @SuppressLint("SimpleDateFormat")

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
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(createUIServices);
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

    public void cargarInformacionThread(){
        thread = new Thread(){
            public void run(){
                handler.post(createUIServices);
            }
        };

        thread.start();
    }

    private void cargarInformacion(PlacaInformation placaInformation){
        byte[] decodedStringimagenSuperior = Base64.decode(placaInformation.getImagenSuperior(), Base64.DEFAULT);
        Bitmap imagenSuperior = BitmapFactory.decodeByteArray(decodedStringimagenSuperior, 0, decodedStringimagenSuperior.length);

        imageViewImagenSuperior.setImageBitmap(imagenSuperior);

        nombreDifunto = placaInformation.getNombre()+ " " + placaInformation.getApellidos();

        tvNombre.setText(nombreDifunto);

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

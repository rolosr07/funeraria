package com.funeraria.funeraria;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.PlacaInformation;
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
import java.util.Date;
import java.util.List;

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

    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME_GET_PLACA_INFORMATION = "getPlacaInformation";

    private int idDifunto = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_placa);

        if(getIntent().getExtras().containsKey("idDifunto")){
            idDifunto = getIntent().getExtras().getInt("idDifunto");
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        imageViewImagenSuperior = (ImageView)findViewById(R.id.imageViewImagenSuperior);
        imageViewImagenOrla = (ImageView) findViewById(R.id.imageViewImagenOrla);
        imageViewImagenOrlaFinal = (ImageView) findViewById(R.id.imageViewImagenFinal);

        tvNombre = (TextView) findViewById(R.id.tvNombre);
        tvFechaNacimiento = (TextView) findViewById(R.id.tvFechaNacimiento);
        tvFechaDeceso = (TextView) findViewById(R.id.tvFechaDeceso);
        tvEsquela = (TextView) findViewById(R.id.tvEsquela);

        showProgress(true);
        loadServicesList(idDifunto);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(VerPlacaActivity.this, VerImagenesDifuntoActivity.class);
                i.putExtra("idDifunto", idDifunto);
                i.putExtra("nombreDifunto", nombreDifunto);
                startActivity(i);
            }
        }, 25000);
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

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIServices);
            }
        };

        thread.start();
    }
    @SuppressLint("SimpleDateFormat")
    final Runnable createUIServices = new Runnable() {

        public void run(){

            showProgress(false);
            if(!webResponseServices.equals("") && !webResponseServices.equals("[]")){
                Type collectionType = new TypeToken<List<PlacaInformation>>(){}.getType();
                List<PlacaInformation> placaInformationList = new Gson().fromJson( webResponseServices , collectionType);
                if(placaInformationList.size() > 0 ){

                    PlacaInformation placaInformation = placaInformationList.get(0);

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

                    byte[] decodedStringImagenOrla = Base64.decode(placaInformation.getImagenOrla(), Base64.DEFAULT);
                    Bitmap imagenOrla = BitmapFactory.decodeByteArray(decodedStringImagenOrla, 0, decodedStringImagenOrla.length);

                    imageViewImagenOrla.setImageBitmap(imagenOrla);

                    tvEsquela.setText(placaInformation.getEsquela());

                    byte[] decodedStringImagenOrlaFinal = Base64.decode(placaInformation.getImagenOrla(), Base64.DEFAULT);
                    Bitmap imagenOrlaFinal = BitmapFactory.decodeByteArray(decodedStringImagenOrlaFinal, 0, decodedStringImagenOrlaFinal.length);

                    imageViewImagenOrlaFinal.setImageBitmap(imagenOrlaFinal);
                }
            }
        }
    };

    public String getMonth(int month) {
        return capitalizeFirstLetter(new DateFormatSymbols().getMonths()[month]);
    }

    public String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(VerPlacaActivity.this, MainActivityPresenter.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finishAffinity();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finishAffinity();
        finish();
    }
}

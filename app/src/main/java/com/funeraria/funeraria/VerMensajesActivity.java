package com.funeraria.funeraria;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.funeraria.funeraria.common.Adapters.CustomPagerServicesMensajesAdapter;
import com.funeraria.funeraria.common.Base;
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


public class VerMensajesActivity extends Base {

    private TextView tvNombre;

    private String webResponseImages = "";
    private Thread thread;
    private Handler handler = new Handler();

    private int idDifunto = 0;
    private String nombreDifunto = "";
    private String imagenOrla = "";

    private int page = 0;
    private int delay = 5000; //milliseconds
    private ViewPager pager;
    private CustomPagerServicesMensajesAdapter mCustomPagerAdapter;

    //private MediaPlayer mPlayer;

    private ImageView imageViewImagenOrla;
    private ImageView imageViewImagenOrlaFinal;

    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoYTipoDeServicioMensajesList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_mensajes);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvNombre = (TextView)findViewById(R.id.tvNombre);
        pager = (ViewPager) findViewById(R.id.pager);
        imageViewImagenOrla = (ImageView) findViewById(R.id.imageViewImagenOrla);
        imageViewImagenOrlaFinal = (ImageView) findViewById(R.id.imageViewImagenFinal);

        //mPlayer = MediaPlayer.create(VerMensajesActivity.this, R.raw.music);
        //mPlayer.start();

        if(getIntent().getExtras().containsKey("imagenOrla")){
            imagenOrla = getIntent().getExtras().getString("imagenOrla");
            byte[] decodedStringImagenOrla = Base64.decode(getIntent().getExtras().getString("imagenOrla"), Base64.DEFAULT);
            Bitmap imagenOrla = BitmapFactory.decodeByteArray(decodedStringImagenOrla, 0, decodedStringImagenOrla.length);

            imageViewImagenOrla.setImageBitmap(imagenOrla);
            imageViewImagenOrla.setVisibility(View.VISIBLE);
            imageViewImagenOrlaFinal.setImageBitmap(imagenOrla);
            imageViewImagenOrlaFinal.setVisibility(View.VISIBLE);
        }

        showProgress(true);

        if(getIntent().getExtras().containsKey("idDifunto")){
            idDifunto = getIntent().getExtras().getInt("idDifunto");
        }

        if(getIntent().getExtras().containsKey("nombreDifunto")){
            nombreDifunto = getIntent().getExtras().getString("nombreDifunto");
        }
        tvNombre.setText(nombreDifunto);

        loadMensajesList(idDifunto);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(VerMensajesActivity.this, VerPlacaActivity.class);
                i.putExtra("idDifunto", idDifunto);
                i.putExtra("nombreDifunto", nombreDifunto);
                i.putExtra("imagenOrla", imagenOrla);
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

    public void loadMensajesList(final int idDifunto){
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

                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> lcs = new Gson().fromJson( webResponseImages , collectionType);

                mCustomPagerAdapter = new CustomPagerServicesMensajesAdapter(VerMensajesActivity.this, lcs);
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
        handler.removeCallbacks(runnable);
        finishAffinity();
        finish();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //mPlayer.stop();
        finishAffinity();
        finish();
        super.onDestroy();
    }
}

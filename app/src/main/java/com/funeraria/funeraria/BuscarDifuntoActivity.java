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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Adapters.CustomAdapterImagenes;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
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


public class BuscarDifuntoActivity extends Base {

    private ImageView imageView;
    private EditText edTextoBusqueda;
    private TextView tvFotos;
    private String webResponse = "";
    private String webResponseImages = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;
    private Spinner spinnerImages;
    private Button buttonBuscar;
    private String textoBusqueda;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "buscarDifuntosPorNombreOApellido";
    private final String METHOD_NAME_GET_IMAGENES_LIST = "getImagenesDifuntoList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        edTextoBusqueda = (EditText)findViewById(R.id.edTextoBusqueda);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerImages = (Spinner) findViewById(R.id.spinnerImagenes);
        imageView = (ImageView)findViewById(R.id.imageView);
        tvFotos = (TextView)findViewById(R.id.tvFotos);

        if(getIntent().getExtras().containsKey("textoBusqueda")){
            textoBusqueda = getIntent().getExtras().getString("textoBusqueda");
            edTextoBusqueda.setText(textoBusqueda);
        }

        buttonBuscar = (Button) findViewById(R.id.buttonBuscar);
        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(BuscarDifuntoActivity.this, BuscarDifuntoActivity.class);
                i.putExtra("textoBusqueda",edTextoBusqueda.getText().toString());
                finish();
                startActivity(i);
            }
        });

        showProgress(true);
        loadDifuntosList(textoBusqueda);
    }

    public void loadDifuntosList(final String textoBusqueda){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_DIFUNTO_LIST);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("textoBusqueda");
                    fromProp.setValue(textoBusqueda);
                    fromProp.setType(String.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponse = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUI);
            }
        };

        thread.start();
    }

    final Runnable createUI = new Runnable() {

        public void run(){

            if(webResponse != null && !webResponse.equals("")){
                Type collectionType = new TypeToken<List<Difunto>>(){}.getType();
                List<Difunto> lcs = new Gson().fromJson( webResponse , collectionType);

                CustomAdapter adapter = new CustomAdapter(BuscarDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Difunto dif = (Difunto) parent.getItemAtPosition(position);
                                showProgress(true);
                                loadImagenesList(dif.getIdDifunto());
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        }
                );
                showProgress(false);
            }
            else{
                showProgress(false);
            }
        }
    };

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

        @SuppressLint("SetTextI18n")
        public void run(){

            if(!webResponseImages.equals("") && !webResponseImages.equals("[]")){
                Type collectionType = new TypeToken<List<Imagen>>(){}.getType();
                List<Imagen> lcs = new Gson().fromJson( webResponseImages , collectionType);

                if(lcs.size() > 0){

                    CustomAdapterImagenes adapter = new CustomAdapterImagenes(BuscarDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerImages.setAdapter(adapter);
                    spinnerImages.setVisibility(View.VISIBLE);
                    showProgress(false);
                    spinnerImages.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Imagen img = (Imagen)parent.getItemAtPosition(position);

                                    byte[] decodedString = Base64.decode(img.getImagen(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    imageView.setImageBitmap(decodedByte);
                                    imageView.setVisibility(View.VISIBLE);

                                    showProgress(false);

                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                }else{
                    spinnerImages.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    tvFotos.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                imageView.setVisibility(View.GONE);
                tvFotos.setVisibility(View.GONE);
                spinnerImages.setVisibility(View.GONE);
            }
        }
    };

}

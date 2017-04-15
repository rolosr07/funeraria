package com.funeraria.funeraria;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Adapters.CustomAdapterImagenes;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
import com.funeraria.funeraria.common.entities.Imagen;
import com.funeraria.funeraria.common.entities.Usuario;
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
    private String webResponse = "";
    private String webResponseImages = "";
    private String webResponseSolicitarAcceso = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;
    private Spinner spinnerImages;
    private String textoBusqueda;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "buscarDifuntosPorNombreOApellido";
    private final String METHOD_NAME_GET_IMAGENES_LIST = "getImagenesDifuntoList";
    private final String METHOD_NAME_GET_SOLICITAR_ACCESO = "solicitarAccesoDifunto";

    private Usuario usuarioActual;

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

        if(getIntent().getExtras().containsKey("textoBusqueda")){
            textoBusqueda = getIntent().getExtras().getString("textoBusqueda");
            edTextoBusqueda.setText(textoBusqueda);
        }

        Button buttonBuscar = (Button) findViewById(R.id.buttonBuscar);
        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(BuscarDifuntoActivity.this, BuscarDifuntoActivity.class);
                i.putExtra("textoBusqueda",edTextoBusqueda.getText().toString());
                finish();
                startActivity(i);
            }
        });

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
            List<Usuario> usuarios = new Gson().fromJson( prefs.getString("USER_DATA","") , collectionType);
            usuarioActual = usuarios.get(0);
        }

        Button buttonSolicitarAcceso = (Button) findViewById(R.id.buttonSolicitarAcceso);
        buttonSolicitarAcceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                Difunto difunto = (Difunto)spinner.getSelectedItem();
                solicitarAcceso(usuarioActual.getIdUsuario(),difunto.getIdDifunto());
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
                    showProgress(false);
                }
            }else{
                showProgress(false);
                imageView.setVisibility(View.GONE);
                spinnerImages.setVisibility(View.GONE);
            }
        }
    };

    public void solicitarAcceso(final int idUsuario, final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_SOLICITAR_ACCESO);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("idUsuario");
                    fromProp1.setValue(idUsuario);
                    fromProp1.setType(int.class);
                    request.addProperty(fromProp1);

                    PropertyInfo fromProp2 = new PropertyInfo();
                    fromProp2.setType(int.class);
                    fromProp2.setName("idDifunto");
                    fromProp2.setValue(idDifunto);
                    request.addProperty(fromProp2);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponseSolicitarAcceso = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUICompra);
            }
        };

        thread.start();
    }

    final Runnable createUICompra = new Runnable() {

        public void run(){

            if(!webResponseSolicitarAcceso.equals("") && !webResponseSolicitarAcceso.equals("[]") && Boolean.parseBoolean(webResponseSolicitarAcceso)){
                Toast.makeText(BuscarDifuntoActivity.this, "Solicitud realizada exitosamente!", Toast.LENGTH_LONG).show();
                showProgress(false);
                Intent i = new Intent(BuscarDifuntoActivity.this, MainActivityUser.class);
                finish();
                startActivity(i);
            } else if(!webResponseSolicitarAcceso.equals("") && !webResponseSolicitarAcceso.equals("[]") && webResponseSolicitarAcceso.equals("AccesoDado")){
                Toast.makeText(BuscarDifuntoActivity.this, "Solicitud ya ha sido realizada!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
            else{
                Toast.makeText(BuscarDifuntoActivity.this, "No se ha podido realizar la solicitud,!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }
    };

}

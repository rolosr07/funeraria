package com.funeraria.funeraria;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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

import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Adapters.CustomAdapterRestos;
import com.funeraria.funeraria.common.Adapters.CustomAdapterServicio;
import com.funeraria.funeraria.common.entities.Difunto;
import com.funeraria.funeraria.common.entities.Restos;
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


public class PlacaActivity extends Base {

    private View mProgressView;
    private View mLoginFormView;

    private ImageView imageViewImagenSuperior;
    private ImageView imageViewImagenOrla;
    private EditText esquelaPersonal;

    private String webResponse = "";
    private String webResponseServices = "";
    private String webResponseRestos = "";
    private String webResponseRegistro = "";
    private Thread thread;
    private Handler handler = new Handler();

    private Spinner spinnerDifuntos;
    private Spinner spinnerImagenSuperior;
    private Spinner spinnerImagenOrla;
    private Spinner spinnerEsquela;
    private Spinner spinnerRestos;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosList";
    private final String METHOD_NAME_GET_SERVICES_LIST = "getServiciosList";
    private final String METHOD_NAME_GET_RESTOS_LIST = "getRestosList";
    private final String METHOD_NAME_REGISTRAR_PLACA = "registrarPlaca";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placa);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        imageViewImagenSuperior = (ImageView)findViewById(R.id.imageViewImagenSuperior);
        imageViewImagenOrla = (ImageView) findViewById(R.id.imageViewImagenOrla);
        esquelaPersonal = (EditText) findViewById(R.id.esquelaPersonal);

        spinnerDifuntos = (Spinner) findViewById(R.id.spinnerDifuntos);
        spinnerImagenSuperior = (Spinner) findViewById(R.id.spinnerImagenSuperior);
        spinnerImagenOrla = (Spinner) findViewById(R.id.spinnerImagenOrla);
        spinnerEsquela = (Spinner) findViewById(R.id.spinnerEsquela);
        spinnerRestos = (Spinner) findViewById(R.id.spinnerRestos);
        showProgress(true);
        loadDifuntosList();
        showProgress(true);
        loadServicesList();
        showProgress(true);
        loadRestosList();

        Button buttonRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Difunto dif = (Difunto) spinnerDifuntos.getSelectedItem();
                Servicio imagenSuperior = (Servicio)spinnerImagenSuperior.getSelectedItem();
                Servicio imagenOrla = (Servicio)spinnerImagenOrla.getSelectedItem();
                Servicio esquela = (Servicio)spinnerEsquela.getSelectedItem();
                Restos restos = (Restos)spinnerRestos.getSelectedItem();
                String esquelaPersonalText = "";
                if(!esquela.getTexto().equals(esquelaPersonal.getText().toString())){
                    esquelaPersonalText = esquelaPersonal.getText().toString();
                }
                showProgress(true);
                registarInscripcion(dif.getIdDifunto(),imagenSuperior.getIdServicio(),imagenOrla.getIdServicio(),esquela.getIdServicio(),restos.getIdLugarRestos(),esquelaPersonalText);
            }
        });
    }

    public void loadDifuntosList(){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_DIFUNTO_LIST);

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

            if(!webResponse.equals("")){
                Type collectionType = new TypeToken<List<Difunto>>(){}.getType();
                List<Difunto> lcs = new Gson().fromJson( webResponse , collectionType);

                CustomAdapter adapter = new CustomAdapter(PlacaActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinnerDifuntos.setAdapter(adapter);
                showProgress(false);
            }
            else{
                showProgress(false);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinnerDifuntos.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadServicesList(){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_SERVICES_LIST);

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

    final Runnable createUIServices = new Runnable() {

        public void run(){

            showProgress(false);
            if(!webResponseServices.equals("") && !webResponseServices.equals("[]")){
                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> servicioList = new Gson().fromJson( webResponseServices , collectionType);

                List<Servicio> servicioImagenSuperiorList = new ArrayList<Servicio>();
                List<Servicio> servicioImagenOrlaList = new ArrayList<Servicio>();
                List<Servicio> servicioImagenEsquelaList = new ArrayList<Servicio>();

                for(Servicio serv : servicioList){
                    if(serv.getIdTipoServicio() == 1){
                        servicioImagenSuperiorList.add(serv);
                    }
                    if(serv.getIdTipoServicio() == 2){
                        servicioImagenOrlaList.add(serv);
                    }
                    if(serv.getIdTipoServicio() == 9){
                        servicioImagenEsquelaList.add(serv);
                    }
                }

                if(servicioImagenSuperiorList.size() > 0){

                    CustomAdapterServicio adapter = new CustomAdapterServicio(PlacaActivity.this, R.layout.simple_spinner_item,servicioImagenSuperiorList);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerImagenSuperior.setAdapter(adapter);

                    spinnerImagenSuperior.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    byte[] decodedString = Base64.decode(servicio.getImagen(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    imageViewImagenSuperior.setImageBitmap(decodedByte);
                                    imageViewImagenSuperior.setVisibility(View.VISIBLE);

                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                }
                if(servicioImagenOrlaList.size() > 0){

                    CustomAdapterServicio adapter = new CustomAdapterServicio(PlacaActivity.this, R.layout.simple_spinner_item,servicioImagenOrlaList);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerImagenOrla.setAdapter(adapter);

                    spinnerImagenOrla.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    byte[] decodedString = Base64.decode(servicio.getImagen(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    imageViewImagenOrla.setImageBitmap(decodedByte);
                                    imageViewImagenOrla.setVisibility(View.VISIBLE);

                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                }
                if(servicioImagenEsquelaList.size() > 0){

                    CustomAdapterServicio adapter = new CustomAdapterServicio(PlacaActivity.this, R.layout.simple_spinner_item,servicioImagenEsquelaList);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerEsquela.setAdapter(adapter);

                    spinnerEsquela.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    esquelaPersonal.setText(servicio.getTexto());

                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                }
            }
        }
    };

    public void loadRestosList(){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_RESTOS_LIST);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SERVICIO);

                    androidHttpTransport.call(SOAP_ACTION_SERVICIO, envelope);
                    Object response = envelope.getResponse();
                    webResponseRestos = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIRestos);
            }
        };

        thread.start();
    }

    final Runnable createUIRestos = new Runnable() {

        public void run(){

            showProgress(false);
            if(!webResponseRestos.equals("") && !webResponseRestos.equals("[]")){
                Type collectionType = new TypeToken<List<Restos>>(){}.getType();
                List<Restos> restosList = new Gson().fromJson( webResponseRestos , collectionType);

                if(restosList.size() > 0){

                    CustomAdapterRestos adapter = new CustomAdapterRestos(PlacaActivity.this, R.layout.simple_spinner_item,restosList);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerRestos.setAdapter(adapter);
                }
            }
        }
    };

    public void registarInscripcion(final int idDifunto, final int idImagenSuperior, final int idOrla, final int idEsquela, final int idRestos, final String esquelaPersonal){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_REGISTRAR_PLACA);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("idImagenSuperior");
                    fromProp1.setValue(idImagenSuperior);
                    fromProp1.setType(int.class);
                    request.addProperty(fromProp1);

                    PropertyInfo fromProp2 = new PropertyInfo();
                    fromProp2.setName("idOrla");
                    fromProp2.setValue(idOrla);
                    fromProp2.setType(int.class);
                    request.addProperty(fromProp2);

                    PropertyInfo fromProp3 = new PropertyInfo();
                    fromProp3.setName("idEsquela");
                    fromProp3.setValue(idEsquela);
                    fromProp3.setType(int.class);
                    request.addProperty(fromProp3);

                    PropertyInfo fromProp4 = new PropertyInfo();
                    fromProp4.setName("idRestos");
                    fromProp4.setValue(idRestos);
                    fromProp4.setType(int.class);
                    request.addProperty(fromProp4);

                    PropertyInfo fromProp5 = new PropertyInfo();
                    fromProp5.setName("esquelaPersonal");
                    fromProp5.setValue(esquelaPersonal);
                    fromProp5.setType(String.class);
                    request.addProperty(fromProp5);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SERVICIO);

                    androidHttpTransport.call(SOAP_ACTION_SERVICIO, envelope);
                    Object response = envelope.getResponse();
                    webResponseRegistro = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIRegistro);
            }
        };

        thread.start();
    }

    final Runnable createUIRegistro = new Runnable() {

        public void run(){

            showProgress(false);
            if(!webResponseRegistro.equals("") && Boolean.valueOf(webResponseRegistro)){
                Toast.makeText(PlacaActivity.this, "Inscripción realizada con exito!", Toast.LENGTH_LONG).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent i = new Intent(PlacaActivity.this, MainActivityAdmin.class);
                        finish();
                        startActivity(i);
                    }
                }, 2000);
            }else{
                Toast.makeText(PlacaActivity.this, "Error al realizar Inscripción!", Toast.LENGTH_LONG).show();
            }
        }
    };
}

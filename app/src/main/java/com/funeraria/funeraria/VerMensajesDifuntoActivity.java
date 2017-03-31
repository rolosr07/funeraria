package com.funeraria.funeraria;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomAdapterServicio;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.entities.Difunto;
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


public class VerMensajesDifuntoActivity extends Base {

    private View mProgressView;
    private View mLoginFormView;
    private TextView txNumeroMensajes;
    private TextView txMensaje;

    private String webResponse = "";
    private String webResponseImages = "";
    private String webResponseActualizar = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;
    private Spinner spinnerMensajes;

    private TextView txNombreUsuario;
    private TextView txFechaCompra;
    private TextView txAutorizado;
    private Button buttonAutorizar;
    private int idServicioComprado;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoYTipoDeServicioMensajesList";
    private final String METHOD_NAME_REGISTAR_SERVICIO_COMPRADO = "actualizarServicioComprado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_mensajes_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroMensajes = (TextView)findViewById(R.id.txNumeroMensajes);
        txMensaje = (TextView)findViewById(R.id.txMensaje);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerMensajes = (Spinner) findViewById(R.id.spinnerMensajes);

        txNombreUsuario = (TextView)findViewById(R.id.txNombreUsuario);
        txFechaCompra = (TextView)findViewById(R.id.txFechaCompra);
        txAutorizado = (TextView)findViewById(R.id.txAutorizado);

        buttonAutorizar = (Button) findViewById(R.id.buttonAutorizar);
        buttonAutorizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                registrarServicioComprado(idServicioComprado);
            }
        });

        showProgress(true);
        loadDifuntosList();
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

                CustomAdapter adapter = new CustomAdapter(VerMensajesDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Difunto dif = (Difunto) parent.getItemAtPosition(position);
                                showProgress(true);
                                loadMensajesList(dif.getIdDifunto());
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
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

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("idTipoServicio");
                    fromProp1.setValue(8);
                    fromProp1.setType(int.class);
                    request.addProperty(fromProp1);

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

            if(webResponseImages!= null && !webResponseImages.equals("") && !webResponseImages.equals("[]")){
                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> lcs = new Gson().fromJson( webResponseImages , collectionType);

                if(lcs.size() > 0){

                    txNumeroMensajes.setText("Cantidad de Imagenes: "+ lcs.size());
                    txNumeroMensajes.setVisibility(View.VISIBLE);

                    CustomAdapterServicio adapter = new CustomAdapterServicio(VerMensajesDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerMensajes.setAdapter(adapter);
                    spinnerMensajes.setVisibility(View.VISIBLE);
                    showProgress(false);
                    spinnerMensajes.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    txMensaje.setText(servicio.getTexto());
                                    txMensaje.setVisibility(View.VISIBLE);

                                    txNombreUsuario.setText("Comprador: "+servicio.getNombreUsuario() + " " + servicio.getApellidoUsuario());
                                    txNombreUsuario.setVisibility(View.VISIBLE);

                                    txFechaCompra.setText("Fecha: "+servicio.getFechaCompra()+"");
                                    txFechaCompra.setVisibility(View.VISIBLE);

                                    if(servicio.getAutorizado().equals("1")){
                                        txAutorizado.setText("Autorizado: Si");
                                        txAutorizado.setVisibility(View.VISIBLE);
                                        buttonAutorizar.setEnabled(false);
                                    }else{
                                        txAutorizado.setText("Autorizado: No");
                                        txAutorizado.setVisibility(View.VISIBLE);
                                        buttonAutorizar.setEnabled(true);
                                    }

                                    idServicioComprado = servicio.getIdServicioComprado();

                                    showProgress(false);

                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                }else{
                    txNumeroMensajes.setText("Cantidad de Imagenes: "+ 0);
                    spinnerMensajes.setVisibility(View.GONE);
                    txMensaje.setVisibility(View.GONE);
                    txNombreUsuario.setVisibility(View.GONE);
                    txFechaCompra.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                txMensaje.setVisibility(View.GONE);
                txNumeroMensajes.setText("Cantidad de Imagenes: "+0);
                spinnerMensajes.setVisibility(View.GONE);
                txNombreUsuario.setVisibility(View.GONE);
                txFechaCompra.setVisibility(View.GONE);
            }
        }
    };

    public void registrarServicioComprado(final int idServicioComprado){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_REGISTAR_SERVICIO_COMPRADO);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idServicioComprado");
                    fromProp.setValue(idServicioComprado);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SERVICIO);

                    androidHttpTransport.call(SOAP_ACTION_SERVICIO, envelope);
                    Object response = envelope.getResponse();
                    webResponseActualizar = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIRegistroServicioComprado);
            }
        };

        thread.start();
    }

    final Runnable createUIRegistroServicioComprado = new Runnable() {

        public void run(){

            if(!webResponseActualizar.equals("") && !webResponseActualizar.equals("[]") && Boolean.parseBoolean(webResponseActualizar)){
                Toast.makeText(VerMensajesDifuntoActivity.this, "Mensaje Autorizado!", Toast.LENGTH_LONG).show();
                showProgress(false);
                loadDifuntosList();
            }
            else{
                Toast.makeText(VerMensajesDifuntoActivity.this, "No se ha podido autorizar el mensaje!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }
    };

}

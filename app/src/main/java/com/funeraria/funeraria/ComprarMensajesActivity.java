package com.funeraria.funeraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Adapters.CustomAdapterServicio;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
import com.funeraria.funeraria.common.entities.Servicio;
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


public class ComprarMensajesActivity extends Base {

    private TextView txNumeroVelas;
    private TextView txDuracion;
    private TextView  txPrecio;
    private TextView txMensaje;
    private EditText edMensajePersonal;

    private String webResponse = "";
    private String webResponseImages = "";
    private String webResponseComprar = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;
    private Spinner spinnerMensajes;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosPorUsuarioList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorTipoDeServicioList";
    private final String METHOD_NAME_COMPRAR_SERVICIO = "comprarMensaje";

    private Usuario usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_mensajes);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroVelas = (TextView)findViewById(R.id.txNumeroVelas);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerMensajes = (Spinner) findViewById(R.id.spinnerMensajes);

        txMensaje = (TextView)findViewById(R.id.txMensaje);

        txDuracion = (TextView)findViewById(R.id.txDuracion);
        txPrecio = (TextView)findViewById(R.id.txPrecio);

        edMensajePersonal = (EditText) findViewById(R.id.edMensajePersonal);

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
            List<Usuario> usuarios = new Gson().fromJson( prefs.getString("USER_DATA","") , collectionType);
            usuarioActual = usuarios.get(0);
        }

        showProgress(true);
        loadDifuntosList();
        showProgress(true);
        loadEsquelasList();

        Button buttonComprar = (Button) findViewById(R.id.buttonComprar);
        buttonComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                int u = usuarioActual.getIdUsuario();
                Difunto d = (Difunto) spinner.getSelectedItem();
                Servicio s = (Servicio) spinnerMensajes.getSelectedItem();

                String mensajePersonal = "";
                if(!edMensajePersonal.getText().toString().equals("")){
                    mensajePersonal = edMensajePersonal.getText().toString();
                }

                comprarServicio(u,d.getIdDifunto(),s.getIdServicio(),mensajePersonal);
            }
        });
    }

    public void loadDifuntosList(){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_DIFUNTO_LIST);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idUsuario");
                    fromProp.setValue(usuarioActual.getIdUsuario());
                    fromProp.setType(int.class);
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

                CustomAdapter adapter = new CustomAdapter(ComprarMensajesActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                showProgress(true);
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

    public void loadEsquelasList(){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_SERVICIOS_LIST);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("idTipoServicio");
                    fromProp1.setValue(9);
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

            if(!webResponseImages.equals("") && !webResponseImages.equals("[]")){
                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> lcs = new Gson().fromJson( webResponseImages , collectionType);

                if(lcs.size() > 0){

                    txNumeroVelas.setText("Esquelas disponibles: "+ lcs.size());
                    txNumeroVelas.setVisibility(View.VISIBLE);

                    CustomAdapterServicio adapter = new CustomAdapterServicio(ComprarMensajesActivity.this, R.layout.simple_spinner_item,lcs);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerMensajes.setAdapter(adapter);
                    spinnerMensajes.setVisibility(View.VISIBLE);
                    spinnerMensajes.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    showProgress(true);
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    txDuracion.setText("Duración en pantalla: "+servicio.getTiempoMostrar()+" minutos");
                                    txDuracion.setVisibility(View.VISIBLE);

                                    txPrecio.setText("Precio: $"+servicio.getPrecio());
                                    txPrecio.setVisibility(View.VISIBLE);

                                    txMensaje.setText(servicio.getTexto());
                                    txMensaje.setVisibility(View.VISIBLE);
                                    showProgress(false);
                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                    showProgress(false);
                }else{
                    txNumeroVelas.setText("Cantidad de Esquelas: "+ 0);
                    spinnerMensajes.setVisibility(View.GONE);
                    txDuracion.setVisibility(View.GONE);
                    txPrecio.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                txNumeroVelas.setText("Cantidad de Esquelas: "+0);
                spinnerMensajes.setVisibility(View.GONE);
                txDuracion.setVisibility(View.GONE);
                txPrecio.setVisibility(View.GONE);
            }
        }
    };

    public void comprarServicio(final int idUsuario, final int idDifunto, final int idServicio, final String mensajePersonal){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_COMPRAR_SERVICIO);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idServicio");
                    fromProp.setValue(idServicio);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

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

                    PropertyInfo fromProp3 = new PropertyInfo();
                    fromProp3.setName("mensajePersonal");
                    fromProp3.setValue(mensajePersonal);
                    fromProp3.setType(String.class);
                    request.addProperty(fromProp3);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SERVICIO);

                    androidHttpTransport.call(SOAP_ACTION_SERVICIO, envelope);
                    Object response = envelope.getResponse();
                    webResponseComprar = response.toString();

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

            if(!webResponseComprar.equals("") && !webResponseComprar.equals("[]") && Boolean.parseBoolean(webResponseComprar)){
                Toast.makeText(ComprarMensajesActivity.this, "Compra realizada!, ahora se podrá ver la vela en la placa de su familiar!", Toast.LENGTH_LONG).show();
                showProgress(false);
                Intent i = new Intent(ComprarMensajesActivity.this, CompraExitoActivity.class);
                finish();
                startActivity(i);
            }
            else{
                Toast.makeText(ComprarMensajesActivity.this, "No se ha podido realizar la compra!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }
    };
}

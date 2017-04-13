package com.funeraria.funeraria;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Adapters.CustomAdapterUsuarios;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
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


public class AutorizarUsuariosDifuntoActivity extends Base {

    private TextView txNumeroUsuarios;
    private TextView txFechaCreacion;
    private TextView txAutorizado;
    private TextView txTipoUsuario;
    private TextView txEmail;

    private String webResponse = "";
    private String webResponseUsuarios = "";
    private String webResponseAutorizar = "";
    private String webResponseBorrar = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinnerDifuntos;
    private Spinner spinnerUsuarios;

    private Button buttonAutorizar;

    private int idAutorizarUsuario;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosList";
    private final String METHOD_NAME_GET_USUARIOS_LIST = "getUsuariosAutorizadosDifuntoList";
    private final String METHOD_NAME_AUTORIZAR_USUARIO = "autorizarUsuario";
    private final String METHOD_NAME_BORRAR_USUARIO = "borrarUsuarioDifunto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorizar_usuarios_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        txNumeroUsuarios = (TextView)findViewById(R.id.txNumeroUsuarios);
        txFechaCreacion = (TextView)findViewById(R.id.txFechaCreacion);
        txAutorizado = (TextView)findViewById(R.id.txAutorizado);
        txTipoUsuario = (TextView)findViewById(R.id.txTipoUsuario);
        txEmail = (TextView)findViewById(R.id.txEmail);

        spinnerDifuntos = (Spinner) findViewById(R.id.spinner);
        spinnerUsuarios = (Spinner) findViewById(R.id.spinnerImagenes);

        buttonAutorizar = (Button) findViewById(R.id.buttonAutorizar);
        buttonAutorizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                autorizarUsuario(idAutorizarUsuario);
            }
        });

        Button buttonRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Difunto dif = (Difunto)spinnerDifuntos.getSelectedItem();
                Intent i = new Intent(AutorizarUsuariosDifuntoActivity.this, RegistrarUsuarioActivity.class);
                i.putExtra("idDifunto", dif.getIdDifunto());
                startActivity(i);
                showProgress(false);
            }
        });

        Button buttonBorrarUsuario = (Button) findViewById(R.id.buttonBorrarUsuario);
        buttonBorrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Difunto dif = (Difunto)spinnerDifuntos.getSelectedItem();
                Usuario usuario = (Usuario)spinnerUsuarios.getSelectedItem();
                showProgress(true);
                borrarUsuario(usuario.getIdUsuarioAutorizado());
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

                CustomAdapter adapter = new CustomAdapter(AutorizarUsuariosDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinnerDifuntos.setAdapter(adapter);

                spinnerDifuntos.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Difunto dif = (Difunto) parent.getItemAtPosition(position);
                                showProgress(true);
                                loadUsuariosList(dif.getIdDifunto());
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

    public void loadUsuariosList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME_GET_USUARIOS_LIST);
                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_USER);

                    androidHttpTransport.call(SOAP_ACTION_USER, envelope);
                    Object response = envelope.getResponse();
                    webResponseUsuarios = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIUsuarios);
            }
        };

        thread.start();
    }

    final Runnable createUIUsuarios = new Runnable() {

        @SuppressLint("SetTextI18n")
        public void run(){

            if(!webResponseUsuarios.equals("") && !webResponseUsuarios.equals("[]")){
                Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
                List<Usuario> lcs = new Gson().fromJson(webResponseUsuarios, collectionType);

                if(lcs.size() > 0){

                    txNumeroUsuarios.setText("Cantidad de Usuarios: "+ lcs.size());
                    txNumeroUsuarios.setVisibility(View.VISIBLE);

                    CustomAdapterUsuarios adapter = new CustomAdapterUsuarios(AutorizarUsuariosDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerUsuarios.setAdapter(adapter);
                    spinnerUsuarios.setVisibility(View.VISIBLE);
                    showProgress(false);
                    spinnerUsuarios.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                Usuario usuario = (Usuario)parent.getItemAtPosition(position);

                                txFechaCreacion.setText("Fecha Creación: "+usuario.getFechaCreacion()+"");
                                txFechaCreacion.setVisibility(View.VISIBLE);

                                if(usuario.getAutorizado().equals("1")){
                                    txAutorizado.setText("Autorizado: Si");
                                    txAutorizado.setVisibility(View.VISIBLE);
                                    buttonAutorizar.setVisibility(View.GONE);
                                }else{
                                    txAutorizado.setText("Autorizado: No");
                                    txAutorizado.setVisibility(View.VISIBLE);
                                    buttonAutorizar.setVisibility(View.VISIBLE);
                                }

                                if(usuario.getRol().equals("user")){
                                    txTipoUsuario.setText("Tipo: Cliente");
                                    txTipoUsuario.setVisibility(View.VISIBLE);
                                }else{
                                    txTipoUsuario.setText("Tipo: Presentador");
                                    txTipoUsuario.setVisibility(View.VISIBLE);
                                }

                                txEmail.setText("Email: "+usuario.getEmail());
                                txEmail.setVisibility(View.VISIBLE);

                                idAutorizarUsuario = usuario.getIdUsuarioAutorizado();
                                showProgress(false);
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        }
                    );
                }else{
                    txNumeroUsuarios.setText("Cantidad de Usuarios: "+ 0);
                    spinnerUsuarios.setVisibility(View.GONE);
                    txFechaCreacion.setVisibility(View.GONE);
                    txAutorizado.setVisibility(View.GONE);
                    txTipoUsuario.setVisibility(View.GONE);
                    txEmail.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                txNumeroUsuarios.setText("Cantidad de Usuarios: "+0);
                spinnerUsuarios.setVisibility(View.GONE);
                txFechaCreacion.setVisibility(View.GONE);
                txAutorizado.setVisibility(View.GONE);
                txTipoUsuario.setVisibility(View.GONE);
                txEmail.setVisibility(View.GONE);
            }
        }
    };

    public void autorizarUsuario(final int idAutorizarUsuario){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME_AUTORIZAR_USUARIO);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idAutorizarUsuario");
                    fromProp.setValue(idAutorizarUsuario);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_USER);

                    androidHttpTransport.call(SOAP_ACTION_USER, envelope);
                    Object response = envelope.getResponse();
                    webResponseAutorizar = response.toString();

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

            if(!webResponseAutorizar.equals("") && !webResponseAutorizar.equals("[]") && Boolean.parseBoolean(webResponseAutorizar)){
                Toast.makeText(AutorizarUsuariosDifuntoActivity.this, "Usuario Autorizado!", Toast.LENGTH_LONG).show();
                showProgress(false);
                loadDifuntosList();
            }
            else{
                Toast.makeText(AutorizarUsuariosDifuntoActivity.this, "No se ha podido autorizar el Usuario!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AutorizarUsuariosDifuntoActivity.this, MainActivityAdmin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finishAffinity();
        finish();
        startActivity(intent);
    }

    public void borrarUsuario(final int idUsuarioAutorizado){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_BORRAR_USUARIO);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idUsuarioAutorizado");
                    fromProp.setValue(idUsuarioAutorizado);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponseBorrar = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIBorrar);
            }
        };

        thread.start();
    }

    final Runnable createUIBorrar = new Runnable() {

        public void run(){

            if(Boolean.valueOf(webResponseBorrar)){
                Difunto dif = (Difunto)spinnerDifuntos.getSelectedItem();
                loadUsuariosList(dif.getIdDifunto());

                Toast.makeText(AutorizarUsuariosDifuntoActivity.this, "Usuario Borrado!", Toast.LENGTH_LONG).show();
            }else{
                showProgress(false);
                Toast.makeText(AutorizarUsuariosDifuntoActivity.this, "No se pudo borrar el Usuario!", Toast.LENGTH_LONG).show();
            }
        }
    };
}

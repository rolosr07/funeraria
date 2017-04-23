package com.funeraria.funeraria;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.LazyAdapterMensajes;
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


public class VerMensajesDifuntoActivity extends Base {

    private TextView txNumeroMensajes;

    private String webResponseImages = "";
    private String webResponseActualizar = "";
    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoYTipoDeServicioMensajesList";
    private final String METHOD_NAME_REGISTAR_SERVICIO_COMPRADO = "actualizarServicioComprado";

    private TextView nombre;

    private ListView list;
    private LazyAdapterMensajes adapterList;

    private List<Servicio> lcs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_mensajes_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroMensajes = (TextView)findViewById(R.id.txNumeroMensajes);

        nombre = (TextView) findViewById(R.id.nombre);
        nombre.setText(getCurrentUser().getNombreDifunto());

        list = (ListView)findViewById(R.id.list);

        showProgress(true);
        loadMensajesList(getCurrentUser().getIdDifunto());
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

            if(webResponseImages!= null && !webResponseImages.equals("") && !webResponseImages.equals("[]")){
                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                lcs = new Gson().fromJson( webResponseImages , collectionType);

                if(lcs.size() > 0){

                    txNumeroMensajes.setText("Cantidad de Mensajes: "+ lcs.size()+"");
                    txNumeroMensajes.setVisibility(View.VISIBLE);

                    adapterList = new LazyAdapterMensajes(VerMensajesDifuntoActivity.this, lcs);
                    list.setAdapter(adapterList);

                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView parent, View view, int position, long id) {
                            showProgress(true);
                            if(lcs.get(position).getAutorizado().equals("1")){
                                registrarServicioComprado(lcs.get(position).getIdServicioComprado(),0);
                            }else{
                                registrarServicioComprado(lcs.get(position).getIdServicioComprado(),1);
                            }
                        }
                    });

                    showProgress(false);
                }else{
                    txNumeroMensajes.setText("Cantidad de Mensajes: "+ 0+"");
                    txNumeroMensajes.setVisibility(View.VISIBLE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                txNumeroMensajes.setText("Cantidad de Mensajes: "+0+"");
                txNumeroMensajes.setVisibility(View.VISIBLE);
            }
        }
    };

    public void registrarServicioComprado(final int idServicioComprado, final int estado){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_REGISTAR_SERVICIO_COMPRADO);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idServicioComprado");
                    fromProp.setValue(idServicioComprado);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("estado");
                    fromProp1.setValue(estado);
                    fromProp1.setType(int.class);
                    request.addProperty(fromProp1);

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
                Toast.makeText(VerMensajesDifuntoActivity.this, "Mensaje Actualizado!", Toast.LENGTH_LONG).show();
                showProgress(false);
                loadMensajesList(getCurrentUser().getIdDifunto());
            }
            else{
                Toast.makeText(VerMensajesDifuntoActivity.this, "No se ha podido autorizar el mensaje!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }
    };

}

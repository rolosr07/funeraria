package com.funeraria.funeraria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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


public class VerFloresCompradasDifuntoActivity extends Base {

    private TextView txNumeroFlores;
    private ImageView imageView;
    private TextView txNombreUsuario;
    private TextView txFechaCompra;

    private String webResponse = "";
    private String webResponseImages = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;
    private Spinner spinnerFlores;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoYTipoDeServicioList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_flores_compradas_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroFlores = (TextView)findViewById(R.id.txNumeroFlores);
        imageView = (ImageView)findViewById(R.id.imageView);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerFlores = (Spinner) findViewById(R.id.spinnerFlores);

        txNombreUsuario = (TextView)findViewById(R.id.txNombreUsuario);
        txFechaCompra = (TextView)findViewById(R.id.txFechaCompra);

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

            if(webResponse != null && !webResponse.equals("")){
                Type collectionType = new TypeToken<List<Difunto>>(){}.getType();
                List<Difunto> lcs = new Gson().fromJson( webResponse , collectionType);

                CustomAdapter adapter = new CustomAdapter(VerFloresCompradasDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Difunto dif = (Difunto) parent.getItemAtPosition(position);
                                showProgress(true);
                                loadFloresList(dif.getIdDifunto());
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        }
                );
                showProgress(false);
            }
            else{
                Toast.makeText(VerFloresCompradasDifuntoActivity.this, "No se ha podido establecer conexion con el servidor!", Toast.LENGTH_LONG).show();
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

    public void loadFloresList(final int idDifunto){
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
                    fromProp1.setValue(4);
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

                    txNumeroFlores.setText("Cantidad de Imagenes: "+ lcs.size());
                    txNumeroFlores.setVisibility(View.VISIBLE);

                    CustomAdapterServicio adapter = new CustomAdapterServicio(VerFloresCompradasDifuntoActivity.this, R.layout.simple_spinner_item,lcs);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerFlores.setAdapter(adapter);
                    spinnerFlores.setVisibility(View.VISIBLE);

                    spinnerFlores.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    byte[] decodedString = Base64.decode(servicio.getImagen(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    imageView.setImageBitmap(decodedByte);
                                    imageView.setVisibility(View.VISIBLE);

                                    txNombreUsuario.setText("Comprador: "+servicio.getNombreUsuario() + " " + servicio.getApellidoUsuario());
                                    txNombreUsuario.setVisibility(View.VISIBLE);

                                    txFechaCompra.setText("Fecha: "+servicio.getFechaCompra()+"");
                                    txFechaCompra.setVisibility(View.VISIBLE);

                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                    showProgress(false);
                }else{
                    txNumeroFlores.setText("Cantidad de Imagenes: "+ 0);
                    spinnerFlores.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    txNombreUsuario.setVisibility(View.GONE);
                    txFechaCompra.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                imageView.setVisibility(View.GONE);
                txNumeroFlores.setText("Cantidad de Imagenes: "+0);
                spinnerFlores.setVisibility(View.GONE);
                txNombreUsuario.setVisibility(View.GONE);
                txFechaCompra.setVisibility(View.GONE);
            }
        }
    };
}

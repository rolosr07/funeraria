package com.funeraria.funeraria;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.funeraria.funeraria.common.Adapters.LazyAdapter;
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


public class VerFloresCompradasDifuntoActivity extends Base {

    private TextView txNumeroFlores;

    private String webResponseImages = "";
    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorIdDifuntoYTipoDeServicioList";

    private ListView list;
    private LazyAdapter adapterList;

    private TextView nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_flores_compradas_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroFlores = (TextView)findViewById(R.id.txNumeroFlores);
        list = (ListView)findViewById(R.id.list);

        nombre = (TextView) findViewById(R.id.nombre);
        nombre.setText(getCurrentUser().getNombreDifunto());

        showProgress(true);
        loadFloresList(getCurrentUser().getIdDifunto());
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

                    adapterList = new LazyAdapter(VerFloresCompradasDifuntoActivity.this, lcs);
                    list.setAdapter(adapterList);

                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView parent, View view, int position, long id) {

                        }
                    });

                    showProgress(false);
                }else{
                    txNumeroFlores.setText("Cantidad de Imagenes: "+ 0);
                    txNumeroFlores.setVisibility(View.VISIBLE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                txNumeroFlores.setText("Cantidad de Imagenes: "+0);
                txNumeroFlores.setVisibility(View.VISIBLE);
            }
        }
    };
}

package com.funeraria.funeraria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomPagerServicesPlacaAdapter;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.Adapters.CustomAdapterRestos;
import com.funeraria.funeraria.common.Adapters.CustomAdapterServicio;
import com.funeraria.funeraria.common.entities.PlacaInformation;
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

    private EditText esquelaPersonal;

    private TextView nombre;
    private TextView encabezado;

    private String webResponseServices = "";
    private String webResponseRestos = "";
    private String webResponseRegistro = "";
    private String webResponseServicesAdquiridos= "";
    private Thread thread;
    private Handler handler = new Handler();

    private Spinner spinnerEsquela;
    private Spinner spinnerRestos;

    private ViewPager pagerImagenSuperior;
    private ViewPager pagerOrla;

    private Button buttonRegistrar;

    private List<Servicio> servicioImagenSuperiorList = new ArrayList<Servicio>();
    private List<Servicio> servicioImagenOrlaList = new ArrayList<Servicio>();
    private List<Servicio> servicioImagenEsquelaList = new ArrayList<Servicio>();
    private List<Restos> restosList = new ArrayList<Restos>();

    private final String METHOD_NAME_GET_SERVICES_LIST = "getServiciosList";
    private final String METHOD_NAME_GET_RESTOS_LIST = "getRestosList";
    private final String METHOD_NAME_REGISTRAR_PLACA = "registrarPlaca";
    private final String METHOD_NAME_GET_PLACA_INFORMATION = "getPlacaInformation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placa);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        esquelaPersonal = (EditText) findViewById(R.id.esquelaPersonal);

        nombre = (TextView) findViewById(R.id.nombre);
        encabezado = (TextView) findViewById(R.id.encabezado);

        spinnerEsquela = (Spinner) findViewById(R.id.spinnerEsquela);
        spinnerRestos = (Spinner) findViewById(R.id.spinnerRestos);

        pagerImagenSuperior = (ViewPager) findViewById(R.id.pagerImagenSuperior);
        pagerOrla = (ViewPager) findViewById(R.id.pagerOrla);

        showProgress(true);
        loadServicesList();

        buttonRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Servicio imagenSuperior = servicioImagenSuperiorList.get(pagerImagenSuperior.getCurrentItem());
                Servicio imagenOrla = servicioImagenOrlaList.get(pagerOrla.getCurrentItem());
                //Servicio esquela = (Servicio)spinnerEsquela.getSelectedItem();
                Restos restos = (Restos)spinnerRestos.getSelectedItem();
                String esquelaPersonalText = "";

                //if(!esquela.getTexto().equals(esquelaPersonal.getText().toString())){
                    esquelaPersonalText = esquelaPersonal.getText().toString();
                //}
                showProgress(true);
                //registarInscripcion(dif.getIdDifunto(),imagenSuperior.getIdServicio(),imagenOrla.getIdServicio(),esquela.getIdServicio(),restos.getIdLugarRestos(),esquelaPersonalText);

                if(getCurrentUser().getIdDifunto() != 0){
                    registarInscripcion(getCurrentUser().getIdDifunto(),imagenSuperior.getIdServicio(),imagenOrla.getIdServicio(),0,restos.getIdLugarRestos(),esquelaPersonalText);
                }else{
                    Toast.makeText(getApplicationContext(), "Debe primero registrar un difunto !", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView nextImagenSuperior = (ImageView) findViewById(R.id.nextImagenSuperior);
        nextImagenSuperior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pagerImagenSuperior.setCurrentItem(pagerImagenSuperior.getCurrentItem()+1);
            }
        });

        ImageView prevImagenSuperior = (ImageView) findViewById(R.id.prevImagenSuperior);
        prevImagenSuperior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pagerImagenSuperior.setCurrentItem(pagerImagenSuperior.getCurrentItem()-1);
            }
        });

        ImageView nextOrla = (ImageView) findViewById(R.id.nextOrla);
        nextOrla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pagerOrla.setCurrentItem(pagerOrla.getCurrentItem()+1);
            }
        });

        ImageView prevImagenOrla = (ImageView) findViewById(R.id.prevImagenOrla);
        prevImagenOrla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pagerOrla.setCurrentItem(pagerOrla.getCurrentItem()-1);
            }
        });
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

                    CustomPagerServicesPlacaAdapter mCustomPagerAdapter = new CustomPagerServicesPlacaAdapter(getApplicationContext(), servicioImagenSuperiorList);
                    pagerImagenSuperior.setAdapter(mCustomPagerAdapter);

                }
                if(servicioImagenOrlaList.size() > 0){

                    CustomPagerServicesPlacaAdapter mCustomPagerAdapter = new CustomPagerServicesPlacaAdapter(getApplicationContext(), servicioImagenOrlaList);
                    pagerOrla.setAdapter(mCustomPagerAdapter);

                    showProgress(true);
                    loadRestosList();
                }
                if(servicioImagenEsquelaList.size() > 0){

                    CustomAdapterServicio adapterEsquela = new CustomAdapterServicio(PlacaActivity.this, R.layout.simple_spinner_item,servicioImagenEsquelaList);
                    adapterEsquela.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerEsquela.setAdapter(adapterEsquela);

                    spinnerEsquela.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    esquelaPersonal.setText(servicio.getTexto());

                                    if(getCurrentUser().getIdDifunto() != 0){
                                        showProgress(true);
                                        loadServicesList(getCurrentUser().getIdDifunto());
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Debe primero registrar un difunto!", Toast.LENGTH_LONG).show();
                                    }



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
                restosList = new Gson().fromJson( webResponseRestos , collectionType);

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
                Toast.makeText(PlacaActivity.this, "Información enviada con exito!", Toast.LENGTH_LONG).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent i = new Intent(PlacaActivity.this, MainActivityAdmin.class);
                        finish();
                        finishAffinity();
                        startActivity(i);
                    }
                }, 2000);
            }else{
                Toast.makeText(PlacaActivity.this, "Error al realizar Inscripción!", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void loadServicesList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_PLACA_INFORMATION);

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
                    webResponseServicesAdquiridos = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIServicesAdquiridos);
            }
        };

        thread.start();
    }

    final Runnable createUIServicesAdquiridos = new Runnable() {

        public void run(){

            showProgress(false);
            if(!webResponseServicesAdquiridos.equals("") && !webResponseServicesAdquiridos.equals("[]")){
                Type collectionType = new TypeToken<List<PlacaInformation>>(){}.getType();
                List<PlacaInformation> placaInformationList = new Gson().fromJson( webResponseServicesAdquiridos , collectionType);
                PlacaInformation placaInformation = placaInformationList.get(0);
                nombre.setText(placaInformation.getNombre()+" "+placaInformation.getApellidos());
                encabezado.setText("Actualizar placa de");
                buttonRegistrar.setText("Actualizar Placa");
                int i = 0;
                for(Servicio ser : servicioImagenSuperiorList){
                    if(ser.getIdServicio() == placaInformation.getIdImagenSuperior()){
                        pagerImagenSuperior.setCurrentItem(i, true);
                    }else{
                        i++;
                    }
                }

                i = 0;
                for(Servicio ser : servicioImagenOrlaList){
                    if(ser.getIdServicio() == placaInformation.getIdImagenOrla()){
                        pagerOrla.setCurrentItem(i, true);
                    }else{
                        i++;
                    }
                }

                i = 0;
                for(Servicio ser : servicioImagenEsquelaList){
                    if(ser.getIdServicio() == placaInformation.getIdEsquela()){
                        spinnerEsquela.setSelection(i);
                    }else{
                        i++;
                    }
                }

                i = 0;
                for(Restos ser : restosList){
                    if(ser.getIdLugarRestos() == placaInformation.getIdNombreLugarRestos()){
                        spinnerRestos.setSelection(i);
                    }else{
                        i++;
                    }
                }
            }
        }
    };
}

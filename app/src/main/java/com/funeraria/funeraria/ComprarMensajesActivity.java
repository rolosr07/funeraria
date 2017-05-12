package com.funeraria.funeraria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Base;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class ComprarMensajesActivity extends Base {

    private EditText edMensajePersonal;
    private String webResponseComprar = "";
    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME_COMPRAR_SERVICIO = "comprarMensaje";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_mensajes);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        edMensajePersonal = (EditText) findViewById(R.id.edMensajePersonal);

        Button buttonComprar = (Button) findViewById(R.id.buttonComprar);
        buttonComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!edMensajePersonal.getText().toString().equals("")){
                    String mensajePersonal = edMensajePersonal.getText().toString();

                    if(!validateUser()){
                        //showDialogUser(ComprarMensajesActivity.this, 0);
                        Intent i = new Intent(ComprarMensajesActivity.this, RegistroUsuarioActivity.class);
                        startActivity(i);
                    }else{
                        if(!validarUsuarioSeleccionoFamiliar()){
                            showDialogSeleccionarFamiliar(ComprarMensajesActivity.this);
                        }else{
                            showProgress(true);
                            comprarServicio(getCurrentUser().getIdUsuario(),getCurrentUser().getIdDifunto(),mensajePersonal);
                        }
                    }
                }else{
                    edMensajePersonal.setError(getString(R.string.error_field_required));
                    edMensajePersonal.requestFocus();
                }
            }
        });

        TextView nameAdmin = (TextView) findViewById(R.id.nameAdmin);
        if(getCurrentUser() != null && getCurrentUser().getIdDifunto() != 0) {
            nameAdmin.setText(getCurrentUser().getNombreDifunto());
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void comprarServicio(final int idUsuario, final int idDifunto, final String mensajePersonal){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_COMPRAR_SERVICIO);

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

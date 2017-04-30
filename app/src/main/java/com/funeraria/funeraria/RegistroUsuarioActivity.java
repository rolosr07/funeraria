package com.funeraria.funeraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.funeraria.funeraria.common.Base;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class RegistroUsuarioActivity extends Base {

    private Button btnRegistrarUsuario;

    private EditText editNombre;
    private EditText editApellido;
    private EditText editEmail;

    private String webResponse = "";
    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME = "registroUsuario";

    private int idDifunto = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);

        initView();

        if(getIntent().getExtras().containsKey("idDifunto")){
            idDifunto = getIntent().getExtras().getInt("idDifunto");
        }

        btnRegistrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editNombre.getWindowToken(), 0);
                registrarUsuario();
            }
        });

        editNombre.requestFocus();
    }

    public void registrarUsuario() {

        // Reset errors.
        editNombre.setError(null);
        editApellido.setError(null);
        editEmail.setError(null);

        // Store values at the time of the login attempt.
        String name = editNombre.getText().toString();
        String apellido = editApellido.getText().toString();
        String email = editEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            editNombre.setError(getString(R.string.error_field_required));
            focusView = editNombre;
            cancel = true;
        }

        if (TextUtils.isEmpty(apellido)) {
            editApellido.setError(getString(R.string.error_field_required));
            focusView = editApellido;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_field_required));
            focusView = editEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            showProgress(false);
        } else {
            String tipoUsuario = "user";
            registrarUsuario(name,apellido,email,tipoUsuario);
        }
    }

    public void registrarUsuario(final String nombre, final String apellido, final String email, final String tipoUsuario){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME);
                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("nombre");
                    fromProp.setValue(nombre);
                    fromProp.setType(String.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp2 = new PropertyInfo();
                    fromProp2.setName("apellido");
                    fromProp2.setValue(apellido);
                    fromProp2.setType(String.class);
                    request.addProperty(fromProp2);

                    PropertyInfo fromProp3 = new PropertyInfo();
                    fromProp3.setName("email");
                    fromProp3.setValue(email);
                    fromProp3.setType(String.class);
                    request.addProperty(fromProp3);

                    PropertyInfo fromProp4 = new PropertyInfo();
                    fromProp4.setName("idDifunto");
                    fromProp4.setValue(idDifunto);
                    fromProp4.setType(int.class);
                    request.addProperty(fromProp4);

                    PropertyInfo fromProp5 = new PropertyInfo();
                    fromProp5.setName("tipoUsuario");
                    fromProp5.setValue(tipoUsuario);
                    fromProp5.setType(String.class);
                    request.addProperty(fromProp5);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_USER);

                    androidHttpTransport.call(SOAP_ACTION_USER, envelope);
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
                btnRegistrarUsuario.setEnabled(false);
                SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                prefs.edit().putString("USER_DATA", webResponse).apply();
                showProgress(false);
                Toast.makeText(RegistroUsuarioActivity.this, "Usuario Registrado con exito!", Toast.LENGTH_LONG).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent i = new Intent(RegistroUsuarioActivity.this, MainActivityUser.class);
                        finish();
                        finishAffinity();
                        startActivity(i);
                    }
                }, 2000);

            }else{
                Toast.makeText(RegistroUsuarioActivity.this, "No se ha podido registrar el Usuario!", Toast.LENGTH_LONG).show();
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

    public void initView() {

        editNombre = (EditText) findViewById(R.id.etNombreUsuario);
        editApellido = (EditText) findViewById(R.id.etApellidoUsuario);
        editEmail = (EditText) findViewById(R.id.etEmail);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        btnRegistrarUsuario = (Button) findViewById(R.id.btnRegistrarUsuario);
    }
}

package com.funeraria.funeraria;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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


public class ConfiguracionAdminActivity extends Base {

    private TextView tvCodigoAdmin;
    private TextView tvUserNameAdmin;
    private EditText tvNombreAdmin;
    private EditText tvApellidoAdmin;
    private EditText tvEmailAdmin;
    private EditText tvpasswordAdmin;

    private String webResponse = "";
    private Thread thread;
    private Handler handler = new Handler();

    private View mProgressView;
    private View mLoginFormView;

    private final String METHOD_NAME = "updateUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_admin);

        tvCodigoAdmin = (TextView) findViewById(R.id.tvCodigoAdmin);
        tvUserNameAdmin = (TextView) findViewById(R.id.tvUserNameAdmin);
        tvNombreAdmin = (EditText) findViewById(R.id.tvNombreAdmin);
        tvApellidoAdmin = (EditText) findViewById(R.id.tvApellidoAdmin);
        tvEmailAdmin = (EditText) findViewById(R.id.tvEmailAdmin);
        tvpasswordAdmin = (EditText) findViewById(R.id.tvpasswordAdmin);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            String[] userData = prefs.getString("USER_DATA","").split(",");

            tvCodigoAdmin.setText(userData[5]);
            tvUserNameAdmin.setText(userData[7]);
            tvNombreAdmin.setText(userData[0]);
            tvApellidoAdmin.setText(userData[1]);
            tvEmailAdmin.setText(userData[3]);
            tvpasswordAdmin.setText(userData[6]);
        }

        Button btnCerrarSession = (Button) findViewById(R.id.btnCerrarSession);
        btnCerrarSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                prefs.edit().putString("USER_DATA", "").apply();
                Intent intent = new Intent(ConfiguracionAdminActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finishAffinity();
                finish();
                startActivity(intent);
            }
        });

        Button btnModificar = (Button) findViewById(R.id.btnModificar);
        btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modificarUsuario();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.configuracion_admin, menu);
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

    public void modificarUsuario() {

        // Reset errors.
        tvNombreAdmin.setError(null);
        tvApellidoAdmin.setError(null);
        tvEmailAdmin.setError(null);
        tvpasswordAdmin.setError(null);

        // Store values at the time of the login attempt.
        String nombre = tvNombreAdmin.getText().toString();
        String apellido = tvApellidoAdmin.getText().toString();
        String email = tvEmailAdmin.getText().toString();
        String password = tvpasswordAdmin.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(nombre)) {
            tvNombreAdmin.setError(getString(R.string.error_field_required));
            focusView = tvNombreAdmin;
            cancel = true;
        }

        if (TextUtils.isEmpty(apellido)) {
            tvApellidoAdmin.setError(getString(R.string.error_field_required));
            focusView = tvApellidoAdmin;
            cancel = true;
        }

        if (TextUtils.isEmpty(apellido)) {
            tvApellidoAdmin.setError(getString(R.string.error_field_required));
            focusView = tvApellidoAdmin;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            tvEmailAdmin.setError(getString(R.string.error_field_required));
            focusView = tvEmailAdmin;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            tvpasswordAdmin.setError(getString(R.string.error_invalid_password));
            focusView = tvpasswordAdmin;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            login(nombre,apellido,email,password);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    public void login(final String nombre,final String apellido, final String email, final String password){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME);

                    SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

                    String[] userData = prefs.getString("USER_DATA","").split(",");

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idUsuario");
                    fromProp.setValue(userData[5]);
                    fromProp.setType(String.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("nombre");
                    fromProp1.setValue(nombre);
                    fromProp1.setType(String.class);
                    request.addProperty(fromProp1);

                    PropertyInfo fromProp2 = new PropertyInfo();
                    fromProp2.setName("apellido");
                    fromProp2.setValue(apellido);
                    fromProp2.setType(String.class);
                    request.addProperty(fromProp2);

                    PropertyInfo fromProp3 = new PropertyInfo();
                    fromProp3.setName("userName");
                    fromProp3.setValue(userData[7]);
                    fromProp3.setType(String.class);
                    request.addProperty(fromProp3);

                    PropertyInfo fromProp4 = new PropertyInfo();
                    fromProp4.setName("password");
                    fromProp4.setValue(password);
                    fromProp4.setType(String.class);
                    request.addProperty(fromProp4);

                    PropertyInfo fromProp5 = new PropertyInfo();
                    fromProp5.setName("email");
                    fromProp5.setValue(email);
                    fromProp5.setType(String.class);
                    request.addProperty(fromProp5);

                    PropertyInfo fromProp6 = new PropertyInfo();
                    fromProp6.setName("rol");
                    fromProp6.setValue(userData[2]);
                    fromProp6.setType(String.class);
                    request.addProperty(fromProp6);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_USER);

                    androidHttpTransport.call(SOAP_ACTION_USER, envelope);
                    Object response = envelope.getResponse();
                    webResponse = response.toString();

                    boolean result = Boolean.valueOf(webResponse);
                    if(result){
                        prefs.edit().putString("USER_DATA", "").apply();
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
                if(webResponse != ""){
                    handler.post(createUI);
                }else{
                    showProgress(false);
                    tvNombreAdmin.setError(getString(R.string.error_server));
                }

            }
        };

        thread.start();
    }

    final Runnable createUI = new Runnable() {

        public void run(){

            boolean result = Boolean.valueOf(webResponse);
            if(result){

                showProgress(false);
                Toast.makeText(ConfiguracionAdminActivity.this, "Se ha actualizado el usuario, por favor inicie sesión nuevamente!", Toast.LENGTH_LONG).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent i = new Intent(ConfiguracionAdminActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finishAffinity();
                        finish();
                        startActivity(i);
                    }
                }, 3000);

            }else{
                tvNombreAdmin.setError(getString(R.string.error_server));
                showProgress(false);
            }
        }
    };
}

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.CustomAdapter;
import com.funeraria.funeraria.common.Difunto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.util.List;


public class MainActivityPresenter extends Base {

    private View mProgressView;
    private View mLoginFormView;

    private String webResponse = "";

    private Thread thread;
    private Handler handler = new Handler();

    private Spinner spinnerDifuntos;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_presenter);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        spinnerDifuntos = (Spinner) findViewById(R.id.spinnerDifuntos);

        showProgress(true);
        loadDifuntosList();

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            String[] userData = prefs.getString("USER_DATA","").split(",");
            TextView nameAdmin = (TextView) findViewById(R.id.nameAdmin);
            nameAdmin.setText(userData[0]+" "+userData[1]);
        }

        Button buttonRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                showProgress(true);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Difunto dif = (Difunto)spinnerDifuntos.getSelectedItem();
                        Intent i = new Intent(MainActivityPresenter.this, VerPlacaActivity.class);
                        i.putExtra("idDifunto", dif.getIdDifunto());
                        startActivity(i);
                        showProgress(false);
                    }
                }, 1000);
            }
        });

        Button btnCerrarSession = (Button) findViewById(R.id.btnCerrarSession);
        btnCerrarSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                prefs.edit().putString("USER_DATA", "").apply();
                Intent intent = new Intent(MainActivityPresenter.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finishAffinity();
                finish();
                startActivity(intent);
            }
        });
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

                CustomAdapter adapter = new CustomAdapter(MainActivityPresenter.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinnerDifuntos.setAdapter(adapter);
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
        // the progress spinnerDifuntos.
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
        getMenuInflater().inflate(R.menu.main_activity_presenter, menu);
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
}

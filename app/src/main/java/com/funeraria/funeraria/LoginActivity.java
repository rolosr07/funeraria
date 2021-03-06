package com.funeraria.funeraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Logo;
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

/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Base {

    private EditText userNameView;
    private EditText mPasswordView;

    private String webResponse = "";
    private String webResponseLogo = "";
    private Thread thread;
    private Handler handler = new Handler();

    private ImageView logo;

    private final String METHOD_NAME = "login";
    private final String METHOD_NAME_LOGO = "loadLogo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Usuario user = getCurrentUser();
        if(user != null)
        {
            redirect(user.getRol());
        }else{

            userNameView = (EditText) findViewById(R.id.userName);

            mPasswordView = (EditText) findViewById(R.id.password);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
                    attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);

            logo = (ImageView) findViewById(R.id.logo);

            SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

            if(!prefs.getString("LOGO_APP","").equals(""))
            {
                String image = prefs.getString("LOGO_APP","");

                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                logo.setImageBitmap(decodedByte);

            }else{
                showProgress(true);
                loadLogo();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        userNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userName = userNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userName)) {
            userNameView.setError(getString(R.string.error_field_required));
            focusView = userNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            login(userName,password);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    public void login(final String userName, final String password){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME);
                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("userName");
                    fromProp.setValue(userName);
                    fromProp.setType(String.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp2 = new PropertyInfo();
                    fromProp2.setName("password");
                    fromProp2.setValue(password);
                    fromProp2.setType(String.class);
                    request.addProperty(fromProp2);

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

    private void redirect(String rol){

        if(rol.equals("admin")){
            Intent i = new Intent(LoginActivity.this, MainActivityAdmin.class);
            finish();
            startActivity(i);
        }else if(rol.equals("user")){
            Intent i = new Intent(LoginActivity.this, MainActivityUser.class);
            finish();
            startActivity(i);
        }else if(rol.equals("presenter")){
            Intent i = new Intent(LoginActivity.this, VerImagenesYMensajesActivity.class);
            finish();
            startActivity(i);
        }
    }

    final Runnable createUI = new Runnable() {

        public void run(){

            if(!webResponse.equals("") && !webResponse.equals("[]")){
                Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
                List<Usuario> usuarios = new Gson().fromJson( webResponse , collectionType);

                SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                prefs.edit().putString("USER_DATA", webResponse).apply();

                redirect(usuarios.get(0).getRol());
                showProgress(false);
            }else{
                userNameView.setError(getString(R.string.error_invalid_email));
                showProgress(false);
            }
        }
    };

    public void loadLogo(){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME_LOGO);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_USER);

                    androidHttpTransport.call(SOAP_ACTION_USER, envelope);
                    Object response = envelope.getResponse();
                    webResponseLogo = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUILoadLogo);
            }
        };

        thread.start();
    }

    final Runnable createUILoadLogo = new Runnable() {

        public void run(){

            if(!webResponseLogo.equals("")){

                Type collectionType = new TypeToken<List<Logo>>(){}.getType();
                List<Logo> logoList = new Gson().fromJson( webResponseLogo , collectionType);

                SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                prefs.edit().putString("LOGO_APP", logoList.get(0).getImagen()).apply();

                byte[] decodedString = Base64.decode(logoList.get(0).getImagen(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                logo.setImageBitmap(decodedByte);

                showProgress(false);
            }else{
                showProgress(false);
            }
        }
    };
}




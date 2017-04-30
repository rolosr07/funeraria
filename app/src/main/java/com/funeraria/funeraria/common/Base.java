package com.funeraria.funeraria.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.view.View;

import com.funeraria.funeraria.BuscarDifuntoActivity;
import com.funeraria.funeraria.RegistroUsuarioActivity;
import com.funeraria.funeraria.common.entities.Imagen;
import com.funeraria.funeraria.common.entities.PlacaInformation;
import com.funeraria.funeraria.common.entities.Servicio;
import com.funeraria.funeraria.common.entities.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paypal.android.MEP.PayPal;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.util.List;

public class Base extends Activity {

    private String IP = "e-propiedadescr.com";
    //private String IP = "192.168.43.195:1233";
    public final String NAMESPACE_USER = "http://"+IP+"/webservices/user.php";
    public final String URL_USER = "http://"+IP+"/webservices/user.php?wsdl";
    public final String SOAP_ACTION_USER = "http://"+IP+"/webservices/user.php?wsdl";

    public final String NAMESPACE_DIFUNTO = "http://"+IP+"/webservices/difunto.php";
    public final String URL_DIFUNTO = "http://"+IP+"/webservices/difunto.php?wsdl";
    public final String SOAP_ACTION_DIFUNTO = "http://"+IP+"/webservices/difunto.php?wsdl";

    public final String NAMESPACE_SERVICIO = "http://"+IP+"/webservices/servicio.php";
    public final String URL_SERVICIO = "http://"+IP+"/webservices/servicio.php?wsdl";
    public final String SOAP_ACTION_SERVICIO = "http://"+IP+"/webservices/servicio.php?wsdl";

    //public final String PAYPAL_KEY = "APP-80W284485P519543T";
    //public final int ENV = PayPal.ENV_SANDBOX;
    //public final String RECIPIENT = "rsoto07.2-facilitator@gmail.com";

    public final String PAYPAL_KEY = "APP-65L593624K297163N";
    public final int ENV = PayPal.ENV_LIVE;
    public final String RECIPIENT = "luis@espacioon.com";

    private static Usuario user = null;
    private static PlacaInformation placaInformation = null;
    private static List<Imagen> listImagen = null;
    private static List<Servicio> listMensajes = null;
    private static List<Servicio> listFloresYVelas = null;

    public View mProgressView;
    public View mLoginFormView;

    public String PLACA_INFORMATION = "PLACA_INFORMATION_DATA";
    public String IMAGENES_DATA = "IMAGENES__DATA";
    public String MENSAJES_DATA = "MENSAJES_DATA";
    public String FLORES_Y_VELAS_DATA = "FLORES_Y_VELAS_DATA";

    private String webResponse = "";
    private Thread thread;
    private Handler handler = new Handler();
    private final String METHOD_NAME = "login";

    private static Usuario getUser() {
        return user;
    }

    public static void setUser(Usuario user) {
        Base.user = user;
    }

    private static PlacaInformation getPlacaInformation() {
        return placaInformation;
    }

    public static void setPlacaInformation(PlacaInformation placaInformation) {
        Base.placaInformation = placaInformation;
    }

    private static List<Imagen> getListImagen() {
        return listImagen;
    }

    public static void setListImagen(List<Imagen> listImagen) {
        Base.listImagen = listImagen;
    }

    private static List<Servicio> getListMensajes() {
        return listMensajes;
    }

    public static void setListMensajes(List<Servicio> listMensajes) {
        Base.listMensajes = listMensajes;
    }

    private static List<Servicio> getListFloresYVelas() {
        return listFloresYVelas;
    }

    public static void setListFloresYVelas(List<Servicio> listFloresYVelas) {
        Base.listFloresYVelas = listFloresYVelas;
    }

    @SuppressLint("ObsoleteSdkInt")
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

    public Usuario getCurrentUser(){

        if(getUser() == null){
            SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

            if(!prefs.getString("USER_DATA","").equals(""))
            {
                Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
                List<Usuario> usuarios = new Gson().fromJson( prefs.getString("USER_DATA","") , collectionType);

                setUser(usuarios.get(0));
            }
        }

        return getUser();
    }

    public PlacaInformation getCurrentPlaca(){

        if(getPlacaInformation() == null){
            SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

            if(!prefs.getString(PLACA_INFORMATION,"").equals(""))
            {
                Type collectionType = new TypeToken<List<PlacaInformation>>(){}.getType();
                List<PlacaInformation> placa = new Gson().fromJson( prefs.getString(PLACA_INFORMATION,"") , collectionType);

                setPlacaInformation(placa.get(0));
            }
        }

        return getPlacaInformation();
    }

    public List<Imagen> getCurrentImagenes(){

        if(getListImagen() == null){
            SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

            if(!prefs.getString(IMAGENES_DATA,"").equals(""))
            {
                Type collectionType = new TypeToken<List<Imagen>>(){}.getType();
                List<Imagen> listImagen = new Gson().fromJson( prefs.getString(IMAGENES_DATA,"") , collectionType);

                setListImagen(listImagen);
            }
        }

        return getListImagen();
    }

    public List<Servicio> getCurrentMensajes(){

        if(getListMensajes() == null){
            SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

            if(!prefs.getString(MENSAJES_DATA,"").equals(""))
            {
                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> listServicios = new Gson().fromJson( prefs.getString(MENSAJES_DATA,"") , collectionType);

                setListMensajes(listServicios);
            }
        }

        return getListMensajes();
    }

    public List<Servicio> getCurrentFloresYVelas(){

        if(getListFloresYVelas() == null){
            SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);

            if(!prefs.getString(FLORES_Y_VELAS_DATA,"").equals(""))
            {
                Type collectionType = new TypeToken<List<Servicio>>(){}.getType();
                List<Servicio> listServicios = new Gson().fromJson( prefs.getString(FLORES_Y_VELAS_DATA,"") , collectionType);

                setListFloresYVelas(listServicios);
            }
        }
        return getListFloresYVelas();
    }

    public void showDialogUser(final Context context, final int idDifunto){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Registro Requerido");
        builder.setMessage("Desea ingresar sus Datos?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                finishAffinity();
                Intent i = new Intent(context, RegistroUsuarioActivity.class);
                i.putExtra("idDifunto", idDifunto);
                startActivity(i);
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean validateUser(){

        //return !(getCurrentUser() == null || getCurrentUser().getRol().equals("admin"));
        return !(getCurrentUser() == null);
    }

    public boolean validarUsuarioSeleccionoFamiliar(){

        return (getCurrentUser() != null && getCurrentUser().getIdDifunto() != 0);
    }

    public void showDialogSeleccionarFamiliar(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Debe Seleccionar un Familiar");
        builder.setMessage("Desea ir a pantalla de busqueda?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                finishAffinity();
                Intent i = new Intent(context, BuscarDifuntoActivity.class);
                startActivity(i);
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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
                    setUser(null);
                    if(!webResponse.equals("") && !webResponse.equals("[]")){

                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString("USER_DATA", webResponse).apply();
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}

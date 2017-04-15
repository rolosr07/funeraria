package com.funeraria.funeraria.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;

import com.paypal.android.MEP.PayPal;

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


    public View mProgressView;
    public View mLoginFormView;

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

}

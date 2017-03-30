package com.funeraria.funeraria.common;

import android.app.Activity;

/**
 * Created by Rolo on 25/03/2017.
 */
public class Base extends Activity {

    private String IP = "e-propiedadescr.com/";
    public final String NAMESPACE_USER = "http://"+IP+"/webServices/user.php";
    public final String URL_USER = "http://"+IP+"/webServices/user.php?wsdl";
    public final String SOAP_ACTION_USER = "http://"+IP+"/webServices/user.php?wsdl";

    public final String NAMESPACE_DIFUNTO = "http://"+IP+"/webServices/difunto.php";
    public final String URL_DIFUNTO = "http://"+IP+"/webServices/difunto.php?wsdl";
    public final String SOAP_ACTION_DIFUNTO = "http://"+IP+"/webServices/difunto.php?wsdl";

    public final String NAMESPACE_SERVICIO = "http://"+IP+"/webServices/servicio.php";
    public final String URL_SERVICIO = "http://"+IP+"/webServices/servicio.php?wsdl";
    public final String SOAP_ACTION_SERVICIO = "http://"+IP+"/webServices/servicio.php?wsdl";

}

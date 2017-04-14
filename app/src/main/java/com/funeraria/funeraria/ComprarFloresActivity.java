package com.funeraria.funeraria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Adapters.CustomAdapterServicio;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
import com.funeraria.funeraria.common.entities.Servicio;
import com.funeraria.funeraria.common.entities.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalInvoiceData;
import com.paypal.android.MEP.PayPalInvoiceItem;
import com.paypal.android.MEP.PayPalPayment;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;


public class ComprarFloresActivity extends Base {

    private TextView txNumeroFlores;
    private ImageView imageView;
    private TextView txDuracion;
    private TextView  txPrecio;

    private String webResponse = "";
    private String webResponseImages = "";
    private String webResponseComprar = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;
    private Spinner spinnerFlores;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosPorUsuarioList";
    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorTipoDeServicioList";
    private final String METHOD_NAME_COMPRAR_SERVICIO = "comprarServicio";

    private Usuario usuarioActual;

    private boolean _paypalLibraryInit = false;
    private CheckoutButton launchPayPalButton;
    public static final int PAYPAL_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_flores);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroFlores = (TextView)findViewById(R.id.txNumeroFlores);
        imageView = (ImageView)findViewById(R.id.imageView);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerFlores = (Spinner) findViewById(R.id.spinnerFlores);

        txDuracion = (TextView)findViewById(R.id.txDuracion);
        txPrecio = (TextView)findViewById(R.id.txPrecio);

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
            List<Usuario> usuarios = new Gson().fromJson( prefs.getString("USER_DATA","") , collectionType);
            usuarioActual = usuarios.get(0);
        }
        showProgress(true);
        loadDifuntosList();
        showProgress(true);
        loadFloresList();

        Button buttonComprar = (Button) findViewById(R.id.buttonComprar);
        buttonComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                int u = usuarioActual.getIdUsuario();
                Difunto d = (Difunto) spinner.getSelectedItem();
                Servicio s = (Servicio)spinnerFlores.getSelectedItem();
                comprarServicio(u,d.getIdDifunto(),s.getIdServicio());
            }
        });

        initLibrary();
        showPayPalButton();
    }

    public void initLibrary() {
        PayPal pp = PayPal.getInstance();

        if (pp == null) {  // Test to see if the library is already initialized

            // This main initialization call takes your Context, AppID, and target server
            pp = PayPal.initWithAppID(this, PAYPAL_KEY, ENV);


            // Required settings:

            // Set the language for the library
            pp.setLanguage("es_ES");

            // Some Optional settings:

            // Sets who pays any transaction fees. Value is:
            // FEEPAYER_SENDER, FEEPAYER_PRIMARYRECEIVER, FEEPAYER_EACHRECEIVER, and FEEPAYER_SECONDARYONLY
            pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);

            // true = transaction requires shipping
            pp.setShippingEnabled(true);

            _paypalLibraryInit = true;
        }
    }

    private void showPayPalButton() {

        // Generate the PayPal checkout button and save it for later use
        PayPal pp = PayPal.getInstance();
        launchPayPalButton = pp.getCheckoutButton(this, PayPal.BUTTON_278x43, CheckoutButton.TEXT_PAY);

        // The OnClick listener for the checkout button
        launchPayPalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a basic PayPal payment
                PayPalPayment payment = createPayment();
                Intent checkoutIntent = PayPal.getInstance().checkout(payment, getApplicationContext());
                startActivityForResult(checkoutIntent,PAYPAL_REQUEST_CODE);
            }
        });

        // Add the listener to the layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams (RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        launchPayPalButton.setLayoutParams(params);
        ((RelativeLayout) findViewById(R.id.RelativeLayout01)).addView(launchPayPalButton);
        ((RelativeLayout) findViewById(R.id.RelativeLayout01)).setGravity(Gravity.CENTER);
    }

    private PayPalPayment createPayment() {

        Difunto difunto = (Difunto) spinner.getSelectedItem();
        Servicio servicio = (Servicio)spinnerFlores.getSelectedItem();

        // Create a basic PayPalPayment.
        PayPalPayment payment = new PayPalPayment();
        // Sets the currency type for this payment.
        payment.setCurrencyType("USD");
        // Sets the recipient for the payment. This can also be a phone number.
        payment.setRecipient(RECIPIENT);
        // Sets the amount of the payment, not including tax and shipping amounts.
        payment.setSubtotal(new BigDecimal(servicio.getPrecio()));
        // Sets the payment type. This can be PAYMENT_TYPE_GOODS, PAYMENT_TYPE_SERVICE, PAYMENT_TYPE_PERSONAL, or PAYMENT_TYPE_NONE.
        payment.setPaymentType(PayPal.PAYMENT_TYPE_SERVICE);

        // PayPalInvoiceData can contain tax and shipping amounts. It also contains an ArrayList of PayPalInvoiceItem which can
        // be filled out. These are not required for any transaction.
        PayPalInvoiceData invoice = new PayPalInvoiceData();
        // Sets the tax amount.
        invoice.setTax(new BigDecimal("0"));
        // Sets the shipping amount.
        invoice.setShipping(new BigDecimal("0"));
        // PayPalInvoiceItem has several parameters available to it. None of these parameters is required.
        PayPalInvoiceItem item1 = new PayPalInvoiceItem();
        // Sets the name of the item.
        item1.setName(servicio.getNombre());
        item1.setID(String.valueOf(servicio.getIdServicio()));
        invoice.getInvoiceItems().add(item1);
        // Sets the PayPalPayment invoice data.
        payment.setInvoiceData(invoice);
        // Sets the merchant name. This is the name of your Application or Company.
        payment.setMerchantName("Memorial Informatica Manchuela");
        // Sets the description of the payment.
        payment.setDescription("Compra de flores para familiar: "+difunto.getNombre()+" "+difunto.getApellidos());
        // Sets the Custom ID. This is any ID that you would like to have associated with the payment.
        payment.setCustomID("8873482296");
        // Sets the Instant Payment Notification url. This url will be hit by the PayPal server upon completion of the payment.
        //payment.setIpnUrl("http://www.exampleapp.com/ipn");
        // Sets the memo. This memo will be part of the notification sent by PayPal to the necessary parties.
        payment.setMemo("Gracias por su compra de flores para familiar: "+difunto.getNombre()+" "+difunto.getApellidos());

        return payment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
            // The payment succeeded
            case Activity.RESULT_OK:
                String payKey = intent.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
                showProgress(true);
                int u = usuarioActual.getIdUsuario();
                Difunto d = (Difunto) spinner.getSelectedItem();
                Servicio s = (Servicio)spinnerFlores.getSelectedItem();
                comprarServicio(u,d.getIdDifunto(),s.getIdServicio());
                break;

            // The payment was canceled
            case Activity.RESULT_CANCELED:
                Toast.makeText(ComprarFloresActivity.this, "Compra cancelada.", Toast.LENGTH_SHORT).show();
                break;

            // The payment failed, get the error from the EXTRA_ERROR_ID and EXTRA_ERROR_MESSAGE
            case PayPalActivity.RESULT_FAILURE:
                String errorID = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
                String errorMessage = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
                Toast.makeText(ComprarFloresActivity.this, "No se ha podido realizar la compra!"+errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    public void loadDifuntosList(){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_DIFUNTO_LIST);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idUsuario");
                    fromProp.setValue(usuarioActual.getIdUsuario());
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

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

            if(webResponse != null && !webResponse.equals("")){
                Type collectionType = new TypeToken<List<Difunto>>(){}.getType();
                List<Difunto> lcs = new Gson().fromJson( webResponse , collectionType);

                CustomAdapter adapter = new CustomAdapter(ComprarFloresActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                showProgress(false);
            }
            else{
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

    public void loadFloresList(){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_GET_SERVICIOS_LIST);

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

                    txNumeroFlores.setText("Flores disponibles: "+ lcs.size());
                    txNumeroFlores.setVisibility(View.VISIBLE);

                    CustomAdapterServicio adapter = new CustomAdapterServicio(ComprarFloresActivity.this, R.layout.simple_spinner_item,lcs);
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                    spinnerFlores.setAdapter(adapter);
                    spinnerFlores.setVisibility(View.VISIBLE);
                    spinnerFlores.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
                                    showProgress(true);
                                    Servicio servicio = (Servicio)parent.getItemAtPosition(position);

                                    byte[] decodedString = Base64.decode(servicio.getImagen(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    imageView.setImageBitmap(decodedByte);
                                    imageView.setVisibility(View.VISIBLE);

                                    txDuracion.setText("Duración en pantalla: "+servicio.getTiempoMostrar()+" minutos");
                                    txDuracion.setVisibility(View.VISIBLE);

                                    txPrecio.setText("Precio: $"+servicio.getPrecio());
                                    txPrecio.setVisibility(View.VISIBLE);
                                    showProgress(false);
                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            }
                    );
                    showProgress(false);
                }else{
                    txNumeroFlores.setText("Cantidad de Velas: "+ 0);
                    spinnerFlores.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    txDuracion.setVisibility(View.GONE);
                    txPrecio.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                imageView.setVisibility(View.GONE);
                txNumeroFlores.setText("Cantidad de Velas: "+0);
                spinnerFlores.setVisibility(View.GONE);
                txDuracion.setVisibility(View.GONE);
                txPrecio.setVisibility(View.GONE);
            }
        }
    };

    public void comprarServicio(final int idUsuario, final int idDifunto, final int idServicio){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_SERVICIO, METHOD_NAME_COMPRAR_SERVICIO);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idServicio");
                    fromProp.setValue(idServicio);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

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
                Toast.makeText(ComprarFloresActivity.this, "Compra realizada!, ahora se podrá ver la vela en la placa de su familiar!", Toast.LENGTH_LONG).show();
                showProgress(false);
                Intent i = new Intent(ComprarFloresActivity.this, CompraExitoActivity.class);
                finish();
                startActivity(i);
            }
            else{
                Toast.makeText(ComprarFloresActivity.this, "No se ha podido realizar la compra!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }

        }
    };

}

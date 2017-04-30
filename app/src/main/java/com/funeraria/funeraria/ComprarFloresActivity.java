package com.funeraria.funeraria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.LazyAdapterComprar;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Servicio;
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

    private String webResponseImages = "";
    private String webResponseComprar = "";
    private Thread thread;
    private Handler handler = new Handler();

    private ListView list;
    private LazyAdapterComprar adapterList;
    private List<Servicio> lcs;

    private final String METHOD_NAME_GET_SERVICIOS_LIST = "getServiciosPorTipoDeServicioList";
    private final String METHOD_NAME_COMPRAR_SERVICIO = "comprarServicio";

    private CheckoutButton launchPayPalButton;
    private boolean _paypalLibraryInit = false;
    public static final int PAYPAL_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_flores);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroFlores = (TextView)findViewById(R.id.txNumeroFlores);
        list = (ListView)findViewById(R.id.list);

        showProgress(true);
        loadFloresList();

        Button buttonComprar = (Button) findViewById(R.id.buttonComprar);
        buttonComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!validateUser()){
                    showDialogUser(ComprarFloresActivity.this, 0);
                }else{
                    if(!validarUsuarioSeleccionoFamiliar()){
                        showDialogSeleccionarFamiliar(ComprarFloresActivity.this);
                    }else{
                        showProgress(true);
                        Servicio s = lcs.get(list.getSelectedItemPosition());
                        comprarServicio(getCurrentUser().getIdUsuario(),getCurrentUser().getIdDifunto(),s.getIdServicio());
                    }
                }
            }
        });

        TextView nameAdmin = (TextView) findViewById(R.id.nameAdmin);
        if(getCurrentUser() != null && getCurrentUser().getIdDifunto() != 0) {
            nameAdmin.setText(getCurrentUser().getNombreDifunto());
        }

        initLibrary();
        //showPayPalButton();
    }

    public void initLibrary() {
        PayPal pp = PayPal.getInstance();

        if (pp == null) {

            pp = PayPal.initWithAppID(this, PAYPAL_KEY, ENV);

            pp.setLanguage("es_ES");

            pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);

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
                if(!validateUser()){
                    showDialogUser(ComprarFloresActivity.this, 0);
                }else{
                    if(!validarUsuarioSeleccionoFamiliar()){
                        showDialogSeleccionarFamiliar(ComprarFloresActivity.this);
                    }else{
                        PayPalPayment payment = createPayment(list.getSelectedItemPosition());
                        Intent checkoutIntent = PayPal.getInstance().checkout(payment, getApplicationContext());
                        startActivityForResult(checkoutIntent,PAYPAL_REQUEST_CODE);
                    }
                }
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

    private PayPalPayment createPayment(int position) {

        Servicio servicio = lcs.get(position);

        // Create a basic PayPalPayment.
        PayPalPayment payment = new PayPalPayment();
        // Sets the currency type for this payment.
        payment.setCurrencyType("EUR");
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
        payment.setDescription("Compra de flores para familiar: "+getCurrentUser().getNombreDifunto());
        // Sets the Custom ID. This is any ID that you would like to have associated with the payment.
        payment.setCustomID("8873482296");
        // Sets the Instant Payment Notification url. This url will be hit by the PayPal server upon completion of the payment.
        //payment.setIpnUrl("http://www.exampleapp.com/ipn");
        // Sets the memo. This memo will be part of the notification sent by PayPal to the necessary parties.
        payment.setMemo("Gracias por su compra de flores para familiar: "+getCurrentUser().getNombreDifunto());

        return payment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
            // The payment succeeded
            case Activity.RESULT_OK:
               // String payKey = intent.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
                showProgress(true);
                Servicio servicio = lcs.get(list.getSelectedItemPosition());
                comprarServicio(getCurrentUser().getIdUsuario(),getCurrentUser().getIdDifunto(),servicio.getIdServicio());
                break;

            // The payment was canceled
            case Activity.RESULT_CANCELED:
                Toast.makeText(ComprarFloresActivity.this, "Compra cancelada.", Toast.LENGTH_SHORT).show();
                break;

            // The payment failed, get the error from the EXTRA_ERROR_ID and EXTRA_ERROR_MESSAGE
            case PayPalActivity.RESULT_FAILURE:
                //String errorID = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
                String errorMessage = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
                Toast.makeText(ComprarFloresActivity.this, "No se ha podido realizar la compra!"+errorMessage, Toast.LENGTH_LONG).show();
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
                lcs = new Gson().fromJson( webResponseImages , collectionType);

                if(lcs.size() > 0){

                    txNumeroFlores.setText("Flores disponibles: "+ lcs.size());
                    txNumeroFlores.setVisibility(View.VISIBLE);

                    adapterList = new LazyAdapterComprar(ComprarFloresActivity.this, lcs);
                    list.setAdapter(adapterList);

                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView parent, View view, int position, long id) {

                            if(!validateUser()){
                                showDialogUser(ComprarFloresActivity.this, 0);
                            }else{
                                if(!validarUsuarioSeleccionoFamiliar()){
                                    showDialogSeleccionarFamiliar(ComprarFloresActivity.this);
                                }else{
                                    PayPalPayment payment = createPayment(position);
                                    Intent checkoutIntent = PayPal.getInstance().checkout(payment, getApplicationContext());
                                    startActivityForResult(checkoutIntent,PAYPAL_REQUEST_CODE);
                                }
                            }

                        }
                    });

                    showProgress(false);
                }else{
                    txNumeroFlores.setText("Cantidad de Velas: "+ 0);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                txNumeroFlores.setText("Cantidad de Velas: "+0);
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

package com.funeraria.funeraria;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

public class RegistrarDifundoActivity extends Base {

    private EditText tvDateNacimiento;
    private EditText tvDisplayDateDeceso;
    private Button btnChangeDateNacimiento;
    private Button btnChangeDateDeceso;
    private Button btnRegistrarDifunto;

    private EditText editNombre;
    private EditText editApellido;

    private TextView encabezado;

    private int year;
    private int month;
    private int day;

    private int yearDe;
    private int monthDe;
    private int dayDe;

    static final int DATE_DIALOG_ID_NACIMIENTO = 999;
    static final int DATE_DIALOG_ID_DECESO = 998;

    private String webResponse = "";
    private String webResponseDifunto = "";
    private Thread thread;
    private Handler handler = new Handler();

    private final String METHOD_NAME = "registrarDifunto";
    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosPorUsuarioList";
    private final String METHOD_NAME_LOGIN = "login";

    private static Difunto difunto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_difundo);

        setCurrentDateOnView();
        addListenerOnButton();

        btnRegistrarDifunto = (Button) findViewById(R.id.btnRegistrarDifunto);
        btnRegistrarDifunto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editNombre.getWindowToken(), 0);
                registrarDifunto();
            }
        });

        editNombre.requestFocus();

        if(getCurrentUser().getIdDifunto()!= 0){
            showProgress(true);
            loadDifuntosList();
        }
    }

    public void registrarDifunto() {

        // Reset errors.
        editNombre.setError(null);
        tvDateNacimiento.setError(null);
        tvDisplayDateDeceso.setError(null);
        editApellido.setError(null);

        // Store values at the time of the login attempt.
        String name = editNombre.getText().toString();
        String apellido = editApellido.getText().toString();
        String fechaNacimiento = tvDateNacimiento.getText().toString();
        String fechaDeceso = tvDisplayDateDeceso.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(apellido)) {
            editApellido.setError(getString(R.string.error_field_required));
            focusView = editApellido;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            editNombre.setError(getString(R.string.error_field_required));
            focusView = editNombre;
            cancel = true;
        }

        if (TextUtils.isEmpty(fechaNacimiento)) {
            tvDateNacimiento.setError(getString(R.string.error_field_required));
            focusView = tvDateNacimiento;
            cancel = true;
        }

        if (TextUtils.isEmpty(fechaDeceso)) {
            tvDisplayDateDeceso.setError(getString(R.string.error_field_required));
            focusView = tvDisplayDateDeceso;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            showProgress(false);
        } else {
            String[] feNa =fechaNacimiento.split("-");
            fechaNacimiento = feNa[2].replace(" ","")+"-"+feNa[1]+"-"+feNa[0];
            String[] feDe =fechaDeceso.split("-");
            fechaDeceso = feDe[2].replace(" ","")+"-"+feDe[1]+"-"+feDe[0];
            int idDifunto = 0;
            if(difunto != null){
                idDifunto = difunto.getIdDifunto();
            }

            registrarDifunto(getCurrentUser().getIdUsuario(),idDifunto, name, apellido, fechaNacimiento, fechaDeceso);
        }
    }

    public void registrarDifunto(final int idUsuario, final int idDifunto, final String nombre, final String apellido, final String fechaNacimiento, final String fechaDeceso){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME);

                    PropertyInfo fromProp0 = new PropertyInfo();
                    fromProp0.setName("idUsuario");
                    fromProp0.setValue(idUsuario);
                    fromProp0.setType(int.class);
                    request.addProperty(fromProp0);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("idDifunto");
                    fromProp1.setValue(idDifunto);
                    fromProp1.setType(int.class);
                    request.addProperty(fromProp1);

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
                    fromProp3.setName("fechaNacimiento");
                    fromProp3.setValue(fechaNacimiento);
                    fromProp3.setType(String.class);
                    request.addProperty(fromProp3);

                    PropertyInfo fromProp4 = new PropertyInfo();
                    fromProp4.setName("fechaDeceso");
                    fromProp4.setValue(fechaDeceso);
                    fromProp4.setType(String.class);
                    request.addProperty(fromProp4);

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

            boolean result = Boolean.valueOf(webResponse);
            if(result){
                btnRegistrarDifunto.setEnabled(false);

                Toast.makeText(RegistrarDifundoActivity.this, "Información registrada con exito!", Toast.LENGTH_LONG).show();
                login(getCurrentUser().getUserName(),getCurrentUser().getPassword());

            }else{
                Toast.makeText(RegistrarDifundoActivity.this, getString(R.string.error_server), Toast.LENGTH_LONG).show();
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

    public void setCurrentDateOnView() {

        tvDateNacimiento = (EditText) findViewById(R.id.tvDateNacimiento);
        tvDisplayDateDeceso = (EditText) findViewById(R.id.tvDateDeceso);
        editNombre = (EditText) findViewById(R.id.nombreDifunto);
        editApellido = (EditText) findViewById(R.id.apellidoDifunto);
        encabezado = (TextView) findViewById(R.id.encabezado);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        yearDe = c.get(Calendar.YEAR);
        monthDe = c.get(Calendar.MONTH);
        dayDe = c.get(Calendar.DAY_OF_MONTH);

        // set current date into textview
        tvDateNacimiento.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(day).append("-").append(month + 1).append("-")
                .append(year).append(" "));

        tvDisplayDateDeceso.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(day).append("-").append(month + 1).append("-")
                .append(year).append(" "));

    }

    public void addListenerOnButton() {

        btnChangeDateNacimiento = (Button) findViewById(R.id.btnChangeDateNacimiento);

        btnChangeDateNacimiento.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showDialog(DATE_DIALOG_ID_NACIMIENTO);

            }

        });

        btnChangeDateDeceso = (Button) findViewById(R.id.btnChangeDateDeceso);

        btnChangeDateDeceso.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showDialog(DATE_DIALOG_ID_DECESO);

            }

        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID_NACIMIENTO:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListenerNacimiento,
                        year, month,day);

            case DATE_DIALOG_ID_DECESO:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListenerDeceso,
                        yearDe, monthDe,dayDe);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListenerNacimiento
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into textview
            tvDateNacimiento.setText(new StringBuilder().append(day).append("-").append(month + 1)
                    .append("-").append(year)
                    .append(" "));
        }
    };

    private DatePickerDialog.OnDateSetListener datePickerListenerDeceso
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // set selected date into textview
            tvDisplayDateDeceso.setText(new StringBuilder().append(day).append("-").append(month + 1)
                    .append("-").append(year)
                    .append(" "));
        }
    };

    public void loadDifuntosList(){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_DIFUNTO_LIST);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idUsuario");
                    fromProp.setValue(getCurrentUser().getIdUsuario());
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponseDifunto = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIDifunto);
            }
        };

        thread.start();
    }

    final Runnable createUIDifunto = new Runnable() {

        public void run(){

            if(webResponseDifunto != null && !webResponseDifunto.equals("")){
                Type collectionType = new TypeToken<List<Difunto>>(){}.getType();
                List<Difunto> lcs = new Gson().fromJson( webResponseDifunto , collectionType);

                difunto = lcs.get(0);
                editNombre.setText(difunto.getNombre());
                editApellido.setText(difunto.getApellidos());

                String[] feNa =difunto.getFechaNacimiento().split("-");
                String[] dayL =feNa[2].split(" ");
                year = Integer.parseInt(feNa[0]);
                month = Integer.parseInt(feNa[1])-1;
                day = Integer.parseInt(dayL[0]);
                tvDateNacimiento.setText(dayL[0]+"-"+feNa[1]+"-"+feNa[0]);

                String[] feDe =difunto.getFechaDefuncion().split("-");
                String[] dayDeL =feDe[2].split(" ");
                yearDe = Integer.parseInt(feDe[0]);
                monthDe = Integer.parseInt(feDe[1])-1;
                dayDe = Integer.parseInt(dayDeL[0]);
                tvDisplayDateDeceso.setText(dayDeL[0]+"-"+feDe[1]+"-"+feDe[0]);
                btnRegistrarDifunto.setText("Actualizar");
                encabezado.setText("Actualizar Difunto");

                showProgress(false);
            }
            else{
                showProgress(false);
            }
        }
    };

    public void login(final String userName, final String password){
        thread = new Thread(){
            public void run(){
                try {
                    SoapObject request = new SoapObject(NAMESPACE_USER, METHOD_NAME_LOGIN);
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
                    String responseString = response.toString();

                    if(!responseString.equals("") && !responseString.equals("[]")) {
                        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
                        prefs.edit().putString("USER_DATA", responseString).apply();
                        user = null;
                    }

                    handler.post(createUILogin);

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        };

        thread.start();
    }

    final Runnable createUILogin = new Runnable() {

        public void run(){

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    showProgress(false);
                    Intent i = new Intent(RegistrarDifundoActivity.this, MainActivityAdmin.class);
                    finish();
                    finishAffinity();
                    startActivity(i);
                }
            }, 2000);
        }
    };
}

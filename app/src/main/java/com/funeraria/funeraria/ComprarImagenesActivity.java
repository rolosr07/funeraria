package com.funeraria.funeraria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomAdapter;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Difunto;
import com.funeraria.funeraria.common.entities.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;


public class ComprarImagenesActivity extends Base {

    private ImageView imgView;

    private String webResponse = "";
    private String webResponseUpload = "";
    private Thread thread;
    private Handler handler = new Handler();
    private Spinner spinner;

    private final String METHOD_NAME_GET_DIFUNTO_LIST = "getDifuntosPorUsuarioList";
    private final String METHOD_NAME_UPLOAD_IMAGEN = "registrarImagenDifunto";
    private String encodeImage = "";
    private String nombreImagen = "";
    private String typeImagen = "";

    private Usuario usuarioActual;

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_imagenes);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        imgView = (ImageView) findViewById(R.id.imgView);
        spinner = (Spinner) findViewById(R.id.spinner);

        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button buttonRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if(!encodeImage.equals("")){
                    Difunto dif = (Difunto)spinner.getSelectedItem();
                    showProgress(true);
                    uploadImagen(dif.getIdDifunto(),encodeImage,nombreImagen,typeImagen);
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            Type collectionType = new TypeToken<List<Usuario>>(){}.getType();
            List<Usuario> usuarios = new Gson().fromJson( prefs.getString("USER_DATA","") , collectionType);
            usuarioActual = usuarios.get(0);
        }

        showProgress(true);
        loadDifuntosList();
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

            if(!webResponse.equals("")){
                Type collectionType = new TypeToken<List<Difunto>>(){}.getType();
                List<Difunto> lcs = new Gson().fromJson( webResponse , collectionType);

                CustomAdapter adapter = new CustomAdapter(ComprarImagenesActivity.this, R.layout.simple_spinner_item,lcs);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        }
                );
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                int fileNameIndex = cursor.getColumnIndex(filePathColumn[1]);
                nombreImagen = cursor.getString(fileNameIndex);

                typeImagen= nombreImagen.replaceAll("^.*\\.", "");
                cursor.close();
            }

            Bitmap bmp = null;
            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imgView.setImageBitmap(bmp);
            imgView.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = null;

        if (parcelFileDescriptor != null) {
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        }

        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArrayImage = byteArrayOutputStream.toByteArray();
        encodeImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        if (parcelFileDescriptor != null) {
            parcelFileDescriptor.close();
        }
        return image;
    }

    public void uploadImagen(final int idDifunto, final String image, final String nombreImagen, final String tipoImagen){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_UPLOAD_IMAGEN);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("nombreImagen");
                    fromProp1.setValue(nombreImagen);
                    fromProp1.setType(String.class);
                    request.addProperty(fromProp1);

                    PropertyInfo fromProp2 = new PropertyInfo();
                    fromProp2.setName("imagen");
                    fromProp2.setValue(image);
                    fromProp2.setType(String.class);
                    request.addProperty(fromProp2);

                    PropertyInfo fromProp3 = new PropertyInfo();
                    fromProp3.setName("tipoImagen");
                    fromProp3.setValue(tipoImagen);
                    fromProp3.setType(String.class);
                    request.addProperty(fromProp3);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponseUpload = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIUpload);
            }
        };

        thread.start();
    }

    final Runnable createUIUpload = new Runnable() {

        public void run(){

            if(Boolean.valueOf(webResponseUpload)){
                imgView.setVisibility(View.GONE);
                Toast.makeText(ComprarImagenesActivity.this, "Imagen Registrada!", Toast.LENGTH_LONG).show();

                Intent i = new Intent(ComprarImagenesActivity.this, CompraExitoActivity.class);
                finish();
                startActivity(i);
            }else{
                showProgress(false);
                Toast.makeText(ComprarImagenesActivity.this, "No se registro la imagen!", Toast.LENGTH_LONG).show();
            }
        }
    };
}

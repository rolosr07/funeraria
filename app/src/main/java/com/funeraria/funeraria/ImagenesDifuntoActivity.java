package com.funeraria.funeraria;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.funeraria.funeraria.common.Adapters.CustomPagerAdapter;
import com.funeraria.funeraria.common.Base;
import com.funeraria.funeraria.common.entities.Imagen;
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
import java.text.Normalizer;
import java.util.List;


public class ImagenesDifuntoActivity extends Base {

    private TextView txNumeroImagenes;
    private ImageView imgView;
    private TextView nombre;

    private String webResponseImages = "";
    private String webResponseUpload = "";
    private String webResponseBorrar = "";
    private Thread thread;
    private Handler handler = new Handler();

    private ViewPager pagerImagenes;
    private List<Imagen> imagenesList;

    private final String METHOD_NAME_GET_IMAGENES_LIST = "getImagenesDifuntoList";
    private final String METHOD_NAME_UPLOAD_IMAGEN = "registrarImagenDifunto";
    private final String METHOD_NAME_BORRAR_IMAGEN = "borrarImagenDifunto";
    private String encodeImage = "";
    private String nombreImagen = "";
    private String typeImagen = "";

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagenes_difunto);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        txNumeroImagenes = (TextView)findViewById(R.id.txNumeroImagenes);
        imgView = (ImageView) findViewById(R.id.imgView);

        pagerImagenes = (ViewPager) findViewById(R.id.pagerImagenes);

        nombre = (TextView) findViewById(R.id.nombre);
        nombre.setText(getCurrentUser().getNombreDifunto());

        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button buttonRegistrar = (Button) findViewById(R.id.buttonRegistrar);
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if(!encodeImage.equals("")){

                    showProgress(true);
                    uploadImagen(getCurrentUser().getIdDifunto(),encodeImage,nombreImagen,typeImagen);
                }
            }
        });

        Button buttonBorrarImagen = (Button) findViewById(R.id.buttonBorrarImagen);
        buttonBorrarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Imagen imagen = imagenesList.get(pagerImagenes.getCurrentItem());
                showProgress(true);
                borrarImagen(getCurrentUser().getIdDifunto(),imagen.getIdImagenes());
            }
        });

        showProgress(true);
        loadImagenesList(getCurrentUser().getIdDifunto());
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

    public void loadImagenesList(final int idDifunto){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_GET_IMAGENES_LIST);
                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
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

        @SuppressLint("SetTextI18n")
        public void run(){

            if(!webResponseImages.equals("") && !webResponseImages.equals("[]")){
                Type collectionType = new TypeToken<List<Imagen>>(){}.getType();
                imagenesList = new Gson().fromJson( webResponseImages , collectionType);

                if(imagenesList.size() > 0){

                    txNumeroImagenes.setText("Cantidad de Imagenes: "+ imagenesList.size());
                    txNumeroImagenes.setVisibility(View.VISIBLE);

                    CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(getApplicationContext(), imagenesList);
                    pagerImagenes.setAdapter(mCustomPagerAdapter);
                    pagerImagenes.setVisibility(View.VISIBLE);
                    showProgress(false);

                }else{
                    txNumeroImagenes.setText("Cantidad de Imagenes: "+ 0);
                    pagerImagenes.setVisibility(View.GONE);
                    showProgress(false);
                }
            }else{
                showProgress(false);
                pagerImagenes.setVisibility(View.GONE);
                txNumeroImagenes.setText("Cantidad de Imagenes: "+0);
            }
        }
    };

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
                    String nombreImagenL = Normalizer.normalize(nombreImagen, Normalizer.Form.NFD);
                    nombreImagenL = nombreImagenL.replaceAll("[^\\p{ASCII}]", "");
                    fromProp1.setValue(nombreImagenL);
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
                loadImagenesList(getCurrentUser().getIdDifunto());
                imgView.setVisibility(View.GONE);
                Toast.makeText(ImagenesDifuntoActivity.this, "Imagen Registrada!", Toast.LENGTH_LONG).show();
            }else{
                showProgress(false);
                Toast.makeText(ImagenesDifuntoActivity.this, "No se registro la imagen!", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void borrarImagen(final int idDifunto, final int idImagen){
        thread = new Thread(){
            public void run(){
                try {

                    SoapObject request = new SoapObject(NAMESPACE_DIFUNTO, METHOD_NAME_BORRAR_IMAGEN);

                    PropertyInfo fromProp = new PropertyInfo();
                    fromProp.setName("idDifunto");
                    fromProp.setValue(idDifunto);
                    fromProp.setType(int.class);
                    request.addProperty(fromProp);

                    PropertyInfo fromProp1 = new PropertyInfo();
                    fromProp1.setName("idImagen");
                    fromProp1.setValue(idImagen);
                    fromProp1.setType(int.class);
                    request.addProperty(fromProp1);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_DIFUNTO);

                    androidHttpTransport.call(SOAP_ACTION_DIFUNTO, envelope);
                    Object response = envelope.getResponse();
                    webResponseBorrar = response.toString();

                }catch(Exception e){
                    e.printStackTrace();
                }
                handler.post(createUIBorrar);
            }
        };

        thread.start();
    }

    final Runnable createUIBorrar = new Runnable() {

        public void run(){

            if(Boolean.valueOf(webResponseBorrar)){
                loadImagenesList(getCurrentUser().getIdDifunto());
                imgView.setVisibility(View.GONE);
                Toast.makeText(ImagenesDifuntoActivity.this, "Imagen Borrada!", Toast.LENGTH_LONG).show();
            }else{
                showProgress(false);
                Toast.makeText(ImagenesDifuntoActivity.this, "No se pudo borrar la imagen!", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivityAdmin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finishAffinity();
        finish();
        startActivity(intent);
    }
}

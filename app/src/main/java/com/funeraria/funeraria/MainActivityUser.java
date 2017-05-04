package com.funeraria.funeraria;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivityUser extends Activity {

    private EditText edTextoBusqueda;
    private Button buttonBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_user);

        edTextoBusqueda = (EditText)findViewById(R.id.edTextoBusqueda);
        buttonBuscar = (Button) findViewById(R.id.buttonBuscar);
        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, BuscarDifuntoActivity.class);
                i.putExtra("textoBusqueda",edTextoBusqueda.getText().toString());
                startActivity(i);
            }
        });

        ////////////////////////////

        Button buttonComprarVelas = (Button) findViewById(R.id.buttonComprarVelas);
        buttonComprarVelas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarVelasActivity.class);
                startActivity(i);
            }
        });

        TextView tvComprarVelas = (TextView) findViewById(R.id.tvComprarVelas);
        tvComprarVelas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarVelasActivity.class);
                startActivity(i);
            }
        });

        //////////////////////////

        Button configButton = (Button) findViewById(R.id.configButton);
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ConfiguracionUsuarioActivity.class);
                startActivity(i);
            }
        });

        TextView configText = (TextView) findViewById(R.id.configText);
        configText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ConfiguracionUsuarioActivity.class);
                startActivity(i);
            }
        });

        //////////////////////////

        Button buttonComprarFlores = (Button) findViewById(R.id.buttonComprarFlores);
        buttonComprarFlores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarFloresActivity.class);
                startActivity(i);
            }
        });

        TextView tvComprarFlores = (TextView) findViewById(R.id.tvComprarFlores);
        tvComprarFlores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarFloresActivity.class);
                startActivity(i);
            }
        });

        //////////////////////////

        Button mensajesButton = (Button) findViewById(R.id.buttonMensajes);
        mensajesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarMensajesActivity.class);
                startActivity(i);
            }
        });

        TextView tvMensajes = (TextView) findViewById(R.id.tvMensajes);
        tvMensajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarMensajesActivity.class);
                startActivity(i);
            }
        });

        //////////////////////////

        Button imagenesButton = (Button) findViewById(R.id.imagenesButton);
        imagenesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarImagenesActivity.class);
                startActivity(i);
            }
        });

        TextView tvImagenes = (TextView) findViewById(R.id.tvImagenesDifunto);
        tvImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityUser.this, ComprarImagenesActivity.class);
                startActivity(i);
            }
        });

        Button btnShare = (Button) findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PackageManager pm=getPackageManager();
                try {

                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");
                    String text = "Te recomiendo que te bajes esta app\n" +
                            "Con la app memorial puedes enviar tus condolencias a tus seres queridos.\n" +
                            "Y el enlace para bajarla cuando la tengamos en el play store";

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.setPackage("com.whatsapp");

                    waIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(waIntent, "Share with"));

                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_user, menu);
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
}

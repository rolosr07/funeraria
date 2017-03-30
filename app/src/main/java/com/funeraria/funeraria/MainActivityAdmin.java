package com.funeraria.funeraria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivityAdmin extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        Button registrarDifuntoButton = (Button) findViewById(R.id.registrarDifuntoButton);
        registrarDifuntoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, RegistrarDifundoActivity.class);
                startActivity(i);
            }
        });

        TextView registrarDifuntoTextView = (TextView) findViewById(R.id.registrarDifuntoTextView);
        registrarDifuntoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, RegistrarDifundoActivity.class);
                startActivity(i);
            }
        });

        //////////////////////////

        Button configButton = (Button) findViewById(R.id.configButton);
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, ConfiguracionAdminActivity.class);
                startActivity(i);
            }
        });

        TextView configText = (TextView) findViewById(R.id.configText);
        configText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, ConfiguracionAdminActivity.class);
                startActivity(i);
            }
        });

        ///////////////////////////

        Button buttonImagenesDifunto = (Button) findViewById(R.id.buttonImagenesDifunto);
        buttonImagenesDifunto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, ImagenesDifuntoActivity.class);
                startActivity(i);
            }
        });

        TextView tvImagenesDifunto = (TextView) findViewById(R.id.tvImagenesDifunto);
        tvImagenesDifunto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, ImagenesDifuntoActivity.class);
                startActivity(i);
            }
        });

        ////////////////////////////

        Button buttonRegistrarPlaca = (Button) findViewById(R.id.buttonRegistrarPlaca);
        buttonRegistrarPlaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, PlacaActivity.class);
                startActivity(i);
            }
        });

        TextView tvRegistrarPlaca = (TextView) findViewById(R.id.tvRegistrarPlaca);
        tvRegistrarPlaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivityAdmin.this, PlacaActivity.class);
                startActivity(i);
            }
        });

        SharedPreferences prefs = getSharedPreferences("com.funeraria.funeraria", Context.MODE_PRIVATE);
        if(!prefs.getString("USER_DATA","").equals(""))
        {
            String[] userData = prefs.getString("USER_DATA","").split(",");
            TextView nameAdmin = (TextView) findViewById(R.id.nameAdmin);
            nameAdmin.setText(userData[0]+" "+userData[1]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.funeraria.funeraria;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.funeraria.funeraria.common.Base;


public class MainActivity extends Base {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button registrarDifuntoButton = (Button) findViewById(R.id.registrarDifuntoButton);
        registrarDifuntoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MainActivityUser.class);
                startActivity(i);
            }
        });

        TextView registrarDifuntoTextView = (TextView) findViewById(R.id.registrarDifuntoTextView);
        registrarDifuntoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MainActivityUser.class);
                startActivity(i);
            }
        });

        ////////////////////////////

        Button buttonVelasCompradas = (Button) findViewById(R.id.buttonVelasCompradas);
        buttonVelasCompradas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        TextView tvVelasCompradas = (TextView) findViewById(R.id.tvVelasCompradas);
        tvVelasCompradas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        ////////////////////////////

        Button buttonAutorizarUsuarios = (Button) findViewById(R.id.buttonAutorizarUsuarios);
        buttonAutorizarUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        TextView tvAutorizarUsuarios = (TextView) findViewById(R.id.tvAutorizarUsuarios);
        tvAutorizarUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}

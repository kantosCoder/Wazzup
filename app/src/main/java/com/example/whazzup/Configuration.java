package com.example.whazzup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class Configuration extends AppCompatActivity {
    //punto de entrada principal// pantalla configuracion // sergio
    //sergio mamon declara las variables al comienzo de la claaase
    private String ipAddress ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_activity);

        EditText IpText =(EditText)findViewById(R.id.DireccionIpText);
        EditText PuertoText = (EditText)findViewById(R.id.PuertoText);
        EditText UserText = (EditText)findViewById(R.id.usuarioText);

        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        //lo inicializas en el oncreate
        ipAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        IpText.setHint(ipAddress);

    }
    public void EnviarCliente(View vista){
        EditText IpText =(EditText)findViewById(R.id.DireccionIpText);
        EditText PuertoText = (EditText)findViewById(R.id.PuertoText);
        EditText UserText = (EditText)findViewById(R.id.usuarioText);
        if(IpText.getText().toString().isEmpty() || PuertoText.getText().toString().isEmpty()
            || UserText.getText().toString().isEmpty()){
            Snackbar.make(vista, "Rellena los campos necesarios", Snackbar.LENGTH_LONG)
                    .setAction("<--", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    })
                    .show();
        }else{
            Intent sendCliente = new Intent();
                sendCliente.setClass(getApplicationContext(),NetCode.class);
                sendCliente.putExtra("ip", IpText.getText().toString());
                sendCliente.putExtra("puerto",PuertoText.getText().toString());
                sendCliente.putExtra("username",UserText.getText().toString());
                sendCliente.putExtra("type","USER");
                startActivity(sendCliente);
        }
    }
    public void EnviarServidor(View vista){
        EditText IpText =(EditText)findViewById(R.id.DireccionIpText);
        EditText PuertoText = (EditText)findViewById(R.id.PuertoText);
        EditText UserText = (EditText)findViewById(R.id.usuarioText);

        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        //y no hace falta que lo vuelvas a inicializar XDDD simplemente pasa el valor

        if(PuertoText.getText().toString().isEmpty()|| UserText.getText().toString().isEmpty()){
            Snackbar.make(vista, "Rellena los campos", Snackbar.LENGTH_LONG)
                    .setAction("<--", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    })
                    .show();
        }else{
            Intent sendServer = new Intent();
            sendServer.setClass(getApplicationContext(),NetCode.class);
            sendServer.putExtra("ip",ipAddress); // HAY QUE ENVIAR LA IP DE TODOS MODOS, CARGAR CON FUNCION
            sendServer.putExtra("puerto",PuertoText.getText().toString());
            sendServer.putExtra("username",UserText.getText().toString());
            sendServer.putExtra("type","SERVER");
            startActivity(sendServer);
        }

    }
}

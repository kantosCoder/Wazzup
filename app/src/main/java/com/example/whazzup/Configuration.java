package com.example.whazzup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
//ACTIVIDAD DE CONFIGURACION
public class Configuration extends AppCompatActivity {
    //punto de entrada principal de la aplicación// pantalla configuracion // SERGIO
    private String ipAddress ="";
    //array de nombres de usuario
    private String[] coolnames1 = new String[20];
    private String namespace ="";
    private EditText UserText;
    private int rannum = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_activity);
        EditText IpText =(EditText)findViewById(R.id.DireccionIpText);
        EditText PuertoText = (EditText)findViewById(R.id.PuertoText);
        UserText = (EditText)findViewById(R.id.usuarioText);
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        ipAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        IpText.setText(ipAddress);
        //cargar array con nombres
        rollnames();
        rannum = (int) (Math.random()*19+0);
        namespace = coolnames1[rannum];
        rannum = (int) (Math.random()*19+0);
        UserText.setText(namespace+coolnames1[rannum]);

    }
    public void AcercaDe(View vista){
        final AlertDialog.Builder alert = new AlertDialog.Builder(Configuration.this);
        View mView =  getLayoutInflater().inflate(R.layout.acerca_dialog,null);
        final TextView AcercaTexto = (TextView) mView.findViewById(R.id.textViewAcerca);
        Button btn_ok = (Button)mView.findViewById(R.id.buttonOkay);
        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
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
    private void rollnames(){
        //nombres en los array
            coolnames1[0] = "Green";
            coolnames1[1] = "Dragon";
            coolnames1[2] = "Visor";
            coolnames1[3] = "Drop";
            coolnames1[4] = "Bass";
            coolnames1[5] = "Canon";
            coolnames1[6] = "Cool";
            coolnames1[7] = "Hippie";
            coolnames1[8] = "Dude";
            coolnames1[9] = "Moon";
            coolnames1[10] = "Can";
            coolnames1[11] = "Purple";
            coolnames1[12] = "Doctor";
            coolnames1[13] = "Student";
            coolnames1[14] = "Visor";
            coolnames1[15] = "Awake";
            coolnames1[16] = "Power";
            coolnames1[17] = "Mug";
            coolnames1[18] = "Extra";
            coolnames1[19] = "Ice";
    }
    public void randomizer(View vista){
        rannum = (int) (Math.random()*19+0);
        namespace = coolnames1[rannum];
        rannum = (int) (Math.random()*19+0);
        UserText.setText(namespace+coolnames1[rannum]);
    }
}

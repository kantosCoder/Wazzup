package com.example.whazzup;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Calendar;

//trabajar en lineas 51, 24,
//CLASE DE CONVERSACION
public class NetCode extends AppCompatActivity {

    //pantalla de chat// clases de conexion //hector
    //HI, I'm coding right here xD
    //bateria de variables:
    private String username ="";
    private String netport = "";
    private LinearLayout textlayout;
    private boolean goodtogo;
    private LinearLayout linearLayoutMensajes;
    private Calendar date;
    private Socket socket;
    private EditText editmessage; //<-definir en oncreate
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    //PUNTO DE ENTRADA
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_code);
        //OBTENCION DE VARIABLES PUTEXTRA
    }
    //NETWORK
    //obtencion de status de la conexion (si/no)
    private void getStatus(boolean status)
    {
        goodtogo=status;
        NetCode.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(goodtogo)
                    editmessage.setEnabled(true);
                else
                    editmessage.setEnabled(false);
            }
        });
    }
    //destruir conexion
    private void destroyNetwork(){
        //!\\definir con exactitud (eliminar finally)
        goodtogo=false;
        try {
            if(dataInputStream!=null) {dataInputStream.close();}
        }catch(Exception e){}
        finally {
            dataInputStream=null;
            try {
                if(dataOutputStream!=null) dataOutputStream.close();
            }catch(Exception e){}
            finally {
                dataOutputStream=null;
                try {
                    if(socket!=null) socket.close();
                }catch(Exception e){}
                finally {
                    socket=null;
                }
            }
        }
    }
    //LAYOUT
    //desplaza los mensajes
    private void messagePush(MessageViewer v)
    {
        textlayout.addView(v);//Añadimos el nuevo mensaje al linearlayput
        v.getParent().requestChildFocus(v, v);//PAra que se haga scroll hasta el nuevo mensaje añadido
    }
    //agregar el resto de metodos

}



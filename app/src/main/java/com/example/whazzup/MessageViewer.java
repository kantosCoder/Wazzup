package com.example.whazzup;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//CLASE DE CONTROL DE MENSAJES
public class MessageViewer extends LinearLayout
{
    //Enteros para indicar el tipo de mensaje
    //modificar/separar
    static int PROPIO=0, NOPROPIO=1;

    public MessageViewer(Context context)
    {
        super(context);

        // Creamos la interfaz a partir del layout
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li = (LayoutInflater)getContext().getSystemService(infService);
        li.inflate(R.layout.message_box, this, true);
    }

    public void setTexto(String txt)
    {
        TextView mensaje=(TextView) findViewById(R.id.MensajeTextoCentral);
        (mensaje).setText(txt);

        //Si el texto es vacío hacemos que no ocupe nada de tamaño
        if(txt.length()==0)
        {
            mensaje.setWidth(0); mensaje.setHeight(0);
        }
    }

    public void setHora(String txt)
    {
        ((TextView) findViewById(R.id.MensajeHora)).setText(txt);
    }

    public void setEnviado(int num)
    {
        int color;//Id del drawable para ponerle de fondo

        if(num==PROPIO)
        {
            SetLayoutNoneWeight(((TextView) findViewById(R.id.MensajeHuecoDerecha)));
            color= R.drawable.sent_message;

        }else if(num==NOPROPIO)
        {
            SetLayoutNoneWeight(((TextView) findViewById(R.id.MensajeHuecoIzquierda)));
            color= R.drawable.received_message;
        }else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((TextView) findViewById(R.id.MensajeTextoCentral)).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }

            ((TextView) findViewById(R.id.MensajeHora)).setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            color= R.drawable.message_infobox;
        }

        ((LinearLayout)findViewById(R.id.MensajeLinearLayout)).setBackgroundResource(color);
    }

    public void setImagen(Bitmap imagen)
    {
        ImageView msgimg=((ImageView) findViewById(R.id.MensajeImagen));

        if(imagen!=null)
            msgimg.setImageBitmap(imagen);
        else
        {
            msgimg.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        }
    }


    private void SetLayoutNoneWeight(TextView tv)
    {
        ViewGroup.LayoutParams lp=tv.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                lp.width, lp.height);
        params.weight = 0f;

        tv.setLayoutParams(params);
    }

}
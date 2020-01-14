package com.example.whazzup;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
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
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

//trabajar en lineas 51, 24, 118
//CLASE DE CONVERSACION
public class NetCode extends AppCompatActivity {

    //pantalla de chat// clases de conexion //hector
    //HI, I'm coding right here xD
    //bateria de variables:
    protected static String msgrole ="SELF";
    protected static String devicerole ="";
    private String ip= "";
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
    //hilos
    //Hilo de cliente
    ClientStartup ClientStart;
    //hilo de server
    ClientAwaitThread NewClients; //nuevos clientes
    //hilo de escucha (cliente/server)
    MessageRefresher HiloEscucha;


    //PUNTO DE ENTRADA
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //app solo en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_net_code);
        //OBTENCION DE VARIABLES PUTEXTRA (metodo de set.devicerole)
        //inicializacion de campos
        //inicializacion de cliente/servidor
        if(devicerole.equals("USER")){
            getStatus(false);
            (ClientStart = new ClientStartup()).start();//Abrimos el hilo para conectarnos al servidor
        }
        if(devicerole.equals("SERVER")){
            (NewClients=new ClientAwaitThread()).start();
        }
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
        //añade nuevo mensaje
        textlayout.addView(v);
        //desplaza
        v.getParent().requestChildFocus(v, v);
    }
    //agrega los mensajes (recibido, enviado)
    protected class newMessage extends Thread{

        private String messagetext="";
        private String role="";
        newMessage(String message) {
            messagetext = message;
        }
        //especifica si el mensaje es propio o extraño
        public void setRole(String currentrole){
            NetCode.msgrole = currentrole;
        }
        @Override
        public void run()
        {
            NetCode.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    //crear un ontrol REAL de mensaje propio o ajeno
                    MessageViewer vista = new MessageViewer(NetCode.this);
                    if(NetCode.msgrole.equals("SELF")) { //esto aun no hace nada, controlar correctamente
                        vista.sentMsg(MessageViewer.SELF);
                    }
                    else{
                        vista.sentMsg(MessageViewer.OTHER);
                    }
                    Calendar calendario = Calendar.getInstance();
                    vista.setDate(""+(String.format("%02d", (calendario.get(Calendar.HOUR_OF_DAY)))+":"+(String.format("%02d", (calendario.get(Calendar.MINUTE))))));
                    vista.setText(messagetext);
                    vista.setImage(null);
                    messagePush(vista);
                }
            });

        }
    }
    //HILOS DE EJECUCION
    //HILO DE ESCUCHA COMUN
    private class MessageRefresher extends Thread
    {
        public boolean executing;
        Socket socket;
        private String line;


        MessageRefresher(Socket s){socket=s;}

        public void run()
        {
            executing=true;

            while(executing)
            {
                line="";
                line=ObtenerCadena();//Obtenemos la cadena del buffer
                if(line!="" && line.length()!=0)//Comprobamos que esa cadena tenga contenido
                    ProcesarCadena();//Procesamos la cadena recibida
            }
        }

        private void ProcesarCadena()
        {
            String[] trozos=line.split(TipoMensaje.delimitador);//Dividimos la cadena para saber que tipo de mensaje es
            if(trozos.length>1)
            {
                //Log.d("Trozos", ""+trozos.length);
                switch (Integer.parseInt(trozos[0]))
                {
                    case TipoMensaje.NombreUser://Si es el nombre de usuario notificamos al hilo de espera de conexion que ya tenemos el nombre de usuario
                    {
                        HiloEspera.UserName=trozos[1];
                    }
                    break;

                    case TipoMensaje.MensajeNormal://Si es un mensaje normal, creamos un hilo para añadirlo a la interfaz
                    {
                        new ShowMessageReceived(trozos[1]).start();
                    }break;

                    case TipoMensaje.EnvioImagen://Si es un fichero, añadimos un hilo que maneje la recepcion del mismo
                    {
                        new ReceiveBitmapThread(Integer.parseInt(trozos[1])).run();
                    }break;

                    case TipoMensaje.Desconexion://Si el usuario se desconecta, mostrmoas en la pantalla que se ha desconectado y reinicimaos la conexión, para esperar nuevos usuarios
                    {
                        new ShowMessageInfo("Se ha desconectado "+trozos[1]+" :(").run();
                        ReiniciarConexion();

                    }break;

                    default:
                        break;
                }
            }
        }

        private String ObtenerCadena()
        {
            String cadena="";

            try {
                cadena=dataInputStream.readUTF();//Leemos del datainputStream una cadena UTF

            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return cadena;
        }
    }


    //Thread Kill
    private void killRefresher()
    {
        if(HiloEscucha!=null)
        {
            HiloEscucha.executing=false;
            HiloEscucha.interrupt();
            HiloEscucha=null;
        }
    }
    //HILO DE CLIENTE
    private class ClientStartup extends Thread
    {
        int respuesta;
        public void run()
        {
            changeTitle("Esperando Servidor...");
            respuesta=-1;
            try
            {
                new ShowMessageInfo("Creando el socket...").start();//Mostramos un mensaje para indicar que estamos creando el socket
                socket = new Socket(mIp, Integer.parseInt(mPuerto));//Creamos el socket


                new ShowMessageInfo("Conectando con el servidor: "+mIp+":"+mPuerto+"...").start();//Mostramos por la interfaz que nos hemos conectado al servidor

                (hearMessages=new GetMessagesThread(socket)).start();//Creamos el hilol de escucha de mensajes
                dataOutputStream= new DataOutputStream(socket.getOutputStream());//Iniciamos el dataoutputstream

                while(ServerName.length()<1){}//Esperamos a que el servidor nos envie su nombre

                //Añadimos un mensaje a la interfaz indicando que estamos pidiendo autorización en el servidor
                new ShowMessageInfo("Pidiendo autorización para entrar al servidor: '"+ServerName+"'").start();

                //Le enviamos al servidor nuestro nombre, para que pueda aceptarnos o rechazarnos jejeje
                SocketSendMessageThread sendNameUser;
                sendNameUser=new SocketSendMessageThread(TipoMensaje.NombreUser+TipoMensaje.delimitador+mNombreUsuario, true);
                sendNameUser.start();

                while(respuesta==-1){}//Esperamos la respuesta


                if(respuesta==1)
                {//Si nos han aceptado, mostramos un mensaje afirmativo, en caso contrario, el mensaje será negativo :(
                    changeTitle(ServerName);
                    new ShowMessageInfo("Has entrado al servidor: '"+ServerName+"', ya puedes hablar todo lo que quieras jeje").start();
                    setConnectionEstablislhed(true);
                }else
                {
                    changeTitle("RECHAZADO HIJOPUTA");
                    setConnectionEstablislhed(false);
                    new ShowMessageInfo("Te han denegado la entrada al servidor: '"+ServerName+"'").start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //HILO DE SERVER
    private class ClientAwaitThread extends Thread
    {
        public boolean esperando_nuevos_clientes;

        public int aceptar_conexion;
        String UserName;

        public void run()
        {
            changeTitle("Esperando Usuario...");
            esperando_nuevos_clientes=true;
            try
            {
                //Abrimos el socket
                serverSocket = new ServerSocket(Integer.parseInt(mPuerto));

                //Mostramos un mensaje para indicar que estamos esperando en la direccion ip y el puerto...
                new ShowMessageInfo("Creado el servidor\n Dirección: "+getIpAddress()+"\nPuerto: "+serverSocket.getLocalPort()).start();


                //Bucle para dejar al servidor a la escucha de clientes
                while (esperando_nuevos_clientes)
                {
                    //Creamos un socket que esta a la espera de una conexion de cliente
                    Socket socket = serverSocket.accept();


                    //Una vez hay conexion con un cliente, creamos los streams de salida/entrada
                    try {
                        dataInputStream = new DataInputStream(socket.getInputStream());
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    }catch(Exception e){ e.printStackTrace();}


                    //Iniciamos el hilo para la escucha y procesado de mensajes
                    (HiloEscucha=new GetMessagesThread(socket)).start();

                    //Creamos un mensaje en la interfaz indicando que hemos encontrado un nuevo usuario
                    new ShowMessageInfo("Se ha encontrado un nuevo usuario, esperando nombre de usuario...").start();

                    //Enviamos al usuario el nombre del servidor
                    SocketSendMessageThread sendNameServer;
                    sendNameServer=new SocketSendMessageThread(TipoMensaje.NombreServer+TipoMensaje.delimitador+mNombreUsuario, false);
                    sendNameServer.run();

                    UserName="";//Esperamos a que el usuario nos envie su nombre
                    while(UserName.length()<1){}

                    changeTitle(UserName);//Ponemos el nombre de usuario como titulo

                    aceptar_conexion=-1;

                    Server.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Crear_Dialogo_Aceptar_Usuario(UserName);
                        }
                    });

                    //Esperamos que el usuario acepte la conexion
                    while(aceptar_conexion==-1);


                    if(aceptar_conexion==1)
                    {
                        //Mostramos un mensaje indicando que hemos aceptado al usuario
                        new ShowMessageInfo("Has aceptado a '"+UserName+"', ya puedes hablar con él jeje").start();

                        //Notificamos al usuario que se ha aceptado la conexion
                        SocketSendMessageThread enviarmensaje;
                        enviarmensaje=new SocketSendMessageThread(TipoMensaje.RespuestaConexion+TipoMensaje.delimitador+"1", true);
                        enviarmensaje.run();

                        esperando_nuevos_clientes=false;
                        setConnectionEstablislhed(true);
                    }else
                    {
                        changeTitle(UserName+" rechazado");

                        //Mostramos un mensaje indicando que hemos rechazado al usuario
                        new ShowMessageInfo("Has rechazado a '"+UserName+"' :(\n Esperando nuevos usuarios...").start();

                        //Notificamos al usuario que se ha rechazado la conexion
                        SocketSendMessageThread enviarmensaje;
                        enviarmensaje=new SocketSendMessageThread(TipoMensaje.RespuestaConexion+TipoMensaje.delimitador+"0", true);
                        enviarmensaje.run();

                        setConnectionEstablislhed(false);

                        //Cerramos el hilo de escucha y los streams, porque cuando haya un nuevo usuario se volveran a crear
                        CerrarHiloEscucha();
                        SuperCloseSocketInputs();
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
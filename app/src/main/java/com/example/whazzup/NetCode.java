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

//trabajar en lineas 51, 24, 118, 214, 290 (changetitle), 244 (cerrar hilos),184(sin editar),392 obtener ip self de sergio
//CLASE DE CONVERSACION
public class NetCode extends AppCompatActivity {

    //pantalla de chat// clases de conexion //hector
    //HI, I'm coding right here xD
    //bateria de variables:
    ServerSocket serverSocket;
    protected static String msgrole ="SELF";
    protected static String devicerole ="";
    private String ip= "";
    private String username ="";
    private String servername = "";
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
    //si estamos conectados, activa el campo de texto a editar
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
    protected class ShowMessageInfo extends Thread//Añade a la interfaz un mensaje de información
    {
        private String msg;

        ShowMessageInfo(String message) {       msg = message;      }

        @Override
        public void run()
        {
            NetCode.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    MessageViewer vista = new MessageViewer(NetCode.this);
                    Calendar calendario = Calendar.getInstance();
                    vista.setDate(""+(String.format("%02d", (calendario.get(Calendar.HOUR_OF_DAY)))+":"+(String.format("%02d", (calendario.get(Calendar.MINUTE))))));
                    vista.setText(msg);
                    //vista.setHora("");
                    vista.sentMsg(-1);
                    vista.setImage(null);
                    messagePush(vista);
                }
            });

        }
    }
    private class SocketMessage extends Thread
    {
        private boolean mostrar_mensaje_enviado;
        private String msg;

        SocketMessage(String message, boolean show_msg)
        {
            msg=message;
            mostrar_mensaje_enviado=show_msg;
        }

        @Override
        public void run()
        {
            try
            {
                dataOutputStream.writeUTF(msg);//Enviamos el mensaje
                //dataOutputStream.close();
                if(mostrar_mensaje_enviado)
                {
                    String[] trozos=msg.split("#");
                    if(trozos.length>1)
                    {
                        switch (Integer.parseInt(trozos[0]))
                        {
                            case 3:
                            {
                                new newMessage(trozos[1]).start();//Añadimos el mensaje a la interfaz
                            }break;
                        }
                    }
                }
            }catch (IOException e)
            {
                e.printStackTrace();
                //message += "¡Algo fue mal! " + e.toString() + "\n";
            }
        }
    }
    //HILOS DE EJECUCION
    //HILO DE ESCUCHA COMUN
    private class MessageRefresher extends Thread
    {
        public boolean executing;
        Socket socket;
        private String buffer;


        MessageRefresher(Socket s){socket=s;}

        public void run()
        {
            executing=true;

            while(executing)
            {
                buffer="";
                buffer=ObtenerCadena();//Obtenemos la cadena del buffer
                if(buffer!="" && buffer.length()!=0)//Comprobamos que esa cadena tenga contenido
                    ProcessMessage();//Procesamos la cadena recibida
            }
        }

        private void ProcessMessage() {
            //procesar cadena, incluir metodos faltantes segun rol
            String[] trozos = buffer.split("#");//Dividimos la cadena para saber que tipo de mensaje es
            if (trozos.length > 1) {
                //Log.d("Trozos", ""+trozos.length);
                switch (Integer.parseInt(trozos[0])) {
                    case 0://Obtenemos el nombre del servidor
                    {
                        servername = trozos[1];
                    }
                    break;
                    case 1://Si es el nombre de usuario notificamos al hilo de espera de conexion que ya tenemos el nombre de usuario
                    {
                        if (devicerole.equals("SERVER")) {
                            NewClients.UserName = trozos[1];
                        }
                    }
                    break;
                    case 2://Obtenemos el nombre del servidor
                    {
                        if (devicerole.equals("CLIENT")) {
                            ClientStart.respuesta = Integer.parseInt(trozos[1]);//Notificamos al hilo de conexion la respuesta del servidor
                        }
                    }
                    break;
                    case 3://Si es un mensaje normal, creamos un hilo para añadirlo a la interfaz
                    {
                        new newMessage(trozos[1]).start();
                    }
                    break;
                    case 4://Si el usuario se desconecta, mostrmoas en la pantalla que se ha desconectado y reinicimaos la conexión, para esperar nuevos usuarios
                    {
                        if (devicerole.equals("SERVER")) {
                            new ShowMessageInfo("Se ha desconectado " + trozos[1] + " :(").run();//Añadimos el mensaje a la interfaz, que se ha desconectado

                            //Cerramos el hilo de escucha, así como los sockets y streams
                            //CerrarHiloEscucha();
                            //CloseSocketInputs();
                        }
                        if (devicerole.equals("CLIENT")) {
                            new ShowMessageInfo("Se ha desconectado " + trozos[1] + " :(").run();
                            //ReiniciarConexion();
                        }

                    }
                    break;
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
            //changeTitle("Esperando Servidor...");
            respuesta=-1;
            try
            {
                new ShowMessageInfo("Creando el socket...").start();//Mostramos un mensaje para indicar que estamos creando el socket
                socket = new Socket(ip, Integer.parseInt(netport));//Creamos el socket


                new ShowMessageInfo("Conectando con el servidor: "+ip+":"+netport+"...").start();//Mostramos por la interfaz que nos hemos conectado al servidor

                (HiloEscucha=new MessageRefresher(socket)).start();//Creamos el hilol de escucha de mensajes
                dataOutputStream= new DataOutputStream(socket.getOutputStream());//Iniciamos el dataoutputstream

                while(servername.length()<1){}//Esperamos a que el servidor nos envie su nombre

                //Añadimos un mensaje a la interfaz indicando que estamos pidiendo autorización en el servidor
                new ShowMessageInfo("Pidiendo autorización para entrar al servidor: '"+servername+"'").start();

                //Le enviamos al servidor nuestro nombre, para que pueda aceptarnos o rechazarnos jejeje
                SocketMessage sendNameUser;
                sendNameUser=new SocketMessage("1#"+username, true);
                sendNameUser.start();

                while(respuesta==-1){}//Esperamos la respuesta


                if(respuesta==1)
                {//Si nos han aceptado, mostramos un mensaje afirmativo, en caso contrario, el mensaje será negativo :(
                    //changeTitle(ServerName);
                    new ShowMessageInfo("Has entrado al servidor: '"+servername+"', ya puedes hablar todo lo que quieras").start();
                    getStatus(true);
                }else
                {
                    //changeTitle("RECHAZADO HIJOPUTA");
                    getStatus(false);
                    new ShowMessageInfo("Te han denegado la entrada al servidor: '"+servername+"'").start();
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
            //changeTitle("Esperando Usuario...");
            esperando_nuevos_clientes=true;
            try
            {
                //Abrimos el socket
                serverSocket = new ServerSocket(Integer.parseInt(netport));

                //Mostramos un mensaje para indicar que estamos esperando en la direccion ip y el puerto...
               //ESPERAR INTENT SERGIO!!! OBTENER SELF IP new ShowMessageInfo("Creado el servidor\n Dirección: "+getIpAddress()+"\nPuerto: "+serverSocket.getLocalPort()).start();


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
                    (HiloEscucha=new MessageRefresher(socket)).start();

                    //Creamos un mensaje en la interfaz indicando que hemos encontrado un nuevo usuario
                    new ShowMessageInfo("Se ha encontrado un nuevo usuario, esperando nombre de usuario...").start();

                    //Enviamos al usuario el nombre del servidor
                    SocketMessage sendNameServer;
                    sendNameServer=new SocketMessage("0#"+username, false);
                    sendNameServer.run();

                    UserName="";//Esperamos a que el usuario nos envie su nombre
                    while(UserName.length()<1){}

                    //changeTitle(UserName);//Ponemos el nombre de usuario como titulo

                        //Mostramos un mensaje indicando que hemos aceptado al usuario
                        new ShowMessageInfo("Has aceptado a '"+UserName+"', ya puedes hablar con él jeje").start();

                        //Notificamos al usuario que se ha aceptado la conexion
                        SocketMessage enviarmensaje;
                        enviarmensaje=new SocketMessage("2#1", true);
                        enviarmensaje.run();

                        esperando_nuevos_clientes=false;
                        getStatus(true);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
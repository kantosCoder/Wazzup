package com.example.whazzup;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
//!\\NETCODE STATUS NOTES
//si el cliente no encuentra ek server, explota
//
//ACTIVIDAD DE CHAT[VARIABLES~ENTRADA~LAYOUT~NETWORK/HILOS]

public class NetCode extends AppCompatActivity {
    //pantalla de chat// clases de conexion // HECTOR
    //>>>>>>VARIABLES:<<<<<<
    ServerSocket serverSocket;
    protected static String msgrole ="SELF";
    protected static String devicerole ="";
    private String ip= "";
    private String username ="";
    private String servername = "";
    private String netport = "";
    protected LinearLayout textlayout;
    private boolean goodtogo;
    private Calendar date;
    private Socket socket;
    private EditText editmessage;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Calendar calendario;
    //BATERÍA DE HILOS
    ClientStartup ClientStart;//Hilo de cliente
    ClientAwaitThread NewClients; //hilo de server
    MessageRefresher HiloEscucha;//hilo de escucha (cliente/server)

    //PUNTO DE ENTRADA DE LA ACTIVIDAD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //app solo en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_net_code);
        //OBTENCION DE VARIABLES PUTEXTRA
        devicerole = getIntent().getStringExtra("type");//ROL ACTUAL DE DISPOSITIVO
        ip = getIntent().getStringExtra("ip");
        netport = getIntent().getStringExtra("puerto");
        username = getIntent().getStringExtra("username");
        //inicializacion de campos
        editmessage = findViewById(R.id.messageInput);
        textlayout = (LinearLayout) findViewById(R.id.ListaMensajes);
        dataInputStream=null;
        dataOutputStream=null;
        socket = null;
        //inicializacion de cliente/servidor
        if(devicerole.equals("USER")){
            getStatus(false);
            (ClientStart = new ClientStartup()).start();
            servername = "";
        }
        if(devicerole.equals("SERVER")){
            serverSocket = null;
            (NewClients=new ClientAwaitThread()).start();
            servername = username;
            username ="";
        }
    }
    //>>>>>>LAYOUT<<<<<<
    //SI HAY CONEXION, ACTIVAMOS EL EDIT_TEXT
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
    //INTRODUCE LOS MENSAJES EN EL LAYOUT
    public void putMessage(View v)
    {
        if(goodtogo)
        {
            String msg=editmessage.getText().toString();//ALMACENAMOS EL MENSAJE
            if(msg!="" && msg.length()>0)
            {
                new SocketMessage("2#"+msg, true).start();//SE ENVIA EL MENSAJE
                editmessage.setText("");
            }
        }
    }
    //DESPLAZA LOS MENSAJES
    private void messagePush(MessageViewer v)
    {
        //añade nuevo mensaje
        textlayout.addView(v);
        //desplaza
        v.getParent().requestChildFocus(v, v);
    }
    //AGREGA LOS NUEVOS MENSAJES AL LAYOUT
    protected class newMessage extends Thread{
        private String messagetext="";
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
                    if(NetCode.msgrole.equals("SELF")) {
                        vista.sentMsg(0);
                    }
                    else{
                        vista.sentMsg(1);
                    }
                    calendario = Calendar.getInstance();
                    vista.setDate(""+(String.format("%02d", (calendario.get(Calendar.HOUR_OF_DAY)))+":"+(String.format("%02d", (calendario.get(Calendar.MINUTE))))));
                    vista.setMsgText(messagetext);
                    messagePush(vista);
                }
            });

        }
    }
    protected class ShowMessageInfo extends Thread//CONVERSACION//MENSAJE DE SISTEMA
    {
        private String msg;
        ShowMessageInfo(String message) {
            msg = message;
        }
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
                    vista.setMsgText(msg);
                    vista.sentMsg(-1);
                    messagePush(vista);
                }
            });

        }
    }
    //CLASE DE CONTROL DE MENSAJES [LAYOUT MESSAGEBOX]
    private class MessageViewer extends LinearLayout
    {
        //Enteros para indicar el tipo de mensaje
        //modificar/separar
        int SELF=0;
        int OTHER=1;

        public MessageViewer(Context context)
        {
            super(context);

            // Creamos la interfaz a partir del layout
            String infService = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li;
            li = (LayoutInflater)getContext().getSystemService(infService);
            li.inflate(R.layout.message_box, this, true);
        }

        public void setMsgText(String txt)
        {
            TextView mensaje=(TextView) findViewById(R.id.mainbox);
            (mensaje).setText(txt);
            if(txt.length()==0)
            {
                mensaje.setWidth(0); mensaje.setHeight(0);
            }
        }

        public void setDate(String txt)
        {
            ((TextView) findViewById(R.id.msgDate)).setText(txt);
        }

        public void sentMsg(int typeofmsg)
        {
            int color;//Id del drawable para ponerle de fondo
            TextView msg = findViewById(R.id.username);
            TextView box = findViewById(R.id.mainbox);
            if(typeofmsg==SELF)//SI EL MENSAJE A MOSTRAR ES NUESTRO
            {
                //elimina cabecera de usuario (ocultar)
                LayoutParams params = (LayoutParams) msg.getLayoutParams(); //parametros del textview
                params.height = 1; //altura del textview
                msg.setLayoutParams(params); //se establecen los parametros
                box.setTextSize(18);
                ((TextView) findViewById(R.id.username)).setText(null);
                layoutMove(((TextView) findViewById(R.id.rigthSpacer)));
                color= R.drawable.sent_message;
            }
            else if(typeofmsg==OTHER)//SI EL MENSAJE A MOSTRAR NO ES NUESTRO
            {
                //reestablece cabecera de usuario
                msg.setTextColor(getResources().getColor(R.color.user));
                LayoutParams params = (LayoutParams) msg.getLayoutParams();
                params.height = 42;
                box.setTextSize(18);
                if(devicerole.equals("USER")){
                    ((TextView) findViewById(R.id.username)).setText(servername);
                }
                if(devicerole.equals("SERVER")){
                    ((TextView) findViewById(R.id.username)).setText(username);
                }
                layoutMove(((TextView) findViewById(R.id.leftSpacer)));
                color= R.drawable.received_message;
            }else
            {
                //reestablece cabecera de usuario
                msg.setTextColor(getResources().getColor(R.color.system));
                box.setTextSize(15);
                LayoutParams params = (LayoutParams) msg.getLayoutParams();
                params.height = 41;
                ((TextView) findViewById(R.id.username)).setText("Sistema");
                ((TextView) findViewById(R.id.msgDate)).setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                color= R.drawable.message_infobox;
            }

            ((LinearLayout)findViewById(R.id.messageLayout)).setBackgroundResource(color);
        }
        private void layoutMove(TextView tv)//Mueve los mensajes a izquierda/derecha
        {
            ViewGroup.LayoutParams lp=tv.getLayoutParams();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(lp.width, lp.height);
            params.weight = 0f;
            tv.setLayoutParams(params);
        }

    }
    //>>>>>>NETWORK/HILOS<<<<<<
    //destruir conexión, notificar, limpiar, volver al menú (ESPECIFICAR EN ONDESTROY)
    private void destroyNetwork(){
        //Limpiar sockets
        goodtogo=false;
        try {
            if(dataInputStream!=null) {
                dataInputStream.close();
                }
        }
        catch(Exception e){}
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        }
        catch(Exception e){}
        try {
            if(socket!=null)  {
                socket.close();
            }
        }
        catch(Exception e){}
                dataOutputStream=null;
                socket=null;
                dataInputStream=null;
        //THREAD KILL (POR HACER)
        if(HiloEscucha!=null)
        {
            HiloEscucha.engine=false;
            HiloEscucha.interrupt();
            HiloEscucha=null;
        }
    }
    //ENVIO DE MENSAJES POR RED
    private class SocketMessage extends Thread
    {
        private boolean abletoshow; //SE ESPECIFICA SI SE MUESTRA O NO, POR SI SE MANDA UN MENSAJE DE SALIDA
        private String msg;

        SocketMessage(String message, boolean show_msg)
        {
            msg=message;
            abletoshow=show_msg;
        }

        @Override
        public void run()
        {
            try
            {
                dataOutputStream.writeUTF(msg);//ENVIO DE MENSAJE POR RED
                if(abletoshow)
                {
                    String[] trozos=msg.split("#");
                    if(trozos.length>1 && Integer.parseInt(trozos[0])==2)
                    {
                        msgrole = "SELF"; //El mensaje que mandamos se va a pasar a pantalla
                        new newMessage(trozos[1]).start();//MENSAJE A LAYOUT
                    }
                }
            }catch (IOException e)
            {
                //Punto de debug
                //MENSAJE DE ERROR EN LA INTERFAZ? por hacer...
            }
        }
    }
    //HILOS DE DE EJECUCION
    //HILO DE ESCUCHA COMUN
    private class MessageRefresher extends Thread
    {
        public boolean engine;
        Socket socket;
        private String buffer;

        MessageRefresher(Socket s){socket=s;}

        public void run()
        {
            engine=true;
            //añadido
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());//Creamos el inputstream
            }catch(Exception e){
                //Punto de debug
            }
            while(engine)
            {
                buffer="";
                buffer=getMsgString();//preparar string para split
                if(buffer!="" && buffer.length()!=0) {//el string no está vacio
                    ProcessMessage();//procesar string de buffer de entrada
                }
            }
        }
        private void ProcessMessage() {
            //obtener y tratar buffer de mensaje
            String[] splitter = buffer.split("#");//dividir el mensaje (cabecera/mensaje)
            if (splitter.length > 1) {
                switch (Integer.parseInt(splitter[0])) {
                    case 0://nameserver
                    {
                        if (devicerole.equals("USER")) {
                            servername = splitter[1];
                        }
                    }
                    break;
                    case 1://username
                    {
                        if (devicerole.equals("SERVER")) {
                            NewClients.UserName = splitter[1];
                        }
                    }
                    break;
                    case 2://normalmessage
                    {
                        msgrole = "OTHER";
                        new newMessage(splitter[1]).start();
                    }
                    break;
                    case 3://discon. message
                    {
                        if (devicerole.equals("SERVER")) {
                            new ShowMessageInfo("Se ha desconectado " + splitter[1] + " :(").run();
                        }
                        if (devicerole.equals("CLIENT")) {
                            new ShowMessageInfo("Se ha desconectado " + splitter[1] + " :(").run();
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        }
        private String getMsgString()
        {
            String buffer="";
            try {
                buffer=dataInputStream.readUTF();//Leemos del datainputStream una cadena UTF
            }catch(Exception e)
            {
                //Punto de debug
            }
            return buffer;
        }
    }
    //HILO DE CLIENTE
    private class ClientStartup extends Thread
    {
        public void run()
        {
            try
            {
                socket = new Socket(ip, Integer.parseInt(netport));//Preparacion de socket
                (HiloEscucha=new MessageRefresher(socket)).start();//Hilo de entrada de mensajes
                new ShowMessageInfo("Conectando a: "+ip+""+netport).start();//Conexion exitosa (mensaje a layout)
                dataOutputStream= new DataOutputStream(socket.getOutputStream());//preparar dataoutput
                while(servername.length()<1){}//OBTENIENDO NOMBRE DEL SERVER / MOMENTO CRITICO, PODRÍA PARARSE
                SocketMessage sendusername;
                sendusername=new SocketMessage("1#"+username, true);
                sendusername.start();
                new ShowMessageInfo("Conexion establecida con: \n '"+servername+"'").start(); //Mensaje de confirmacion a interfaz
                getStatus(true); //se habilita la escritura
            } catch (IOException e) {
                new ShowMessageInfo("No se ha podido conectar a "+ip+"").start();
                getStatus(false);
                //INSERTAR METODO NETWORKDESTROY...
            }
        }
    }
    //HILO DE SERVER
    private class ClientAwaitThread extends Thread
    {
        public boolean waiting;
        String UserName = "";
        public void run()
        {
            waiting=true;
            try
            {
                new ShowMessageInfo("Servidor iniciado...").start();//El server escucha correctamente
                serverSocket = new ServerSocket(Integer.parseInt(netport)); //socket de server en el puerto especificado
                while (waiting)
                {
                    Socket socket = serverSocket.accept();//Streams
                    try {
                        dataInputStream = new DataInputStream(socket.getInputStream());
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    }catch(Exception e){
                        //Punto de debug...
                        }
                    (HiloEscucha=new MessageRefresher(socket)).start();//escucha de mensajes
                    new ShowMessageInfo("Nueva conexión entrante...").start();//intento de entrada de usuario
                    SocketMessage sendservername; //envío de nombre de server a cliente
                    sendservername=new SocketMessage("0#"+servername, false);
                    sendservername.run();
                    while(UserName.length()<1){}//OBTENIENDO NOMBRE DEL SERVER / MOMENTO CRITICO, PODRÍA PARARSE
                    new ShowMessageInfo("'"+UserName+"' ha entrado al chat").start(); //El usuario ha establecido conexión
                    username = UserName; //Carga de variable local a global para tratar el nombre en la interfaz
                    //Notificamos al usuario que se ha aceptado la conexion
                    waiting=false;
                    getStatus(true);
                }
            }
            catch (IOException e)
            {
                //Punto de debug
            }
        }
    }
}
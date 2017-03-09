import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client{

    private String ip_server= null; // "192.168.133.1"
    private Socket server = null;
    private String user = null;
    private String passwd = null;
    private PrintWriter stream_out = null;
    private Scanner stream_in = null;
    private int port;
    private boolean connected = false;

    public static void main(String[] args){
        Client client = new Client("Dieter", "Klopp", "192.168.133.1");
        if (client.connect()){
            client.receive(System.out);
            client.send("Test Nachricht");
        }else{
            System.out.println("Markus wars!!!");
        }
    }

    public Client(String user, String passwd, String ip_server){
        this(user, passwd, ip_server, 6697);
    }

    public Client(String user, String passwd, String ip_server, int port){
        this.ip_server = ip_server;
        this.user = user;
        this.passwd = passwd;
        this.port = port;
    }

    public boolean disconnect(){
        if (connected) {
            try {
                this.stream_in.close();
                this.stream_out.close();
            } catch (Exception e) {
                return false;
            }
            this.connected = false;
        }
        return true;
    }

    public boolean is_connected(){
        return this.connected;
    }

    public boolean connect(){
        try {
            this.server = new Socket(ip_server, port);
            this.stream_out = new PrintWriter(server.getOutputStream(), true);
            this.stream_in = new Scanner(server.getInputStream());
            this.connected = true;
        } catch (IOException e) {
            System.out.print("Connection failed");
            return false;
        }
        // Send/Receive Handshake
        System.out.print("Connection established");
        return true;
    }

    public void send(final String message){
        // wenn verbunden, dann vom Input in outputServer schreiben
        if (this.is_connected()){
            // get Message
            String  msg = message;
            // send
            this.stream_out.write(msg);
        }
    }

    public void receive(final OutputStream out){
        final Client client = this;

        Thread in = new Thread(){
            public void run(){
                while (client.is_connected()){
                    String raw_message = client.stream_in.next();
                    String msg = raw_message;
                    
                    try {
                        out.write(msg.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        in.start();
    }

}

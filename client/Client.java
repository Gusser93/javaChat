package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import tools.IrcParser;
import tools.Message;

public class Client{

    private String serverIp= null; // "192.168.133.1"
    public Socket socket = null;
    public String user = null;
    public String nickname = null;
    public String passwd = null;
    public PrintWriter stream_out = null;
    public Scanner stream_in = null;
    private int port;
    private boolean connected = false;

    public static void main(String[] args){
        Client client = new Client("Dieter", "Klopp", "Nick", "192.168.133.1");
        if (client.connect()){
            client.receive(System.out);
            client.bcast("Test Nachricht");
        }else{
            System.out.println("Markus wars!!!");
        }
    }

    public Client(String user, String passwd, String nickname, Socket socket) throws IOException {
        this.user = user;
        this.nickname = nickname;
        this.passwd = passwd;
        this.socket = socket;

        this.stream_out = new PrintWriter(this.socket.getOutputStream(), true);
        this.stream_in = new Scanner(this.socket.getInputStream());
        this.stream_in.useDelimiter(IrcParser.CRLF);
        this.connected = true;
    }

    public Client(String user, String passwd, String nickname, String serverIp){
        this(user, passwd, nickname, serverIp, 6697);
    }

    public Client(String user, String passwd, String nickname, String serverIp, int port){
        this.serverIp = serverIp;
        this.user = user;
        this.nickname = nickname;
        this.passwd = passwd;
        this.port = port;
    }

    public boolean disconnect(){
        if (connected) {
            try {
                this.stream_in.close();
                this.stream_out.close();
                this.socket.close();
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
            this.socket = new Socket(serverIp, port);
            this.stream_out = new PrintWriter(this.socket.getOutputStream(), true);
            this.stream_in = new Scanner(this.socket.getInputStream());
            this.stream_in.useDelimiter(IrcParser.CRLF);

            // send Handshake
            this.connected = true;
            // send password
            Message msg = Message.sendPassword(this.passwd);
            this.stream_out.write(msg.toString());
            // send nickname
            msg = Message.sendPassword(this.nickname);
            this.stream_out.write(msg.toString());
            // send username
            msg = Message.sendPassword(this.user);
            this.stream_out.write(msg.toString());

        } catch (IOException e) {
            System.out.print("Connection failed");
            return false;
        }
        // Send/Receive Handshake
        System.out.print("Connection established");
        return true;
    }

    public void bcast(final String message) {
        this.send(message, Message.AT_ALL);
    }

    public void send(final String message, final String target){
        // wenn verbunden, dann vom Input in outputServer schreiben
        if (this.is_connected()){
            // get Message
            Message msg = Message.sendPrivateMessage(target, message);
            // send
            this.stream_out.write(msg.toString());
        }
    }

    public void receive(final OutputStream out){
        final Client client = this;
        PrintWriter writer = new PrintWriter(out);

        Thread in = new Thread(){
            public void run(){
                while (client.is_connected()){
                    String raw_message = client.stream_in.next();
                    Message msg = new Message(raw_message);
                    writer.write(msg.toString());
                }
            }
        };
        in.start();
    }

}

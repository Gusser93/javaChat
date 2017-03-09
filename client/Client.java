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

    public static void main(String[] args) throws Exception {
        Client client = new Client("Dieter", "SuperSecret", "Nick", "192.168.133.96");
        if (client.connect()){
            client.receive(System.out);
            client.bcast("Test Nachricht");
            System.out.println("Inside");
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
            System.out.println("####### Handshake #######");
            // send password
            Message msg = Message.sendPassword(this.passwd);
            this.stream_out.write(msg.toString());
            this.stream_out.flush();
            System.out.println("Send Password");

            // send nickname
            msg = Message.sendNickname(this.nickname);
            this.stream_out.write(msg.toString());
            this.stream_out.flush();
            System.out.println("Send Nickname");

            // send username
            msg = Message.sendUser(this.user, this.user, IrcParser.Mode.NONE);
            this.stream_out.write(msg.toString());
            this.stream_out.flush();
            System.out.println("Send User");

            // finished Handshake
            this.connected = true;
            System.out.println("####### Connection established #######");

        } catch (IOException e) {
        	e.printStackTrace();
            System.out.println("Connection failed");
            return false;
        }

        return true;
    }

    public void bcast(final String message) {
        this.send(message, Message.AT_ALL);
    }

    public void send(final String message, final String target){
        if (this.is_connected()){
            // Send message
            Message msg = Message.sendPrivateMessage(target, message);
            this.stream_out.write(msg.toString());
            this.stream_out.flush();
        }
    }

    public void receive(final OutputStream out){
        final Client client = this;
        PrintWriter writer = new PrintWriter(out);

        Thread in = new Thread(){
            public void run(){
                while (client.is_connected() && client.stream_in.hasNext()){
                    String raw_message = client.stream_in.next();
                    Message msg = new Message(raw_message);
                    writer.write(msg.toString());
                }
            }
        };
        in.start();
    }

}

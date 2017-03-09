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
    private PrintWriter output = null;

    public static void main(String[] args) throws Exception {
        Client client = new Client("Dieter", "SuperSecret", "Nick", "192.168.133.96", System.out);
        if (client.connect()){
            client.bcast("Test Nachricht");
            client.disconnect();
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

    public Client(String user, String passwd, String nickname, String serverIp, OutputStream output){
        this(user, passwd, nickname, serverIp, 6697, output);
    }

    public Client(String user, String passwd, String nickname, String serverIp, int port, OutputStream output){
        this.serverIp = serverIp;
        this.user = user;
        this.nickname = nickname;
        this.passwd = passwd;
        this.port = port;
        this.output = new PrintWriter(output);
    }

    public boolean disconnect(){
        if (connected) {
            try {
                // close connection
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

            this.receive();
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

    public void receive(){
        final Client client = this;

        Thread in = new Thread(){
            public void run(){
                while (client.is_connected()){
                    // receive message and write to output
                    String raw_message = client.stream_in.next();
                    Message msg = new Message(raw_message);
                    client.output.println(msg.toString());
                    client.output.flush();
                }
            }
        };
        in.start();
    }

}

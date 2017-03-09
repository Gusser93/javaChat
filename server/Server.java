package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import tools.IrcParser;
import tools.Message;

public class Server{

    ArrayList<Socket> clients = null;
    private int port;
    private ServerSocket socket = null;
    private boolean connected = false;

    public Server(int port) throws IOException {
        this.port = port;
        this.socket = new ServerSocket(port);
        this.connected = true;
    }

    public void accept_connections(){
        final  Server server = this;
        Thread t = new Thread(){
            public void run() {
                while (server.is_connected()){
                    try {
                        Socket client = server.socket.accept();
                        server.clients.add(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    public boolean is_connected(){
        return this.connected;
    }

    public void receive(final Socket client, final OutputStream out){
    	PrintWriter printer = new PrintWriter(out);
        final Server server = this;
        Thread t = new Thread() {
            public void run() {
                try {
                    Scanner stream_in = new Scanner(client.getInputStream());
                    stream_in.useDelimiter(IrcParser.CRLF);
                    while (server.is_connected()) {
                        String raw_message = stream_in.next();
                        Message msg = new Message(raw_message);
                        printer.print(msg.toString());
                        //out.write(msg.toString().getBytes());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void send(final Socket client, String message) {
        final Server server = this;
        Thread t = new Thread(){
            public void run() {
                try {
                    if (server.is_connected()){
                        PrintWriter stream_out = new PrintWriter(client.getOutputStream(), true);
                        //String msg = message;
                        Message msg = Message.sendPrivateMessage("<target>", message);
                        stream_out.write(msg.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(6679);
        server.accept_connections();
        OutputStream output = new OutputStream();
        while (true) {

        }
    }

}

package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import tools.IrcParser;
import tools.Message;
import client.Client;
import java.util.HashMap;

public class Server{
    HashMap<String, Client> clients = new HashMap<String, Client>();
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
                        Socket socket = server.socket.accept();
                        // Perform Handshake
                        server.add_client(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    public void add_client(Socket socket) throws IOException{
        // perform Handshake
        String username = "";
        String passwd = "";

        Client client = new Client(username, passwd, socket);
        this.clients.put(username, client);
        this.receive(client);
    }

    public boolean is_connected(){
        return this.connected;
    }

    public void receive(final Client client){
        final Server server = this;
        Thread t = new Thread() {
            public void run() {
                    while (server.is_connected()) {
                        String raw_message = client.stream_in.next();
                        Message msg = new Message(raw_message);
                        server.process_message(msg);
                    }
            }
        };
        t.start();
    }

    public void process_message(Message message) {
        String target = message.getTarget();
        if (target.equals(Message.AT_ALL)) {
            for (Client client : this.clients.values()) {
                this.send(client, message.getBody());
            }
        } else {
            this.send(this.clients.get(target), message.getBody());
        }
    }

    public void send(final Client client, final String message) {
        final Server server = this;
        Thread t = new Thread(){
            public void run() {
                  if (server.is_connected()){
                      String username = client.user;
                      Message msg = Message.sendPrivateMessage(username, message);
                      client.stream_out.write(msg.toString());
                  }
            }
        };
        t.start();
    }

    public static void main(String[] args) throws Exception {

        Server server = new Server(6679);
        server.accept_connections();
    }

}

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
    private boolean running = false;

    public Server(int port) throws IOException {
        this.port = port;
        this.socket = new ServerSocket(port);
        this.running = true;
    }

    public void accept_connections(){
        while (this.is_running()){
            try {
                Socket socket = this.socket.accept();
                // Perform Handshake
                this.establish_client_connection(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void establish_client_connection(Socket socket) throws IOException{
        // perform Handshake
        String username = "";
        String nickname = "";
        String passwd = "";
        
        Client client = new Client(username, passwd, nickname, socket);

        // block until we received our Handshake
        //Scanner stream_in = new Scanner(socket.getInputStream());
        //stream_in.useDelimiter(IrcParser.CRLF);

        // password
        String raw_message = client.stream_in.next();
        System.out.println(raw_message);
        Message msg = new Message(raw_message);
        client.passwd = msg.params.get(0);

        // nickname
        raw_message = client.stream_in.next();
        System.out.println(raw_message);
        msg = new Message(raw_message);
        client.nickname = msg.params.get(0);

        // username
        raw_message = client.stream_in.next();
        System.out.println(raw_message);
        msg = new Message(raw_message);
        client.user = msg.params.get(0);

        // create Client after Handshake
        this.clients.put(username, client);
        this.receive(client);
    }

    public boolean is_running() {
        return this.running;
    }

    public void stop() throws IOException {
        for (Client client : this.clients.values()) {
            client.disconnect();
        }
        this.socket.close();
    }

    public void receive(final Client client){
        final Server server = this;
        Thread t = new Thread() {
            public void run() {
                  while (server.is_running() ){//&& client.stream_in.hasNext()) {
                      String raw_message = client.stream_in.next();
                      Message msg = new Message(raw_message);
                      server.process_message(client, msg);
                      System.out.println(msg.toString());
                  }
            }
        };
        t.start();
    }

    public void process_message(Client client, Message message) {
        String target = message.getTarget();
        if (target.equals(Message.AT_ALL)) {
            for (Client cl : this.clients.values()) {
                //if (!cl.equals(client)) {
                    this.send(cl, message.getBody());
                //}
            }
        } else {
            this.send(this.clients.get(target), message.getBody());
        }
    }

    public void send(final Client client, final String message) {
        final Server server = this;
        Thread t = new Thread(){
            public void run() {
                  if (server.is_running()){
                      String username = client.user;
                      Message msg = Message.sendPrivateMessage(username, message);
                      System.out.println(msg);
                      System.out.println(msg.toString().endsWith(IrcParser.CRLF));
                      client.stream_out.print(msg.toString());
                      client.stream_out.flush();
                  }
            }
        };
        t.start();
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(6697);
        server.accept_connections();
    }

}

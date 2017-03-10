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
import database.MySqlConnector;

import java.util.HashMap;

public class Server{
    HashMap<String, Client> clients = new HashMap<String, Client>();
    private int port;
    private ServerSocket socket = null;
    private boolean running = false;
    private MySqlConnector database;
    private String serverName = "ChatServer";

    public Server(int port) throws IOException {
        this.port = port;
        this.socket = new ServerSocket(port);
        this.running = true;
        this.database = new MySqlConnector();
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
        Client client = new Client("", "", "",  socket);

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
        
        System.out.println(client.user + " " + client.nickname + " " + client.passwd);
        
        if(this.checkUserExists(client.user))
        	if(this.checkCorrectUser(client.user, client.passwd))
        		welcomeUser(client);
        	else
        		rejectUser(client);
        else {
        	this.createUser(client.user, client.passwd);
        	this.welcomeUser(client);
        }
    }
    
    private void welcomeUser(Client client) {
        // create Client after Handshake
        this.clients.put(client.user, client);
        serverBcast(Message.sendBroadcastMessage(client.user + " entered chat."));
        send(client, Message.sendWelcome(client.nickname, client.user, "", serverName)); 
        this.receive(client);	
    }
    
    private void rejectUser(Client client) {
    	send(client.stream_out, Message.sendReject(client.nickname, serverName));
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
    
    private void bcast(Client source, Message message) {
    	for (Client destination : this.clients.values()) {
            if (!destination.equals(source)) {
                this.send(source, destination, message.getBody());
            }
        }
    }
    
    private void serverBcast(Message message) {
    	for (Client destination : this.clients.values()) {
            this.send(this.serverName, destination, message.getBody());
        }
    }

    public void process_message(Client source, Message message) {
        // get target of message
        String target = message.getTarget();

        if (target.equals(Message.AT_ALL)) {
            // Broadcast message
            bcast(source, message);
        } else {
            // send message to specific client
            this.send(source, this.clients.get(target), message.getBody());
        }
    }

    public void send(final Client src, final Client dst, final String message) {
       send(dst, Message.sendPrivateMessageFromServer(src.user, dst.user, message));
    }

    public void send(final String src, final Client dst, final String message) {
       send(dst, Message.sendPrivateMessageFromServer(src, dst.user, message));
    }

    public void send(final Client dst, final Message message) {
        final Server server = this;
        Thread t = new Thread(){
            public void run() {
                  if (server.is_running()){
                      dst.stream_out.print(message.toString());
                      dst.stream_out.flush();
                  }
            }
        };
        t.start();
    }

    public void send(final PrintWriter dst, final Message message) {
        final Server server = this;
        Thread t = new Thread(){
            public void run() {
                  if (server.is_running()){
                      dst.print(message.toString());
                      dst.flush();
                  }
            }
        };
        t.start();
    }
    
    public boolean checkUserExists(String username) {
    	return database.checkUserExists(username);
    }
    
    public boolean createUser(String username, String password) {
    	return database.registerUser(username, password);
    }
    
    public boolean checkCorrectUser(String username, String password) {
    	return database.checkCorrectLogin(username, password);
    }

    public static void main(String[] args) throws Exception {
        // start server
        Server server = new Server(6697);
        server.accept_connections();
    }

}

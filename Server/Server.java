import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

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
        final Server server = this;
        Thread t = new Thread() {
            public void run() {
                Scanner stream_in = null;
                try {
                    stream_in = new Scanner(client.getInputStream());
                    while (server.is_connected()) {
                        String raw_message = stream_in.next();
                        String msg = raw_message;
                        out.write(msg.getBytes());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void send(final Socket client, String message){
        final Server server = this;
        Thread t = new Thread(){
            public void run(){
                
                if (server.is_connected()){
                    String msg = message;
                    client.getOutputStream();
                }
            }
        };
        t.start();
    }

    public static void main(String[] args){

    }



}
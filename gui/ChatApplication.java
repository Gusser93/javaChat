package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;


import java.io.*;
import client.Client;
import tools.Message;
import java.awt.EventQueue;


interface ChatAreaInterface {
    public void appendText(String text);
}

class StreamCapturer extends OutputStream {
    private StringBuilder buffer;
    private ChatAreaInterface chatArea;

    public StreamCapturer(ChatAreaInterface chatArea) {
        this.buffer = new StringBuilder();
        this.chatArea = chatArea;
    }

    @Override
    public void write(int b) throws IOException {
        char c = (char) b;
        String value = Character.toString(c);
        buffer.append(value);
        if (value.equals("\n")) {
            this.chatArea.appendText(buffer.toString());
            buffer.delete(0, buffer.length());
        }
    }
}


public class ChatApplication extends Application implements ChatAreaInterface {

    public TextArea chatArea = new TextArea();
    public Client client = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // window title
        primaryStage.setTitle("Chat");

        // create Client connection
        StreamCapturer out = new StreamCapturer(this);
        this.client = new Client("David", "SuperSecret", "Nick", "192.168.133.96", out);

        // main grid
        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setHgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));

        // add chat area
        this.chatArea.setEditable(false);
        grid.add(this.chatArea, 0, 0, 2, 1);

        // Person to message
        TextField destUser = new TextField ();
        destUser.setText(Message.AT_ALL);

        grid.add(new Label("To: "), 0, 1);
        grid.add(destUser, 1, 1);

        // Add send button and message field
        TextField messageField = new TextField();

        Button sendBt = new Button("Send");
        sendBt.setMinWidth(50);

        sendBt.setOnAction(action -> {
            String dest = destUser.getText();
            String message = messageField.getText();
            this.client.send(message, dest);
            messageField.clear();

            /* write own message to chat
            Message msg = Message.sendPrivateMessage(target, message);
            out.write(msg);
            out.flush();*/

        });

        grid.add(sendBt, 0, 2);
        grid.add(messageField, 1, 2);

        // start scene
        Scene scene = new Scene(grid, 200, 100);
        primaryStage.setScene(scene);
        primaryStage.show();

        // connect to server
        this.client.connect();
    }

    @Override
    public void appendText(final String text) {
        if (EventQueue.isDispatchThread()) {
            this.chatArea.setText(this.chatArea.getText() + text);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    appendText(text);
                }
            });

        }
    }

    @Override
    public void stop(){
        System.out.println("Stage is closing");
        this.client.disconnect();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

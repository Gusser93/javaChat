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
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.PasswordField;
import javafx.util.Pair;
import javafx.scene.Node;
import java.util.Optional;

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

    public String[] showLoginDialog(Stage primaryStage) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Login");


        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(loginButtonType);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        TextField nickname = new TextField();
        nickname.setPromptText("Nickname");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Nickname:"), 0, 1);
        grid.add(nickname, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(password, 1, 2);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // loginbutton and password must not be empty
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean usernameSet = !username.getText().trim().isEmpty();
            boolean passwordSet = !password.getText().trim().isEmpty();
            loginButton.setDisable(!(usernameSet && passwordSet));
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean usernameSet = !username.getText().trim().isEmpty();
            boolean passwordSet = !password.getText().trim().isEmpty();
            loginButton.setDisable(!(usernameSet && passwordSet));
        });

        // set content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // get result
        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            return new String[]{username.getText(), nickname.getText(), password.getText()};
        }

        return null;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // window title
        primaryStage.setTitle("Chat");

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
            if (this.client != null) {
                String dest = destUser.getText();
                String message = messageField.getText();
                this.client.send(message, dest);
                messageField.clear();
            }
        });

        grid.add(sendBt, 0, 2);
        grid.add(messageField, 1, 2);

        // start scene
        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        // login
        String[] loginData = this.showLoginDialog(primaryStage);

        // create Client connection
        StreamCapturer out = new StreamCapturer(this);
        this.client = new Client(loginData[0], loginData[2], loginData[1], "192.168.133.96", out);

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
        System.out.println(this.client.disconnect());
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public AnchorPane messagePanel;
    @FXML
    public TextField textField;
    @FXML
    public TextArea textArea;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private static final int PORT = 3000;
    private static final String ADDRESS = "localhost";

    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private Stage stage;
    private Stage regStage;
    private RegistrationController registrationController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        messagePanel.setVisible(authenticated);
        messagePanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }

        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        setAuthenticated(false);
    }

    private void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/auth_ok")) {
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.startsWith("/reg")) {
                                registrationController.result(str);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")){
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.equals("/clientList")) {
                                String[] token = str.split(" ");

                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        }


                        textArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());
        passwordField.clear();

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "MyFirstChat";
        } else {
            title = String.format("MyFirstChat [ %s ]", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void clientListMouseAction(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText(String.format("/w %s", receiver));
    }

    private void createRegStage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registration.fxml"));
            Parent root = null;
            root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("MyFirstChat");
            regStage.setScene(new Scene(root, 800, 600));
            registrationController = fxmlLoader.getController();
            registrationController.setController(this);

            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void tryReg(ActionEvent actionEvent) {
        if (regStage == null){
            createRegStage();
        }
        regStage.show();
    }
    public void registration(String login, String password, String nickname){
        String msg = String.format("login: %s, password: %s, nickname: %s", login, password, nickname);
        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

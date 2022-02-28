package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrationController {
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    @FXML
    public TextField nicknameField;
    @FXML
    public TextArea textArea;
    @FXML
    public Button registrationButton;

    private HelloController controller;

    public void setController(HelloController controller) {
        this.controller = controller;
    }

    @FXML
    public void tryReg(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText().trim();
        String nickname = nicknameField.getText();

        controller.registration(login, password, nickname);
    }

    public void result(String command){
        if (command.equals("/reg_ok")){
            textArea.appendText("Registration was successful");
        } else {
            textArea.appendText("Login or nickname are taken");
        }
    }
}

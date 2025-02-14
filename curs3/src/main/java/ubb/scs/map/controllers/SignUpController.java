package ubb.scs.map.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.PasswordUtils;

import java.io.IOException;

public class SignUpController {
    private NetworkService networkService;
    private MessageService messageService;

    @FXML
    private TextField firstNameText;
    @FXML
    private TextField lastNameText;
    @FXML
    private TextField usernameText;
    @FXML
    private TextField passwordText;
    @FXML
    private TextField passwordConfirmationText;
    @FXML
    private Label messageToUser;
    @FXML
    private Button createAccountButton;

    private NetworkService getNetworkService() {
        return networkService;
    }

    void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }


    public void handleCreateAccount(ActionEvent event) throws IOException {
        if (!passwordText.getText().equals(passwordConfirmationText.getText())) {
            messageToUser.setText("Passwords do not match");
        } else {
            String hashedPassword = PasswordUtils.hashPassword(passwordText.getText());
            User newUser = new User(usernameText.getText(), firstNameText.getText(), lastNameText.getText(), hashedPassword);
            try {
                networkService.addUser(newUser);
                messageToUser.setText("Account created successfully");

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/userView.fxml"));
                Stage stage = new Stage();
                VBox appLayout = loader.load();
                Scene scene = new Scene(appLayout, 650, 500);
                stage.setTitle("Account Settings");
                stage.setScene(scene);

                UserController userController = loader.getController();
                userController.setNetworkService(networkService);
                userController.setMessageService(messageService);
                userController.initApp(newUser);


                stage.show();
            } catch (ValidationException | UserAlreadyExistsException e) {
                messageToUser.setText(e.getMessage());
                firstNameText.setText("");
                lastNameText.setText("");
                usernameText.setText("");
                passwordText.setText("");
                passwordConfirmationText.setText("");
            } catch (Exception e) {
                messageToUser.setText(e.getMessage());
            }
        }


    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}

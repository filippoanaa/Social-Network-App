package ubb.scs.map.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.MyAlerts;
import ubb.scs.map.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SignUpController {
    private NetworkService networkService;
    private MessageService messageService;
    private byte[] profilePictureBytes;

    @FXML
    private TextField firstNameText;
    @FXML
    private TextField lastNameText;
    @FXML
    private TextField usernameText;
    @FXML
    private PasswordField passwordText;
    @FXML
    private PasswordField passwordConfirmationText;

    void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void handleUploadPicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                profilePictureBytes = Files.readAllBytes(selectedFile.toPath());
            } catch (IOException e) {
                MyAlerts.showErrorAlert("Failed to upload picture.");
            }
        }
    }

    public void handleCreateAccount(ActionEvent event) {
        if (!passwordText.getText().equals(passwordConfirmationText.getText())) {
            MyAlerts.showErrorAlert("Passwords do not match");
            passwordText.clear();
            passwordConfirmationText.clear();
        } else {
            if (profilePictureBytes == null) {
                MyAlerts.showErrorAlert("Profile picture is required.");
                return;
            }

            User newUser = new User(usernameText.getText(), firstNameText.getText(), lastNameText.getText(), passwordText.getText(), profilePictureBytes);
            try {
                networkService.addUser(newUser);
                MyAlerts.showConfirmationAlert("Account created successfully");

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();

                UIUtils.openAccountSettings(networkService, messageService, newUser);
            }catch (ValidationException e){
                MyAlerts.showErrorAlert(e.getMessage());
            } catch (Exception e) {
                MyAlerts.showErrorAlert(e.getMessage());
            }
            clearFields();
        }
    }


    private void clearFields() {
        firstNameText.setText("");
        lastNameText.setText("");
        usernameText.setText("");
        passwordText.setText("");
        passwordConfirmationText.setText("");
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}
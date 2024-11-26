package ubb.scs.map.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
import ubb.scs.map.domain.validators.FriendshipValidator;
import ubb.scs.map.domain.validators.UserValidator;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.repository.database.FriendshipRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.util.UUID;

public class SignUpController {
    private NetworkService networkService;
    private MessageService messageService;

    @FXML
    public TextField firstNameText;
    @FXML
    public TextField lastNameText;
    @FXML
    public TextField usernameText;
    @FXML
    public TextField passwordText;
    @FXML
    public TextField passwordConfirmationText;
    @FXML
    public Label messageToUser;
    @FXML
    public Button createAccountButton;

    private NetworkService getNetworkService() {
        return networkService;
    }
    void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }



    public void handleCreateAccount(ActionEvent event) throws IOException {
        if(!passwordText.getText().equals(passwordConfirmationText.getText())) {
            messageToUser.setText("Passwords do not match");
        }else{
            User newUser = new User(usernameText.getText(), firstNameText.getText(), lastNameText.getText(), passwordText.getText());
            try{
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
            }catch(ValidationException | UserAlreadyExistsException e){
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

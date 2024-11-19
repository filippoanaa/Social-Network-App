package ubb.scs.map.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.util.UUID;

public class SignUpController {
    private NetworkService networkService;

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

    private void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    @FXML
    public void initialize() {
        final String url = "jdbc:postgresql://localhost:5432/social_network";
        final String username = "postgres";
        final String password = "postgres";

        Repository<UUID, User> userRepository = new UserRepositoryDatabase(url, username, password);
        Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository = new FriendshipRepositoryDatabase(url, username, password);
        Validator<User> userValidator = new UserValidator();
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        this.networkService = new NetworkService(userRepository, friendshipRepository, userValidator, friendshipValidator);

        firstNameText.setText("");
        lastNameText.setText("");
        usernameText.setText("");
        passwordText.setText("");
        passwordConfirmationText.setText("");

    }

    public void handleCreateAccount(ActionEvent event) throws IOException {
        if(!passwordText.getText().equals(passwordConfirmationText.getText())) {
            messageToUser.setText("Passwords do not match");
        }else{
            User newUser = new User(usernameText.getText(), firstNameText.getText(), lastNameText.getText(), passwordText.getText());
            try{
                networkService.addUser(newUser);
                UserController.setActiveUser(newUser);
                messageToUser.setText("Account created successfully");

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/userView.fxml"));
                AnchorPane root = loader.load();
                Scene scene = new Scene(root, 650, 500);
                Stage stage = new Stage();
                stage.setTitle("Account Settings");
                stage.setScene(scene);
                stage.show();
            }catch(ValidationException | UserAlreadyExistsException e){
                messageToUser.setText(e.getMessage());
                firstNameText.setText("");
                lastNameText.setText("");
                usernameText.setText("");
                passwordText.setText("");
                passwordConfirmationText.setText("");
            }
        }


    }
}

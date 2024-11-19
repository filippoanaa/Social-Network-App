package ubb.scs.map.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.validators.FriendshipValidator;
import ubb.scs.map.domain.validators.UserValidator;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.repository.database.FriendshipRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class LogInController {

    private NetworkService networkService;

    @FXML
    TextField usernameText;
    @FXML
    PasswordField passwordText;
    @FXML
    Button loginButton;
    @FXML
    Label messageToUser;

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

        usernameText.setText("");
        passwordText.setText("");

    }

    public void handleLogInButton(ActionEvent event) throws IOException {
        String username = usernameText.getText();
        String password = passwordText.getText();
        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Both username and password are required");
            }
            if (!networkService.verifyCredentials(username, password)) {
                throw new IllegalArgumentException("Invalid username or password");
            }
            User user = networkService.findUserByUsername(username);
            UserController.setActiveUser(user);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/userView.fxml"));
            VBox root = loader.load();
            Scene scene = new Scene(root, 650, 500);
            Stage stage = new Stage();
            stage.setTitle("Account Settings");
            stage.setScene(scene);
            stage.show();

        } catch (IllegalArgumentException e) {
            messageToUser.setText(e.getMessage());
            usernameText.setText("");
            passwordText.setText("");
        }
        catch(EntityMissingException e) {
            messageToUser.setText("No account found! Click to sing up.");
            usernameText.setText("");
            passwordText.setText("");
        }

    }

    public void handleSignUp(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/signup.fxml"));
        GridPane root = loader.load();
        Scene scene = new Scene(root, 650, 500);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Sign Up");
        stage.show();
    }


}

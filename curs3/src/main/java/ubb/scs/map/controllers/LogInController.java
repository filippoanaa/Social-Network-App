package ubb.scs.map.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.util.List;

public class LogInController {

    private NetworkService networkService;
    private MessageService messageService;

    @FXML
    TextField usernameText;
    @FXML
    PasswordField passwordText;
    @FXML
    Button loginButton;
    @FXML
    Label messageToUser;

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public NetworkService getNetworkService() {
        return networkService;
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
            userController.initApp(user);

            stage.show();


            List<Friendship> pendingRequests = networkService.getReceivedRequests(user.getId()).stream()
                    .filter(friendship -> friendship.getFriendshipStatus().equals(FriendshipStatus.PENDING))
                    .toList();
            for (Friendship friendship : pendingRequests) {
                if (!friendship.isNotificationSent()) {
                    userController.sendNotification(friendship);

                }
            }


        } catch (IllegalArgumentException e) {
            messageToUser.setText(e.getMessage());
            usernameText.setText("");
            passwordText.setText("");
        } catch (EntityMissingException e) {
            messageToUser.setText("No account found! Click to sing up.");
            usernameText.setText("");
            passwordText.setText("");
        }
        usernameText.setText("");
        passwordText.setText("");

    }

    public void handleSignUp(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/signUp.fxml"));
        Stage stage = new Stage();
        GridPane appLayout = loader.load();
        Scene scene = new Scene(appLayout, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Sign Up");

        SignUpController controller = loader.getController();
        controller.setNetworkService(networkService);
        controller.setMessageService(messageService);
        stage.show();
    }


    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}

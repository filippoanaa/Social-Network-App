package ubb.scs.map.controllers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.User;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.MyAlerts;
import ubb.scs.map.utils.UIUtils;

import java.io.IOException;
import java.util.List;

public class LogInController {
    private NetworkService networkService;
    private MessageService messageService;

    @FXML
    private TextField usernameText;
    @FXML
    private PasswordField passwordText;

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void handleLogInButton() throws IOException {

        String username = usernameText.getText();
        String password = passwordText.getText();
        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Both username and password are required.");
            }
            if (!networkService.verifyCredentials(username, password)) {
                throw new IllegalArgumentException("Invalid username or password. Click to sign up!");
            }

            User user = networkService.findUserByUsername(username);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/userView.fxml"));
            UserController userController = UIUtils.openAccountSettings(networkService, messageService, user);


            List<Friendship> pendingRequests = networkService.getReceivedRequests(user.getId()).stream()
                    .filter(friendship -> friendship.getFriendshipStatus().equals(FriendshipStatus.PENDING))
                    .toList();
            for (Friendship friendship : pendingRequests) {
                if (!friendship.isNotificationSent()) {
                    userController.sendNotification(friendship);
                }
            }


        } catch (IllegalArgumentException e) {
            MyAlerts.showErrorAlert(e.getMessage());
        }
        usernameText.setText("");
        passwordText.setText("");

    }

    public void handleSignUp() throws IOException {
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


}

package ubb.scs.map.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;


public class ProfileController {

    @FXML
    private Label lblUsername;
    @FXML
    private Label lblFullName;
    @FXML
    private Label lblFriendsCount;
    @FXML
    private Label lblFriendStatus;
    @FXML
    private ImageView imgProfilePicture;
    @FXML
    private Button btnDynamic;
    @FXML
    private Button btnOpenChat;

    private NetworkService networkService;
    private MessageService messageService;
    private User currentUser;
    private User connectedUser;
    private UserController mainController;

    public void setMainController(UserController mainController) {
        this.mainController = mainController;
        System.out.println("Main Controller all set");
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }


    public void setUsers(User user, User connectedUser) {
        this.currentUser = user;
        this.connectedUser = connectedUser;
        lblUsername.setText(user.getUsername());
        lblFullName.setText(user.getFirstName() + " " + user.getLastName());
        lblFriendsCount.setText(String.valueOf(networkService.getFriendsCount(currentUser.getId())));

        String imagePath = "/images/matt.jpg";
        try {
            imgProfilePicture.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
            Image image = new Image(getClass().getResource(imagePath).toExternalForm());
            imgProfilePicture.setImage(image);
        } catch (Exception e) {
            System.err.println("Image not found: " + imagePath);
            e.printStackTrace();
        }

        updateDynamicButton();
    }

    @FXML
    private void updateDynamicButton() {
        Friendship friendship = networkService.findFriendship(currentUser.getId(), connectedUser.getId());
        if (friendship == null) {
            btnDynamic.setText("Add Friend");
            lblFriendStatus.setText("No connection");
            btnDynamic.setDisable(false);
            btnDynamic.setOnAction(event -> sendFriendRequest());
        } else if (friendship.getFriendshipStatus().equals(FriendshipStatus.PENDING)) {
            if (friendship.isSender(connectedUser.getId())) {
                btnDynamic.setText("Cancel Request");
                lblFriendStatus.setText("Friend request sent");
                btnDynamic.setDisable(false);
                btnDynamic.setOnAction(event -> cancelFriendRequest());
            } else {
                btnDynamic.setText("Accept Request");
                lblFriendStatus.setText("Friend request received");
                btnDynamic.setDisable(false);
                btnDynamic.setOnAction(event -> acceptFriendRequest());
            }
        } else if (friendship.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED)) {
            btnDynamic.setText("Remove Friend");
            lblFriendStatus.setText("Friends");
            btnDynamic.setDisable(false);
            btnDynamic.setOnAction(event -> removeFriend());
        }
        System.out.println("Dynamic button updated");
    }

    @FXML
    private void sendFriendRequest() {
        Friendship friendship = new Friendship(currentUser.getId(), connectedUser.getId());
        friendship.setSender(connectedUser.getId());
        networkService.createFriendship(friendship);
        mainController.initSentRequests();
        mainController.initReceivedRequests();
        updateDynamicButton();
    }

    @FXML
    private void cancelFriendRequest() {
        networkService.declineFriendRequest(new Tuple<>(currentUser.getId(), connectedUser.getId()));
        mainController.initSentRequests();
        updateDynamicButton();
    }

    @FXML
    private void acceptFriendRequest() {
        networkService.acceptFriendRequest(new Tuple<>(connectedUser.getId(), currentUser.getId()));
        mainController.initReceivedRequests();
        mainController.initPageOfFriends();
        lblFriendStatus.setText("Friends");
        updateDynamicButton();
    }

    @FXML
    private void removeFriend() {
        networkService.removeFriendRequest(currentUser.getId(), connectedUser.getId());
        mainController.initPageOfFriends();
        mainController.initReceivedRequests();
        lblFriendStatus.setText("No connection");
        updateDynamicButton();
    }


    @FXML
    private void handleOpenChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/chatView.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Chat");

            MessageController messageController = loader.getController();
            messageController.setMessageService(messageService);
            messageController.setUsers(connectedUser, currentUser);
            messageController.handleOpenChat();

            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
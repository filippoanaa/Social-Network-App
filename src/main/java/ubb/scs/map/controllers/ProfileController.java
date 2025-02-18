package ubb.scs.map.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.MyAlerts;
import ubb.scs.map.utils.UIUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class ProfileController{

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

    private NetworkService networkService;
    private MessageService messageService;
    private User currentUser;
    private User connectedUser;

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

        try {
            byte[] profilePicData = currentUser.getProfilePicture();
            if(profilePicData != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(profilePicData);
                Image profileImage = new Image(bis);
                imgProfilePicture.setImage(profileImage);
            }

        } catch (Exception e) {
            MyAlerts.showErrorAlert(e.getMessage());
        }

        updateDynamicButton();
    }

    @FXML
    private void updateDynamicButton() {
        Friendship friendship = networkService.findFriendship(currentUser.getId(), connectedUser.getId()).orElse(null);
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
    }

    @FXML
    private void sendFriendRequest() {
        Friendship friendship = new Friendship(currentUser.getId(), connectedUser.getId());
        friendship.setSender(connectedUser.getId());
        networkService.createFriendship(friendship);
        updateDynamicButton();
    }

    @FXML
    private void cancelFriendRequest() {
        networkService.declineFriendRequest(new Tuple<>(currentUser.getId(), connectedUser.getId()));
        updateDynamicButton();
    }

    @FXML
    private void acceptFriendRequest() {
        networkService.acceptFriendRequest(new Tuple<>(connectedUser.getId(), currentUser.getId()));
        lblFriendStatus.setText("Friends");
        lblFriendsCount.setText(String.valueOf(networkService.getFriendsCount(currentUser.getId())));
        updateDynamicButton();
    }

    @FXML
    private void removeFriend() {
        networkService.removeFriendship(currentUser.getId(), connectedUser.getId());
        lblFriendStatus.setText("No connection");
        lblFriendsCount.setText(String.valueOf(networkService.getFriendsCount(currentUser.getId())));
        updateDynamicButton();
    }


    @FXML
    private void handleOpenChat() throws IOException {
        UIUtils.openChatController(messageService, connectedUser, currentUser);
    }

}
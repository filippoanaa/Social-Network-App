package ubb.scs.map.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import ubb.scs.map.domain.*;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class UserController implements Initializable {
    private User activeUser;
    public Label welcomeLabel;
    public Button buttonSendFriendRequest;
    public Button buttonRemoveFriend;

    public TableView<User> friendsTable;
    public TableColumn<User, String> friendColumnUsername;
    public TableColumn<User, String> friendColumnFirstName;
    public TableColumn<User, String> friendColumnLastName;

    public TableView<User> usersTable;
    public TableColumn<User, String> userColumnUsername;
    public TableColumn<User, String> userColumnFirstName;
    public TableColumn<User, String> userColumnLastName;
    public Label messageToUser;

    public TableView<Friendship> receivedFriendRequestsTable;
    public TableColumn<Friendship, String> receivedFriendRequestsFirstName;
    public TableColumn<Friendship, String> receivedFriendRequestsLastName;
    public TableColumn<Friendship, String> receivedFriendRequestsDate;
    public TableColumn<Friendship, String> receivedFriendRequestsStatus;

    public TableView<Friendship> sentFriendRequestsTable;
    public TableColumn<Friendship, String> sentFriendRequestsFirstName;
    public TableColumn<Friendship, String> sentFriendRequestsLastName;
    public TableColumn<Friendship, String> sentFriendRequestsDate;
    public TableColumn<Friendship, String> sentFriendRequestsStatus;

    public Button buttonAcceptFriendRequest;
    public Button buttonDeclineFriendRequest;

    public Button buttonDeleteFriendRequest;
    public Button buttonDeleteAccount;
    public Button buttonLogOut;

    ObservableList<User> usersModel = FXCollections.observableArrayList();
    ObservableList<User> friendsModel = FXCollections.observableArrayList();
    ObservableList<Friendship> receivedFriendRequestModel = FXCollections.observableArrayList();
    ObservableList<Friendship> sentFriendRequestModel = FXCollections.observableArrayList();


    private NetworkService networkService;


    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void initApp(User newUser) {
        this.activeUser = newUser;
        welcomeLabel.setText("Welcome, " + activeUser.getUsername() + "!");
        setupUserTable();
        setupFriendTable();
        setupReceivedFriendRequestsTable();
        setupSentFriendRequestsTable();
        initFriendsList();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usersTable.setItems(usersModel);
        friendsTable.setItems(friendsModel);
        receivedFriendRequestsTable.setItems(receivedFriendRequestModel);
        sentFriendRequestsTable.setItems(sentFriendRequestModel);
        friendsList.setItems(friendsListModel);
        messagesList.setItems(messagesModel);
    }

    private void initFriends() {
        //actualizez continutul listei
        Iterable<User> users = networkService.getAcceptedFriendRequests(activeUser.getId());
        List<User> usersList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        friendsModel.setAll(usersList);
    }


    private void initUsers() {
        Iterable<User> users = networkService.getAllUsers();
        List<User> usersList = StreamSupport.stream(users.spliterator(), false)
                .filter(user -> !Objects.equals(user.getId(), activeUser.getId()))
                .collect(Collectors.toList());
        usersModel.setAll(usersList);
    }


    private void initReceivedRequests() {
        Iterable<Friendship> friendships = networkService.getReceivedRequests(activeUser.getId());
        List<Friendship> friendRequestsList = StreamSupport.stream(friendships.spliterator(), false)
                .collect(Collectors.toList());
        receivedFriendRequestModel.setAll(friendRequestsList);
    }

    private void initSentRequests() {
        Iterable<Friendship> friendships = networkService.getSentRequests(activeUser.getId());
        List<Friendship> friendRequestsList = StreamSupport.stream(friendships.spliterator(), false)
                .filter(friendship -> !friendship.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED))
                .collect(Collectors.toList());
        sentFriendRequestModel.setAll(friendRequestsList);
    }


    private void setupUserTable() {
        //configurez coloane si initializez datele in tabel
        userColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        userColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        userColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        initUsers();
    }

    private void setupFriendTable() {
        friendColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        initFriends();
    }

    private void setupSentFriendRequestsTable() {
        sentFriendRequestsFirstName.setCellValueFactory(cellData -> {
            Friendship friendship = cellData.getValue();
            UUID receiverId = getRecipientId(friendship);
            Optional<User> receiver = networkService.findUserById(receiverId);
            return receiver.map(user -> new SimpleStringProperty(user.getFirstName())).orElse(new SimpleStringProperty("Unknown"));
        });

        sentFriendRequestsLastName.setCellValueFactory(cellData -> {
            Friendship friendship = cellData.getValue();
            UUID receiverId = getRecipientId(friendship);
            Optional<User> receiver = networkService.findUserById(receiverId);
            return receiver.map(user -> new SimpleStringProperty(user.getLastName())).orElse(new SimpleStringProperty("Unknown"));
        });

        sentFriendRequestsDate.setCellValueFactory(new PropertyValueFactory<>("friendsFrom"));
        sentFriendRequestsStatus.setCellValueFactory(new PropertyValueFactory<>("friendshipStatus"));
        initSentRequests();
    }

    private void setupReceivedFriendRequestsTable() {
        receivedFriendRequestsFirstName.setCellValueFactory(cellData -> {
            UUID senderId = cellData.getValue().getIdSender();
            Optional<User> sender = networkService.findUserById(senderId);
            return sender.map(user -> new SimpleStringProperty(user.getFirstName()))
                    .orElse(new SimpleStringProperty("Unknown"));
        });

        receivedFriendRequestsLastName.setCellValueFactory(cellData -> {
            UUID senderId = cellData.getValue().getIdSender();
            Optional<User> sender = networkService.findUserById(senderId);
            return sender.map(user -> new SimpleStringProperty(user.getLastName()))
                    .orElse(new SimpleStringProperty("Unknown"));
        });

        receivedFriendRequestsDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFriendsFrom().toString()));
        receivedFriendRequestsStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFriendshipStatus().name()));
        initReceivedRequests();
    }

    public UUID getRecipientId(Friendship friendship) {
        if (friendship.isSender(friendship.getUser1())) {
            return friendship.getUser2();
        } else {
            return friendship.getUser1();
        }
    }


    public void handleRemoveFriend(ActionEvent event) {
        User user = friendsTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            try {
                networkService.removeFriendship(user.getId(), activeUser.getId());
                initFriends();
                initReceivedRequests();
                messageToUser.setText("Friend removed successfully");
            } catch (Exception e) {
                messageToUser.setText(e.getMessage());
            }
        } else {
            messageToUser.setText("Select a friend from Friends Table!.");
        }
    }


    public void handleSendRequest(ActionEvent event) {
        try {
            User user = usersTable.getSelectionModel().getSelectedItem();
            if (user != null) {
                User newFriend = networkService.findUserByUsername(user.getUsername());
                Friendship friendship = new Friendship(activeUser.getId(), newFriend.getId());
                friendship.setSender(activeUser.getId());

                networkService.createFriendship(friendship);
                messageToUser.setText("Friend request sent to " + newFriend.getUsername());


                initSentRequests();
                initReceivedRequests();
            } else {
                messageToUser.setText("Select an user from the available users table");
            }
        } catch (ValidationException | UserMissingException | EntityAlreadyExistsException e) {
            messageToUser.setText(e.getMessage());
        }

    }


    public void sendNotification(Friendship friendship) {
        User sender = networkService.findUserById(friendship.getIdSender()).orElseThrow();
        String message = "You have a new friend request from: "  + sender.getFirstName() + " " + sender.getLastName();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New friend request");
        alert.setHeaderText(message);
        alert.show();

        friendship.setNotificationSent(true);
        networkService.updateFriendship(friendship);
    }


    public void handleDeleteFriendRequest(ActionEvent event) {
        Friendship friendship = sentFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.removeFriendRequest(friendship.getId().getE1(), friendship.getId().getE2());
                messageToUser.setText("Friend request deleted");
                initSentRequests();
            } catch (EntityMissingException | IllegalArgumentException e) {
                messageToUser.setText(e.getMessage());
            }
        } else {
            messageToUser.setText("Select a friend request from the table first!");
        }

    }

    public void handleAcceptFriendRequest(ActionEvent event) {
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.acceptFriendRequest(friendship.getId());
                messageToUser.setText("Friend request accepted");
                initReceivedRequests();
                initFriends();
            } catch (Exception e) {
                messageToUser.setText(e.getMessage());
            }
        } else {
            messageToUser.setText("Select a friend request first");
        }
    }

    public void handleDeclineFriendRequest(ActionEvent event) {
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.declineFriendRequest(friendship.getId());
                initReceivedRequests();
                initFriends();
                messageToUser.setText("Friend request declined");
            } catch (EntityMissingException | IllegalArgumentException e) {
                messageToUser.setText(e.getMessage());
            }
        } else {
            messageToUser.setText("Select a friend request first");
        }
    }


    public void handleDeleteAccount(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure you want to delete your account?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    networkService.deleteUser(activeUser.getId());
                    messageToUser.setText("Account deleted successfully");

                    Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                    currentStage.close();
                } catch (Exception e) {
                    messageToUser.setText("Error deleting account: " + e.getMessage());
                }
            } else {
                messageToUser.setText("We are glad you've changed your mind.");
            }
        });
    }

    public void handleLogOut(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    private MessageService messageService;
    public Button buttonSendMessage;
    public TextField messageTextField;

    ObservableList<Message> messagesModel = FXCollections.observableArrayList();
    ObservableList<String> friendsListModel = FXCollections.observableArrayList();

    public ListView<String> friendsList = new ListView<>();
    public ListView<Message> messagesList = new ListView<>();


    @FXML
    private Label chatWithLabel;
    @FXML
    private TextArea chatTextArea;

    String chatWith;


    private void initFriendsList() {
        Iterable<User> friends = networkService.getAcceptedFriendRequests(activeUser.getId());
        List<String> usernames = StreamSupport.stream(friends.spliterator(), false)
                .map(User::getUsername)
                .collect(Collectors.toList());
        friendsListModel.setAll(usernames);
    }

    private List<Message> initMessages() {
        User chatWithUser = networkService.findUserByUsername(chatWith);
        Iterable<Message> messages = messageService.getMessagesBetween(activeUser.getId(), chatWithUser.getId());
        List<Message> messagesList = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        messagesModel.setAll(messagesList);
        return messagesList;

    }

    public void handelSendMessage() {
        String text = messageTextField.getText();
        if (!text.isEmpty()) {
            messageTextField.clear();

            List<User> to = new ArrayList<>();

            chatWith = friendsList.getSelectionModel().getSelectedItem();
            User recipient = networkService.findUserByUsername(chatWith);
            to.add(recipient);

            Message message = new Message(activeUser, to, text);
            message.setFrom(activeUser);
            messageService.addMessage(message);

            chatTextArea.appendText("You: " + message.getText() + "\n");
            messagesModel.add(message);
            //initMessages();

        } else {
            messageToUser.setText("Select a friends first and type a message");
        }
    }


    public void handleFriendSelection(MouseEvent mouseEvent) {
        chatWith = friendsList.getSelectionModel().getSelectedItem();
        if (chatWith != null) {
            chatTextArea.clear();
            chatWithLabel.setText("Chatting with: " + chatWith);

            messagesList.setItems(messagesModel);

            List<Message> messages = initMessages();
            formatMessages(messages);

        }
    }

    private void formatMessages(List<Message> messages) {
        for (Message message : messages) {
            if (message.getFrom().getId().equals(activeUser.getId())) {
                chatTextArea.appendText("You: " + message.getText() + "\n");
            } else {
                chatTextArea.appendText(chatWith + ": " + message.getText() + "\n");
            }
        }

    }

}
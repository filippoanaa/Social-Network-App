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
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.Page;
import ubb.scs.map.utils.Pageable;

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

    private ObservableList<User> usersModel = FXCollections.observableArrayList();
    private ObservableList<User> friendsModel = FXCollections.observableArrayList();
    private ObservableList<Friendship> receivedFriendRequestModel = FXCollections.observableArrayList();
    private ObservableList<Friendship> sentFriendRequestModel = FXCollections.observableArrayList();


    public Button buttonAcceptFriendRequest;
    public Button buttonDeclineFriendRequest;

    public Button buttonDeleteFriendRequest;

    public Button buttonDeleteAccount;
    public Button buttonLogOut;

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
        initUsersList();
        initMultiUserSelectionList();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usersTable.setItems(usersModel);
        friendsTable.setItems(friendsModel);
        receivedFriendRequestsTable.setItems(receivedFriendRequestModel);
        sentFriendRequestsTable.setItems(sentFriendRequestModel);
        usersList.setItems(usersListModel);
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
        //initFriends();
        initPageOfFriends();
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
        messageToUser.setText("");
        User user = friendsTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            try {
                networkService.removeFriendship(user.getId(), activeUser.getId());
                initPageOfFriends();
                //initFriends();
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
        messageToUser.setText("");
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
        messageToUser.setText("");

        User sender = networkService.findUserById(friendship.getIdSender()).orElseThrow();
        String message = "You have a new friend request from: " + sender.getFirstName() + " " + sender.getLastName();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New friend request");
        alert.setHeaderText(message);
        alert.show();

        friendship.setNotificationSent(true);
        networkService.updateFriendship(friendship);
    }


    public void handleDeleteFriendRequest(ActionEvent event) {
        messageToUser.setText("");
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
        messageToUser.setText("");
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.acceptFriendRequest(friendship.getId());
                messageToUser.setText("Friend request accepted");
                initReceivedRequests();
                //initFriends();
                initPageOfFriends();
            } catch (Exception e) {
                messageToUser.setText(e.getMessage());
            }
        } else {
            messageToUser.setText("Select a friend request first");
        }
    }

    public void handleDeclineFriendRequest(ActionEvent event) {
        messageToUser.setText("");
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.declineFriendRequest(friendship.getId());
                initReceivedRequests();
                //initFriends();
                initPageOfFriends();
                messageToUser.setText("Friend request declined");
            } catch (EntityMissingException | IllegalArgumentException e) {
                messageToUser.setText(e.getMessage());
            }
        } else {
            messageToUser.setText("Select a friend request first");
        }
    }


    public void handleDeleteAccount(ActionEvent event) throws IOException {
        messageToUser.setText("");

        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("NO", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?", yesButton, noButton);
        alert.setTitle("Confirmation");

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
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

    ObservableList<String> usersListModel = FXCollections.observableArrayList();
    ObservableList<Message> messagesModel = FXCollections.observableArrayList();
    ObservableList<CheckBox> checkBoxList = FXCollections.observableArrayList();

    public ListView<String> usersList = new ListView<>();
    public ListView<Message> messagesList = new ListView<>();
    public ListView<CheckBox> multiUserSelectionList = new ListView<>();

    public Button buttonDeleteConversation;

    @FXML
    private Label chatWithLabel;


    private void initUsersList() {
        Iterable<User> users = networkService.getAllUsers();
        List<String> usernames = StreamSupport.stream(users.spliterator(), false)
                .filter(user -> !user.getId().equals(activeUser.getId()))
                .map(User::getUsername)
                .collect(Collectors.toList());
        usersListModel.setAll(usernames);
    }

    private List<Message> initMessages() {
        String chatWith = usersList.getSelectionModel().getSelectedItem();
        User chatWithUser = networkService.findUserByUsername(chatWith);
        Iterable<Message> messages = messageService.getMessagesBetween(activeUser.getId(), chatWithUser.getId());
        List<Message> messagesList = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        messagesModel.setAll(messagesList);
        return messagesList;

    }

    private void initMultiUserSelectionList() {
        for (User user : networkService.getAllUsers()) {
            if (!user.getId().equals(activeUser.getId())) {
                CheckBox checkBox = new CheckBox(user.getUsername());
                checkBoxList.add(checkBox);
            }
        }
        multiUserSelectionList.setItems(checkBoxList);
    }


    public void handleSendMessage() {
        messageToUser.setText("");
        String text = messageTextField.getText();
        if (!text.isEmpty()) {
            messageTextField.clear();

            List<User> to = new ArrayList<>();

            boolean isMultipleUsersSelected = multiUserSelectionList.getItems().stream()
                    .anyMatch(CheckBox::isSelected);

            String chatWith = usersList.getSelectionModel().getSelectedItem();
            boolean isOneSingleUserSelected = chatWith != null;

            if (isMultipleUsersSelected && isOneSingleUserSelected) {
                messageToUser.setText("Please select either a single user or multiple users, not both!");

                for (CheckBox checkBox : multiUserSelectionList.getItems()) {
                    checkBox.setSelected(false);
                }

                usersList.getSelectionModel().clearSelection();

                return;
            }

            if (isMultipleUsersSelected) {
                for (CheckBox checkBox : multiUserSelectionList.getItems()) {
                    if (checkBox.isSelected()) {
                        String username = checkBox.getText();
                        User user = networkService.findUserByUsername(username);
                        to.add(user);
                    }
                }
            } else if (isOneSingleUserSelected) {
                User recipient = networkService.findUserByUsername(chatWith);
                to.add(recipient);

            } else {
                messageToUser.setText("Please select at least one recipient.");
                return;
            }


            Message message = new Message(activeUser, to, text);

            Message selectedMessage = messagesList.getSelectionModel().getSelectedItem();
            if (selectedMessage != null) {
                message.setReply(selectedMessage);
                messagesList.getSelectionModel().clearSelection();
            }

            message.setFrom(activeUser);
            messageService.addMessage(message);
            messagesModel.add(message);

        } else {
            messageToUser.setText("Type a message before sending.");
        }

        for (CheckBox checkBox : multiUserSelectionList.getItems()) {
            checkBox.setSelected(false);
        }
        usersList.getSelectionModel().clearSelection();
    }


    public void handleFriendSelection(MouseEvent mouseEvent) {
        messageToUser.setText("");
        String chatWith = usersList.getSelectionModel().getSelectedItem();
        if (chatWith != null) {
            chatWithLabel.setText("Chatting with: " + chatWith);

            List<Message> messages = initMessages();
            if (messages.isEmpty()) {
                messageToUser.setText("No messages found");
            }
            formatMessages(messages);
        }
    }


    private void formatMessages(List<Message> messages) {
        messagesModel.setAll(messages);

        messagesList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    String displayText = "";
                    if (message.getFrom().equals(activeUser)) {
                        displayText += "You: " + message.getText();
                    } else {
                        displayText += message.getFrom().getUsername() + ": " + message.getText();
                    }
                    if (message.getReply() != null) {
                        displayText += " (Reply to: " + message.getReply().getText() + ")";
                    }
                    setText(displayText);
                }
            }
        });
    }

    public void handleDeleteConversation(ActionEvent actionEvent) {
        messageToUser.setText("");
        String chatWith = usersList.getSelectionModel().getSelectedItem();
        if (chatWith != null) {
            User chatWithUser = networkService.findUserByUsername(chatWith);
            messageService.deleteConversation(activeUser.getId(), chatWithUser.getId());
            messageToUser.setText("Conversation deleted successfully");
            initMessages();
        } else {
            messageToUser.setText("Select a conversation from the inbox first");
        }
    }


    private int currentPageFriendsTable = 0;

    @FXML
    Button buttonNextFT;
    @FXML
    Button buttonPreviousFT;
    @FXML
    Label labelPageFT;


    private void initPageOfFriends() {

        int pageSizeFT = 3;
        Pageable pageable = new Pageable(currentPageFriendsTable, pageSizeFT);
        Page<User> page = networkService.findAllFriendsOnPage(pageable, activeUser.getId());

        int maxPage = Math.max(0, (int)(Math.ceil((double) page.getTotalNumberOfElements() / pageSizeFT) - 1));

        if(currentPageFriendsTable > maxPage) {
            currentPageFriendsTable = maxPage;
            page = networkService.findAllFriendsOnPage(new Pageable(currentPageFriendsTable, pageSizeFT), activeUser.getId());
        }

        int totalNumberOfFriendsTable = page.getTotalNumberOfElements();

        buttonPreviousFT.setDisable(currentPageFriendsTable == 0);
        buttonNextFT.setDisable((currentPageFriendsTable + 1) * pageSizeFT >= totalNumberOfFriendsTable);

        List<User> friends = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());

        friendsModel.setAll(friends);

        labelPageFT.setText((currentPageFriendsTable + 1) + "/" + (maxPage + 1));

    }


    public void onNextPageFT(ActionEvent actionEvent) {
        currentPageFriendsTable++;
        initPageOfFriends();
    }

    public void onPreviousPageFT(ActionEvent actionEvent) {
        currentPageFriendsTable--;
        initPageOfFriends();
    }



}

package ubb.scs.map.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.Observer;
import ubb.scs.map.utils.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class UserController implements Initializable, Observer {
    private User activeUser;
    private NetworkService networkService;
    private MessageService messageService;

    @FXML
    private Label welcomeLabel;

    //friends tab
    @FXML
    private TableView<User> friendsTable;
    @FXML
    private TableColumn<User, String> friendColumnUsername;
    @FXML
    private TableColumn<User, String> friendColumnFirstName;
    @FXML
    private TableColumn<User, String> friendColumnLastName;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonPrevious;
    @FXML
    private Label labelPage;
    private int currentPageFriendsTable = 0;

    //available users tab
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> userColumnUsername;
    @FXML
    private TableColumn<User, String> userColumnFirstName;
    @FXML
    private TableColumn<User, String> userColumnLastName;

    //manage received requests tab
    @FXML
    private TableView<Friendship> receivedFriendRequestsTable;
    @FXML
    private TableColumn<Friendship, String> receivedFriendRequestsFirstName;
    @FXML
    private TableColumn<Friendship, String> receivedFriendRequestsLastName;
    @FXML
    private TableColumn<Friendship, String> receivedFriendRequestsDate;
    @FXML
    private TableColumn<Friendship, String> receivedFriendRequestsStatus;

    //manage sent friend requests tab
    @FXML
    private TableView<Friendship> sentFriendRequestsTable;
    @FXML
    private TableColumn<Friendship, String> sentFriendRequestsFirstName;
    @FXML
    private TableColumn<Friendship, String> sentFriendRequestsLastName;
    @FXML
    private TableColumn<Friendship, String> sentFriendRequestsDate;
    @FXML
    private TableColumn<Friendship, String> sentFriendRequestsStatus;


    //messages tab
    @FXML
    private TextField messageTextField;
    @FXML
    private ListView<String> usersList;
    @FXML
    private ListView<CheckBox> multiUserSelectionList;
    private final ObservableList<String> usersListModel = FXCollections.observableArrayList();
    private final ObservableList<CheckBox> checkBoxList = FXCollections.observableArrayList();

    //account settings tab
    @FXML
    private ImageView imgProfilePicture;
    @FXML
    private Label lblFriendsCount;


    private final ObservableList<User> usersModel = FXCollections.observableArrayList();
    private final ObservableList<User> friendsModel = FXCollections.observableArrayList();
    private final ObservableList<Friendship> receivedFriendRequestModel = FXCollections.observableArrayList();
    private final ObservableList<Friendship> sentFriendRequestModel = FXCollections.observableArrayList();


    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
        networkService.addObserver(this);
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
        messageService.addObserver(this);
    }


    private void loadUserProfileData(User user) {
        try {
            byte[] profilePicData = user.getProfilePicture();
            if (profilePicData != null) {
                Image profileImage = new Image(new ByteArrayInputStream(profilePicData));
                imgProfilePicture.setImage(profileImage);
            }
            lblFriendsCount.setText(String.valueOf(networkService.getFriendsCount(user.getId())));
        } catch (Exception e) {
            MyAlerts.showErrorAlert("Failed to load profile data.");
        }
    }

    @FXML
    private void handleChangeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                byte[] profilePictureData = Files.readAllBytes(selectedFile.toPath());
                activeUser.setProfilePicture(profilePictureData);
                networkService.updateUser(activeUser);

                Image profileImage = new Image(selectedFile.toURI().toString());
                imgProfilePicture.setImage(profileImage);
                MyAlerts.showConfirmationAlert("Profile picture updated successfully.");
            } catch (IOException e) {
                MyAlerts.showErrorAlert("Failed to update profile picture.");
            }
        }
    }


    public void initApp(User user) {
        this.activeUser = user;
        welcomeLabel.setText("Welcome, " + activeUser.getUsername() + "!");
        loadUserProfileData(user);

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

    }

    protected void initUsers() {
        Iterable<User> users = networkService.getAllUsers();
        List<User> usersList = StreamSupport.stream(users.spliterator(), false)
                .filter(user -> !Objects.equals(user.getId(), activeUser.getId()))
                .collect(Collectors.toList());
        usersModel.setAll(usersList);
    }


    protected void initReceivedRequests() {
        Iterable<Friendship> friendships = networkService.getReceivedRequests(activeUser.getId());
        List<Friendship> friendRequestsList = StreamSupport.stream(friendships.spliterator(), false)
                .collect(Collectors.toList());
        receivedFriendRequestModel.setAll(friendRequestsList);
    }

    protected void initSentRequests() {
        Iterable<Friendship> friendships = networkService.getSentRequests(activeUser.getId());
        List<Friendship> friendRequestsList = StreamSupport.stream(friendships.spliterator(), false)
                .filter(friendship -> !friendship.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED))
                .collect(Collectors.toList());
        sentFriendRequestModel.setAll(friendRequestsList);
    }

    protected void initPageOfFriends() {
        int pageSizeFT = 3;
        Pageable pageable = new Pageable(currentPageFriendsTable, pageSizeFT);
        Page<User> page = networkService.findAllFriendsOnPage(pageable, activeUser.getId());

        int maxPage = Math.max(0, (int) (Math.ceil((double) page.getTotalNumberOfElements() / pageSizeFT) - 1));

        if (currentPageFriendsTable > maxPage) {
            currentPageFriendsTable = maxPage;
            page = networkService.findAllFriendsOnPage(new Pageable(currentPageFriendsTable, pageSizeFT), activeUser.getId());
        }

        int totalNumberOfFriendsTable = page.getTotalNumberOfElements();

        buttonPrevious.setDisable(currentPageFriendsTable == 0);
        buttonNext.setDisable((currentPageFriendsTable + 1) * pageSizeFT >= totalNumberOfFriendsTable);

        List<User> friends = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());

        friendsModel.setAll(friends);

        labelPage.setText((currentPageFriendsTable + 1) + "/" + (maxPage + 1));

    }

    protected void initUsersList() {
        Iterable<User> users = networkService.getAllUsers();
        List<String> usernames = StreamSupport.stream(users.spliterator(), false)
                .filter(user -> !user.getId().equals(activeUser.getId()))
                .map(User::getUsername)
                .collect(Collectors.toList());
        usersListModel.setAll(usernames);
    }


    protected void initMultiUserSelectionList() {
        for (User user : networkService.getAllUsers()) {
            if (!user.getId().equals(activeUser.getId())) {
                CheckBox checkBox = new CheckBox(user.getUsername());
                checkBoxList.add(checkBox);
            }
        }
        multiUserSelectionList.setItems(checkBoxList);
    }

    @FXML
    public void onNextPageFT() {
        currentPageFriendsTable++;
        initPageOfFriends();
    }

    @FXML
    public void onPreviousPageFT() {
        currentPageFriendsTable--;
        initPageOfFriends();
    }

    private void setupUserTable() {
        userColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        userColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        userColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        initUsers();
    }

    private void setupFriendTable() {
        friendColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
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

    private UUID getRecipientId(Friendship friendship) {
        if (friendship.isSender(friendship.getUser1())) {
            return friendship.getUser2();
        } else {
            return friendship.getUser1();
        }
    }


    @FXML
    private void handleRemoveFriend() {
        User user = friendsTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            try {
                networkService.removeFriendship(user.getId(), activeUser.getId());
                MyAlerts.showConfirmationAlert("Friend removed successfully");
            } catch (Exception e) {
                MyAlerts.showErrorAlert(e.getMessage());
            }
        } else {
            MyAlerts.showErrorAlert("Select a friend from Friends Table!");
        }
    }


    protected void sendNotification(Friendship friendship) {
        User sender = networkService.findUserById(friendship.getIdSender()).orElseThrow();
        String message = "You have a new friend request from: " + sender.getFirstName() + " " + sender.getLastName();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New friend request");
        alert.setHeaderText(message);
        alert.show();

        friendship.setNotificationSent(true);
        networkService.updateFriendship(friendship);
    }


    @FXML
    private void handleDeleteFriendRequest() {
        Friendship friendship = sentFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.removeFriendship(friendship.getId().getE1(), friendship.getId().getE2());
                MyAlerts.showConfirmationAlert("Friend request deleted");
            } catch (EntityMissingException | IllegalArgumentException e) {
                MyAlerts.showErrorAlert(e.getMessage());
            }
        } else {
            MyAlerts.showErrorAlert("Select a friend request from the table first!");
        }

    }

    @FXML
    private void handleAcceptFriendRequest() {
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.acceptFriendRequest(friendship.getId());
                MyAlerts.showConfirmationAlert("Friend request accepted");
            } catch (Exception e) {
                MyAlerts.showErrorAlert(e.getMessage());
            }
        } else {
            MyAlerts.showErrorAlert("Select a friend request first");
        }
    }

    @FXML
    private void handleDeclineFriendRequest() {
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if (friendship != null) {
            try {
                networkService.declineFriendRequest(friendship.getId());
                MyAlerts.showConfirmationAlert("Friend request declined");
            } catch (EntityMissingException | IllegalArgumentException e) {
                MyAlerts.showErrorAlert(e.getMessage());
            }
        } else {
            MyAlerts.showErrorAlert("Select a friend request first");
        }
    }

    @FXML
    private void handleViewProfile() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/profileView.fxml"));
                Parent root = loader.load();

                ProfileController controller = loader.getController();
                controller.setNetworkService(networkService);
                controller.setMessageService(messageService);
                controller.setUsers(networkService.findUserByUsername(user.getUsername()), networkService.findUserByUsername(activeUser.getUsername()));


                Stage stage = new Stage();
                stage.setTitle("User Profile");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                MyAlerts.showErrorAlert(e.getMessage());
            }
        }
    }


    @FXML
    private void handleSendMessage() throws IOException {
        String text = messageTextField.getText();
        if (text.isEmpty()) {
            MyAlerts.showErrorAlert("Type a message before sending.");
            return;
        }

        messageTextField.clear();
        List<User> recipients = new ArrayList<>();

        boolean multipleUsersSelected = multiUserSelectionList.getItems().stream().anyMatch(CheckBox::isSelected);
        String selectedUser = usersList.getSelectionModel().getSelectedItem();

        if (multipleUsersSelected && selectedUser != null) {
            MyAlerts.showErrorAlert("Please select either a single user or multiple users, not both!");
            clearSelections();
            return;
        }

        if (multipleUsersSelected) {
            multiUserSelectionList.getItems().stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .map(networkService::findUserByUsername)
                    .forEach(recipients::add);

            Message message = new Message(activeUser, recipients, text);
            message.setFrom(activeUser);
            messageService.addMessage(message);
            MyAlerts.showConfirmationAlert("Message successfully sent to selected users");
            checkBoxList.forEach(cb -> cb.setSelected(false));
        } else if (selectedUser != null) {
            handleOpenChat();
        } else {
            MyAlerts.showErrorAlert("Please select at least one recipient.");
        }

    }

    private void clearSelections() {
        multiUserSelectionList.getItems().forEach(checkBox -> checkBox.setSelected(false));
        usersList.getSelectionModel().clearSelection();
    }


    @FXML
    private void handleOpenChat() throws IOException {
        User chatWithUser = networkService.findUserByUsername(usersList.getSelectionModel().getSelectedItem());
        UIUtils.openChatController(messageService, activeUser, chatWithUser);
    }


    @FXML
    private void handleDeleteAccount(ActionEvent event){
        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("NO", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?", yesButton, noButton);
        alert.setTitle("Confirmation");

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                try {
                    networkService.deleteUser(activeUser.getId());
                    MyAlerts.showConfirmationAlert("Account deleted successfully");

                    Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                    currentStage.close();
                } catch (Exception e) {
                    MyAlerts.showErrorAlert("Error deleting account: " + e.getMessage());
                }
            } else {
                MyAlerts.showConfirmationAlert("We are glad you've changed your mind.");
            }
        });
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    @Override
    public void update() {
        initUsers();
        initSentRequests();
        initPageOfFriends();
        initReceivedRequests();
        initUsersList();

    }
}

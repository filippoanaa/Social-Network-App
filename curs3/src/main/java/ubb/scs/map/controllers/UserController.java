package ubb.scs.map.controllers;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.FriendshipValidator;
import ubb.scs.map.domain.validators.UserValidator;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.event.UserEvent;
import ubb.scs.map.observer.Observer;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.repository.database.FriendshipRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserController implements Observer<UserEvent> {
    static User activeUser;
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

    private NetworkService networkService;
    ObservableList<User> usersModel = FXCollections.observableArrayList();
    ObservableList<User> friendsModel = FXCollections.observableArrayList();
    ObservableList<Friendship> receivedFriendRequestModel = FXCollections.observableArrayList();
    ObservableList<Friendship> sentFriendRequestModel = FXCollections.observableArrayList();

    static void setActiveUser(User activeUser) {
        UserController.activeUser = activeUser;
    }

    private void initFriends() {
        Iterable<User> users = networkService.getAcceptedFriendRequests(activeUser.getId());
        List<User> usersList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        friendsModel.setAll(usersList);
        friendsTable.refresh();
    }


    private void initUsers() {
        Iterable<User> users = networkService.getAllUsers();
        List<User> usersList = StreamSupport.stream(users.spliterator(), false)
                .filter(user -> !Objects.equals(user.getId(), activeUser.getId()))
                .collect(Collectors.toList());
        usersModel.setAll(usersList);
        usersTable.refresh();
    }


    private void initReceivedRequests(){
        Iterable<Friendship> friendships = networkService.getReceivedRequests(activeUser.getId());
        List<Friendship> friendRequestsList = StreamSupport.stream(friendships.spliterator(), false)
                .collect(Collectors.toList());
        receivedFriendRequestModel.setAll(friendRequestsList);
        receivedFriendRequestsTable.refresh();
    }

    private void initSentRequests() {
        Iterable<Friendship> friendships = networkService.getSentRequests(activeUser.getId());
        List<Friendship> friendRequestsList = StreamSupport.stream(friendships.spliterator(), false)
                .filter(friendship -> !friendship.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED))
                .collect(Collectors.toList());
        sentFriendRequestModel.setAll(friendRequestsList);
        sentFriendRequestsTable.refresh();
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
        NetworkService usersService = new NetworkService(userRepository, friendshipRepository, userValidator, friendshipValidator);
        this.networkService = usersService;
        usersService.addObserver(this);

        friendColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        friendColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        friendsTable.setItems(friendsModel);
        initFriends();

        userColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        userColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        userColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usersTable.setItems(usersModel);
        initUsers();

        sentFriendRequestsFirstName.setCellValueFactory(cellData -> {
            UUID senderId = cellData.getValue().getIdSender(); // Expeditorul cererii
            UUID receiverId = cellData.getValue().getUser1().equals(senderId)
                    ? cellData.getValue().getUser2()
                    : cellData.getValue().getUser1(); // Destinatarul este celălalt utilizator
            Optional<User> receiver = networkService.findUserById(receiverId);
            return receiver.map(user -> new SimpleStringProperty(user.getFirstName()))
                    .orElse(new SimpleStringProperty("Unknown"));
        });
        sentFriendRequestsLastName.setCellValueFactory(cellData -> {
            UUID senderId = cellData.getValue().getIdSender(); // Expeditorul cererii
            UUID receiverId = cellData.getValue().getUser1().equals(senderId)
                    ? cellData.getValue().getUser2()
                    : cellData.getValue().getUser1(); // Destinatarul este celălalt utilizator
            Optional<User> receiver = networkService.findUserById(receiverId);
            return receiver.map(user -> new SimpleStringProperty(user.getLastName()))
                    .orElse(new SimpleStringProperty("Unknown"));
        });
        sentFriendRequestsDate.setCellValueFactory(new PropertyValueFactory<>("friendsFrom"));
        sentFriendRequestsStatus.setCellValueFactory(new PropertyValueFactory<>("friendshipStatus"));
        sentFriendRequestsTable.setItems(sentFriendRequestModel);
        initSentRequests();

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
        receivedFriendRequestsTable.setItems(receivedFriendRequestModel);
        initReceivedRequests();

        welcomeLabel.setText("Welcome, " + activeUser.getFirstName() + " " + activeUser.getLastName());
        friendsTable.refresh();
        usersTable.refresh();
        receivedFriendRequestsTable.refresh();
        sentFriendRequestsTable.refresh();
    }

    @Override
    public void update(UserEvent event) {
        initFriends();
        initUsers();
        initReceivedRequests();
    }

    public void handleRemoveFriend(ActionEvent event) {
        User user = (User) friendsTable.getSelectionModel().getSelectedItem();
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
            if(user != null){
                User newFriend = networkService.findUserByUsername(user.getUsername());
                Friendship friendship = new Friendship(activeUser.getId(), newFriend.getId());
                friendship.setSender(activeUser.getId());
                networkService.createFriendship(friendship);
                messageToUser.setText("Friend request sent to " + newFriend.getUsername());
                initSentRequests();
            }else{
                messageToUser.setText("Select an user from the available users table");
            }
        } catch (ValidationException | UserMissingException | EntityAlreadyExistsException e) {
            messageToUser.setText(e.getMessage());
        }
    }

    public void handleDeleteFriendRequest(ActionEvent event){
        Friendship friendship = sentFriendRequestsTable.getSelectionModel().getSelectedItem();
        if( friendship != null){
            try{
                networkService.removeFriendRequest(friendship.getId().getE1(), friendship.getId().getE2());
                messageToUser.setText("Friend request deleted");
                initSentRequests();
            }catch(EntityMissingException | IllegalArgumentException e){
                messageToUser.setText(e.getMessage());
            }
        }else{
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

    public void handleDeclineFriendRequest(ActionEvent event){
        Friendship friendship = receivedFriendRequestsTable.getSelectionModel().getSelectedItem();
        if(friendship != null){
            try{
                networkService.declineFriendRequest(friendship.getId());
                initReceivedRequests();
                initFriends();
                messageToUser.setText("Friend request declined");
            }catch(EntityMissingException | IllegalArgumentException e){
                messageToUser.setText(e.getMessage());
            }
        }else{
            messageToUser.setText("Select a friend request first");
        }
    }

//    public void handleDeleteAccount(ActionEvent event) throws IOException {
//        try {

//            networkService.deleteUser(activeUser.getId());
//
//            messageToUser.setText("Account deleted successfully");
//
//
//            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
//            currentStage.close();
//
//            //openLogInWindow();
//
//        } catch (Exception e) {
//            messageToUser.setText("Error deleting account: " + e.getMessage());
//        }
//    }

//    private void openLogInWindow() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/log-in.fxml"));
//            Parent root = loader.load();
//
//            Stage logInStage = new Stage();
//            logInStage.setScene(new Scene(root));
//            logInStage.setTitle("Log In");
//            logInStage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error loading log-in window", e);
//        }
//    }


}

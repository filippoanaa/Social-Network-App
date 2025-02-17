package ubb.scs.map.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.utils.MyAlerts;
import ubb.scs.map.utils.Observer;
import ubb.scs.map.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageController implements Observer {
    @FXML
    private Label chattingWithLbl;
    @FXML
    private TextField messageTextField;
    @FXML
    private ListView<Message> messagesList;

    private MessageService messageService;
    private User connectedUser;
    private User currentUser;
    private final ObservableList<Message> messagesModel = FXCollections.observableArrayList();

    public void setUsers(User connectedUser, User currentUser) {
        this.connectedUser = connectedUser;
        this.currentUser = currentUser;
        chattingWithLbl.setText("Chatting with: " + currentUser.getUsername());
        initMessages();
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
        messageService.addObserver(this);
    }

    @FXML
    public void initialize() {
        messagesList.setItems(messagesModel);
    }

    public void handleSendMessage() {
        String text = messageTextField.getText();
        if (text.isEmpty()) {
            MyAlerts.showErrorAlert("Type a message before sending.");
            return;
        }
        messageTextField.clear();
        List<User> to = new ArrayList<>();
        to.add(currentUser);

        Message message = new Message(connectedUser, to, text);

        Message selectedMessage = messagesList.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            message.setReply(selectedMessage);
            messagesList.getSelectionModel().clearSelection();
        }
        message.setFrom(connectedUser);
        messageService.addMessage(message);

    }

    @FXML
    private void handleDeleteConversation() {
        messageService.deleteConversation(connectedUser.getId(), currentUser.getId());
        MyAlerts.showConfirmationAlert("Conversation deleted successfully");

    }

    private void initMessages() {
        Iterable<Message> messages = messageService.getMessagesBetween(connectedUser.getId(), currentUser.getId());
        List<Message> messagesLst = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        UIUtils.formatMessages(messagesLst, messagesModel, messagesList, connectedUser);
    }

    @Override
    public void update() {
        initMessages();
    }
}
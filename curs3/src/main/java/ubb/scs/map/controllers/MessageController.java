package ubb.scs.map.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.service.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageController {
    @FXML
    private Button buttonSendMessage;
    @FXML
    private TextField messageTextField;
    @FXML
    private ListView<Message> messagesList;

    private MessageService messageService;
    private User connectedUser;
    private User currentUser;
    private ObservableList<Message> messagesModel = FXCollections.observableArrayList();

    public void setUsers(User connectedUser, User currentUser) {
        this.connectedUser = connectedUser;
        this.currentUser = currentUser;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @FXML
    public void initialize() {
        messagesList.setItems(messagesModel);
    }

    public void handleSendMessage(){
        String text = messageTextField.getText();
        if (!text.isEmpty()) {
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
            messagesModel.add(message);
        }
        messageTextField.clear();
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
                    if (message.getFrom().equals(connectedUser)) {
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

    public void handleOpenChat(){
        Iterable<Message> messages = messageService.getMessagesBetween(connectedUser.getId(), currentUser.getId());
        List<Message> messagesList = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        messagesModel.setAll(messagesList);
        formatMessages(messagesList);
    }
}
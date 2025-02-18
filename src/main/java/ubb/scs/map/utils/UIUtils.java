package ubb.scs.map.utils;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ubb.scs.map.controllers.MessageController;
import ubb.scs.map.controllers.UserController;
import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;
import java.util.List;

public class UIUtils {
    public static UserController openAccountSettings(NetworkService networkService, MessageService messageService, User user) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(UIUtils.class.getResource("/views/userView.fxml"));
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
        return userController;
    }

    public static void openChatController(MessageService messageService, User activeUser, User chatWithUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource("/views/chatView.fxml"));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.setTitle("Chat");

        MessageController messageController = loader.getController();
        messageController.setMessageService(messageService);
        messageController.setUsers(activeUser, chatWithUser);
        newStage.show();

    }
    public static void formatMessages(List<Message> messages, ObservableList<Message> messagesModel, ListView<Message> messagesListView, User activeUser) {
        messagesModel.setAll(messages);
        messagesListView.setCellFactory(listView -> new ListCell<>() {
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



}

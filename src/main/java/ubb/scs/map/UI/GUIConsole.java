package ubb.scs.map.UI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ubb.scs.map.controllers.LogInController;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.validators.FriendshipValidator;
import ubb.scs.map.domain.validators.UserValidator;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.FriendshipPagingRepository;
import ubb.scs.map.repository.database.FriendshipRepositoryDatabase;
import ubb.scs.map.repository.database.MessageRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;
import ubb.scs.map.service.MessageService;
import ubb.scs.map.service.NetworkService;
import ubb.scs.map.utils.MyAlerts;

import java.io.IOException;

public class GUIConsole extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage){
        try {

            final String url = "jdbc:postgresql://localhost:5432/social_network";
            final String username = "postgres";
            final String password = "postgres";

            UserRepositoryDatabase userRepository = new UserRepositoryDatabase(url, username, password);
          FriendshipPagingRepository friendshipRepository = new FriendshipRepositoryDatabase(url, username, password);
            MessageRepositoryDatabase messageRepository = new MessageRepositoryDatabase(url, username, password, userRepository);
            Validator<User> userValidator = new UserValidator();
            Validator<Friendship> friendshipValidator = new FriendshipValidator();
            NetworkService networkService = new NetworkService(userRepository, friendshipRepository, userValidator, friendshipValidator);
            MessageService messageService = new MessageService(messageRepository, userRepository);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/log-in.fxml"));
            GridPane logInLayout = loader.load();
            stage.setScene(new Scene(logInLayout));
            stage.setTitle("Log in");

            LogInController controller = loader.getController();
            controller.setNetworkService(networkService);
            controller.setMessageService(messageService);
            stage.show();
        } catch (IOException e) {
            MyAlerts.showErrorAlert(e.getMessage());
        }

    }


}

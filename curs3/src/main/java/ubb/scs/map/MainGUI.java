package ubb.scs.map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ubb.scs.map.controllers.LogInController;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.validators.FriendshipValidator;
import ubb.scs.map.domain.validators.UserValidator;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.repository.database.FriendshipRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;
import ubb.scs.map.service.FriendshipService;
import ubb.scs.map.service.NetworkService;

import java.io.IOException;

public class MainGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/log-in.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Log In");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading FXML file", e);
        }

    }

}

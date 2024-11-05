package ubb.scs.map;

import ubb.scs.map.UI.UIConsole;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.validators.FriendshipValidator;
import ubb.scs.map.domain.validators.UserValidator;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.repository.file.FriendshipRepository;
import ubb.scs.map.repository.file.UserRepository;
import ubb.scs.map.service.FriendshipService;
import ubb.scs.map.service.UserService;


public class Main {
    public static void main(String[] args) {
        Validator<User> userValidator = new UserValidator();
        Repository<String, User> userRepository = new UserRepository("D:\\Semestrul 3\\MAP\\teme-labs\\SocialNetwork\\curs3\\data\\users.txt");
        Repository<Tuple<String, String>, Friendship> friendshipRepository = new FriendshipRepository("D:\\Semestrul 3\\MAP\\teme-labs\\SocialNetwork\\curs3\\data\\friendships.txt");
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        UserService userService = new UserService(userRepository,friendshipRepository, userValidator);
        FriendshipService friendshipService = new FriendshipService(userRepository,friendshipRepository,friendshipValidator);
        UIConsole console = new UIConsole(userService, friendshipService);
        console.run();


    }
}
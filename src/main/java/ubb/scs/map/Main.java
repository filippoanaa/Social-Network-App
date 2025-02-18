//package ubb.scs.map;
//
//import ubb.scs.map.domain.Friendship;
//import ubb.scs.map.domain.Tuple;
//import ubb.scs.map.domain.User;
//import ubb.scs.map.domain.validators.FriendshipValidator;
//import ubb.scs.map.domain.validators.UserValidator;
//import ubb.scs.map.domain.validators.Validator;
//import ubb.scs.map.repository.Repository;
//import ubb.scs.map.repository.database.FriendshipRepositoryDatabase;
//import ubb.scs.map.repository.database.UserRepositoryDatabase;
//import ubb.scs.map.repository.file.FriendshipRepository;
//import ubb.scs.map.repository.file.UserRepository;
//import ubb.scs.map.service.FriendshipService;
//import ubb.scs.map.service.NetworkService;
//import ubb.scs.map.service.NetworkService;
//
//import java.util.UUID;
//
//
//public class Main {
//    public static void main(String[] args) {
//        Validator<User> userValidator = new UserValidator();
//        Repository<UUID, User> userRepository = new UserRepositoryDatabase("jdbc:postgresql://localhost:5432/social_network", "postgres","postgres");
//        Repository<Tuple<UUID, UUID>, Friendship> friendshipRepository = new FriendshipRepositoryDatabase("jdbc:postgresql://localhost:5432/social_network", "postgres","postgres");
//        Validator<Friendship> friendshipValidator = new FriendshipValidator();
//        NetworkService userService = new NetworkService(userRepository,friendshipRepository, userValidator, friendshipValidator);
//        FriendshipService friendshipService = new FriendshipService(userService);
//        UIConsole console = new UIConsole(userService, friendshipService);
//        console.run();
//
//
//    }
//}
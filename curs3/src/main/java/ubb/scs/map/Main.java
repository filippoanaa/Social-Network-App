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
import ubb.scs.map.repository.memory.InMemoryRepository;
import ubb.scs.map.service.UsersService;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Validator<User> userValidator = new UserValidator();
        Repository<String, User> repoUser = new UserRepository("D:\\Semestrul 3\\MAP\\teme-labs\\SocialNetwork\\curs3\\data\\utilizatori.txt");
        Repository<Tuple<String, String>, Friendship> friendshipRepository = new FriendshipRepository("D:\\Semestrul 3\\MAP\\teme-labs\\SocialNetwork\\curs3\\data\\friendships.txt");
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        UsersService usersService = new UsersService(repoUser,userValidator,friendshipRepository,friendshipValidator);
        UIConsole console = new UIConsole(usersService);
        console.run();


    }
}
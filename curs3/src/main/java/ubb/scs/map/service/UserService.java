package ubb.scs.map.service;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class UserService {
    private final Repository<String, User> userRepository;
    private final Repository<Tuple<String, String>, Friendship> friendshipRepository;
    private final Validator<User> userValidator;

    public UserService(Repository<String, User> userRepository, Repository<Tuple<String, String>, Friendship> friendshipRepository, Validator<User> userValidator) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidator = userValidator;
    }

    /**
     * Method that adds a user
     *
     * @param username  String
     * @param firstName String
     * @param lastName  String
     * @throws ValidationException if the user is not valid
     */
    public void addUser(String username, String firstName, String lastName) throws ValidationException {
        User user = new User(username, firstName, lastName);
        userValidator.validate(user);
        userRepository.save(user);

    }

    /**
     * Method that deletes a user by username
     *
     * @param username String
     */
    public User deleteUser(String username) {
        User user = userRepository.findOne(username)
                .orElseThrow(() -> new UserMissingException(username));

        List<Friendship> toDelete = new ArrayList<>();
        friendshipRepository.findAll().forEach(friendship -> {
            if (friendship.getId().getE1().equals(username) || friendship.getId().getE2().equals(username)) {
                toDelete.add(friendship);
            }
        });


        toDelete.forEach(friendship -> friendshipRepository.delete(friendship.getId()));
        user.getFriends().forEach(friend -> friend.removeFriend(user));
        userRepository.delete(user.getId());
        return user;

    }
}






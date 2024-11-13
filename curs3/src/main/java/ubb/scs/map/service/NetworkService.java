package ubb.scs.map.service;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.event.UserEvent;
import ubb.scs.map.observer.Observable;
import ubb.scs.map.observer.Observer;
import ubb.scs.map.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class NetworkService{
    private final Repository<String, User> userRepository;
    private final Repository<Tuple<String, String>, Friendship> friendshipRepository;
    private final Validator<User> userValidator;
    private final Validator<Friendship> friendshipValidator;
    //private final List<Observer<UserEvent>> observers;

    public NetworkService(Repository<String, User> userRepository, Repository<Tuple<String, String>, Friendship> friendshipRepository, Validator<User> userValidator, Validator<Friendship> friendshipValidator) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidator = userValidator;
        this.friendshipValidator = friendshipValidator;
    }

    /**
     * Method that adds a user
     *
     * @param username  String
     * @param firstName String
     * @param lastName  String
     * @throws ValidationException if the user is not valid
     */
    public void addUser(String username, String firstName, String lastName, String password) throws ValidationException {
        User user = new User(username, firstName, lastName, password);
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

    /**
     * Modifies an existing User
     * @param username String
     * @param password String
     * @param firstName String
     * @param lastName String
     * @throws EntityMissingException if the user does not exist
     * @throws ValidationException if the given data is not valid for a user to be created
     */
    public void updateUser(String username, String firstName, String lastName, String password) throws EntityMissingException, ValidationException{
        User user = new User(username, firstName, lastName, password);
        userValidator.validate(user);
        userRepository.update(user);
    }

    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }

    /**
     * Method that checks if the users exist, and if the friendship doesn't already exist
     *
     * @param username1 String
     * @param username2 String
     * @throws UserMissingException         if one or both of the users don t exist
     * @throws EntityAlreadyExistsException if the friendship already exists
     */
    private void friendshipsUsersChecking(String username1, String username2) throws UserMissingException {
        boolean user1Exists = userRepository.exists(username1);
        boolean user2Exists = userRepository.exists(username2);

        if (!user1Exists && !user2Exists) {
            throw new UserMissingException("Both users " + username1 + " and " + username2 + " do not exist!");
        } else if (!user1Exists) {
            throw new UserMissingException("User " + username1 + " does not exist!");
        } else if (!user2Exists) {
            throw new UserMissingException("User " + username2 + " does not exist!");
        }


    }


    /**
     * Method that adds a friendship.
     *
     * @param username1 String
     * @param username2 String
     * @throws ValidationException          if the friendship data is invalid.
     * @throws UserMissingException         if one or both users do not exist.
     * @throws EntityAlreadyExistsException if the friendship already exists.
     */
    public void addFriendship(String username1, String username2) throws ValidationException, UserMissingException, EntityAlreadyExistsException {
        Friendship friendship = new Friendship(username1, username2);
        friendshipValidator.validate(friendship);
        friendshipsUsersChecking(username1, username2);
        User user1 = userRepository.findOne(username1)
                .orElseThrow(() -> new UserMissingException(username1));
        User user2 = userRepository.findOne(username2)
                .orElseThrow(() -> new UserMissingException(username2));
        user1.addFriend(user2);
        user2.addFriend(user1);
        friendshipRepository.save(friendship);

    }
    public Iterable<Friendship> getAllFriendships(){
        return friendshipRepository.findAll();
    }

    /**
     * method that removes a friendship
     *
     * @param username1 String
     * @param username2 String
     * @throws ValidationException          if the friendship data is invalid.
     * @throws UserMissingException         if one or both users do not exist.
     * @throws EntityAlreadyExistsException if the friendship already exists.
     */
    public void removeFriendship(String username1, String username2) throws ValidationException, UserMissingException, EntityAlreadyExistsException {
        Tuple<String, String> frId = new Tuple<>(username1, username2);
        Tuple<String, String> reverseFrId = new Tuple<>(username2, username1);
        Friendship friendship = friendshipRepository.findOne(frId)
                .orElseThrow(() -> new UserMissingException(frId.toString()));
        if (friendship == null) {
            friendship = friendshipRepository.findOne(reverseFrId)
                    .orElseThrow(() -> new UserMissingException(reverseFrId.toString()));
        }
        friendshipsUsersChecking(username1, username2);
        User user1 = userRepository.findOne(username1)
                .orElseThrow(() -> new UserMissingException(username1));
        User user2 = userRepository.findOne(username2)
                .orElseThrow(() -> new UserMissingException(username2));
        user1.removeFriend(user2);
        user2.removeFriend(user1);
        friendshipRepository.delete(friendship.getId());
    }


    /**
     * Verifies if the password matches the users' password
     * @param username  a String given by the user
     * @param password a String given by the user
     * @return true if the password given by the user is equal with the password of the user
     */
    public boolean verifyCredentials(String username, String password) {
        Optional<User> user = userRepository.findOne(username);
        return  user.isPresent() && Objects.equals(user.get().getPassword(), password);
    }

    public Optional<User> findUser(String username) {
        return userRepository.findOne(username);
    }

    public Iterable<User> getFriendsOfUser(String username) {
        User user = userRepository.findOne(username).orElseThrow(() -> new UserMissingException(username));
        List<User> friends = new ArrayList<>();
        for (Friendship friendship : friendshipRepository.findAll()) {
            if (friendship.getId().getE1().equals(username)) {
                userRepository.findOne(friendship.getId().getE2()).ifPresent(friends::add);
            } else if (friendship.getId().getE2().equals(username)) {
                userRepository.findOne(friendship.getId().getE1()).ifPresent(friends::add);
            }
        }
        return friends;

    }
//
//    @Override
//    public void addObserver(Observer<UserEvent> observer) {
//
//    }
//
//    @Override
//    public void removeObserver(Observer<UserEvent> observer) {
//
//    }
//
//    @Override
//    public void notifyObservers(UserEvent event) {
//
//    }
}






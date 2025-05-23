package ubb.scs.map.service;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.FriendshipPagingRepository;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.utils.Observable;
import ubb.scs.map.utils.Page;
import ubb.scs.map.utils.Pageable;
import ubb.scs.map.utils.PasswordUtils;
import java.time.LocalDateTime;
import java.util.*;

public class NetworkService extends Observable {
    private final Repository<UUID, User> userRepository;
    private final FriendshipPagingRepository friendshipRepository;
    private final Validator<User> userValidator;
    private final Validator<Friendship> friendshipValidator;

    public NetworkService(Repository<UUID, User> userRepository, FriendshipPagingRepository friendshipRepository, Validator<User> userValidator, Validator<Friendship> friendshipValidator) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidator = userValidator;
        this.friendshipValidator = friendshipValidator;
    }

    public boolean verifyCredentials(String username, String password) {
        User user = findUserByUsername(username);
        return user != null && PasswordUtils.checkPassword(password, user.getPassword());
    }


    public User findUserByUsername(String username){
        Iterable<User> users = userRepository.findAll();
        for(User user : users){
            if(user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    public Optional<User> findUserById(UUID id){
        return userRepository.findOne(id);
    }



    public void addUser(User user) throws ValidationException, EntityAlreadyExistsException {
        userValidator.validate(user);
        if(findUserByUsername(user.getUsername()) != null)
            throw new UserAlreadyExistsException("User with username: " + user.getUsername() + " already exists!");

        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        notifyObservers();
    }

    public void deleteUser(UUID id) throws EntityMissingException{
        User user = findUserById(id).orElse(null);
        if (user != null) {
            List<Friendship> toDelete = new ArrayList<>();
            friendshipRepository.findAll().forEach(friendship -> {
                if (friendship.getId().getE1().equals(id) || friendship.getId().getE2().equals(id)){
                    toDelete.add(friendship);
                }
            });
            toDelete.forEach(friendship -> friendshipRepository.delete(friendship.getId()));
            userRepository.delete(user.getId());
            notifyObservers();
        }else{
            throw new EntityMissingException("User not found!");
        }

    }

    public void updateUser(User user) throws ValidationException {
        userValidator.validate(user);
        userRepository.update(user);
        notifyObservers();
    }

    public void updateFriendship(Friendship friendship){
        friendshipRepository.update(friendship);
        notifyObservers();
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }


    private void friendshipsUsersChecking(String username1, String username2) throws UserMissingException {
        User u1 = findUserByUsername(username1);
        User u2 = findUserByUsername(username2);
        boolean user1Exists = userRepository.exists(u1.getId());
        boolean user2Exists = userRepository.exists(u2.getId());

        if (!user1Exists && !user2Exists) {
            throw new UserMissingException("Both users " + username1 + " and " + username2 + " do not exist!");
        } else if (!user1Exists) {
            throw new UserMissingException("User " + username1 + " does not exist!");
        } else if (!user2Exists) {
            throw new UserMissingException("User " + username2 + " does not exist!");
        }
    }

    public void createFriendship(Friendship friendship) throws ValidationException, UserMissingException, EntityAlreadyExistsException {
        friendshipValidator.validate(friendship);
        if (friendshipRepository.exists(friendship.getId())) {
            throw new EntityAlreadyExistsException("A friend request has already been sent to this user");
        }

        friendshipRepository.save(friendship);
        notifyObservers();
    }


    public Iterable<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
    }


    public void removeFriendship(UUID id1, UUID id2)throws EntityMissingException{
        User user1 = findUserById(id1).orElseThrow();
        User user2 = findUserById(id2).orElseThrow();

        friendshipsUsersChecking(user1.getUsername(), user2.getUsername());


        UUID idUser1 = user1.getId();
        UUID idUser2 = user2.getId();

        Tuple<UUID, UUID> friendshipId1 = new Tuple<>(idUser1, idUser2);
        Tuple<UUID, UUID> friendshipId2 = new Tuple<>(idUser2, idUser1);


        Friendship friendship = friendshipRepository.findOne(friendshipId1).orElse(
                friendshipRepository.findOne(friendshipId2).orElseThrow(() -> new EntityMissingException("Friendship does not exist!"))
        );

        user1.removeFriend(user2);
        user2.removeFriend(user1);
        friendshipRepository.delete(friendship.getId());
        notifyObservers();
    }

    public void removeFriendRequest(UUID id1, UUID id2){
        Friendship friendship = friendshipRepository.findOne(new Tuple<>(id1, id2)).orElseThrow();
        if(friendship.getFriendshipStatus() != FriendshipStatus.PENDING)
            throw new IllegalArgumentException("You cannot delete this friend request anymore!It's already declined.");
        else {
            removeFriendship(id1, id2);
            notifyObservers();
        }
    }


    public List<User> getAcceptedFriendRequests(UUID id) {
        List<User> friends = new ArrayList<>();
        for (Friendship friendship : friendshipRepository.findAll()) {
            UUID user1 = friendship.getUser1();
            UUID user2 = friendship.getUser2();
            if (user1.equals(id) && friendship.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED)) {
                userRepository.findOne(user2).ifPresent(friends::add);
            } else if (user2.equals(id) && friendship.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED)) {
                userRepository.findOne(user1).ifPresent(friends::add);
            }
        }
        return friends;
    }


    public List<Friendship> getSentRequests(UUID id) {
        List<Friendship> friendships = new ArrayList<>();
        for (Friendship friendship : friendshipRepository.findAll() ) {
            UUID user1 = friendship.getUser1();
            UUID user2 = friendship.getUser2();
            if (user1.equals(id) && friendship.isSender(user1) ) {
                friendships.add(friendship);
            } else if (user2.equals(id) && friendship.isSender(user2)) {
                friendships.add(friendship);
            }
        }
        return friendships;
    }

    public List<Friendship> getReceivedRequests(UUID id){
        List<Friendship> friendships = new ArrayList<>();
        for (Friendship friendship : friendshipRepository.findAll() ) {
            UUID user1 = friendship.getUser1();
            UUID user2 = friendship.getUser2();
            if (user1.equals(id) && !friendship.isSender(user1) && friendship.getFriendshipStatus().equals(FriendshipStatus.PENDING)) {
                friendships.add(friendship);
            } else if (user2.equals(id) && !friendship.isSender(user2)  && friendship.getFriendshipStatus().equals(FriendshipStatus.PENDING)) {
                friendships.add(friendship);
            }
        }
        return friendships;
    }


    public void  acceptFriendRequest(Tuple<UUID, UUID> id) throws IllegalArgumentException{
        Friendship friendship = friendshipRepository.findOne(id).orElseThrow();
        if(!friendship.getFriendshipStatus().equals(FriendshipStatus.PENDING)){
            throw new IllegalArgumentException("You cannot accept this friend request!");
        }
        friendship.setFriendshipStatus(FriendshipStatus.ACCEPTED);
        friendship.setFriendsFrom(LocalDateTime.now());

        friendshipRepository.update(friendship);

        User user1 = findUserById(friendship.getId().getE1()).orElseThrow();
        User user2 = findUserById(friendship.getId().getE2()).orElseThrow();

        user1.addFriend(user2);
        user2.addFriend(user1);
        notifyObservers();
    }

    public void declineFriendRequest(Tuple<UUID, UUID> id){
        Friendship friendship = friendshipRepository.findOne(id).orElseThrow();
        friendshipRepository.delete(friendship.getId());
        notifyObservers();
    }

    public Page<User> findAllFriendsOnPage(Pageable pageable, UUID id) {
        Page<Friendship> friendsPage = friendshipRepository.findAllOnFriendsOnPage(pageable, id);

        List<User> users = new ArrayList<>();

        for (Friendship friendship : friendsPage.getElementsOnPage()) {
            UUID user1 = friendship.getUser1();
            UUID user2 = friendship.getUser2();

            if (user1.equals(id)) {
                userRepository.findOne(user2).ifPresent(users::add);
            } else if (user2.equals(id)) {
                userRepository.findOne(user1).ifPresent(users::add);
            }
        }

        return new Page<>(users, friendsPage.getTotalNumberOfElements());
    }


    public int getFriendsCount(UUID id) {
        return getAcceptedFriendRequests(id).size();
    }

    public Optional<Friendship> findFriendship(UUID id1, UUID id2) {
        return friendshipRepository.findOne(new Tuple<>(id1, id2));
    }
}



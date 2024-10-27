package ubb.scs.map.service;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersService {
    private final Repository<String, User> userRepository;
    private final Validator<User> userValidator;
    private final Repository<Tuple<String, String>, Friendship> friendshipRepository;
    private final Validator<Friendship> friendshipValidator;

    public UsersService(Repository<String, User> userRepository, Validator<User> userValidator, Repository<Tuple<String, String>, Friendship> friendshipRepository, Validator<Friendship> friendshipValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.friendshipRepository = friendshipRepository;
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
    public int deleteUser(String username) {
        User user = userRepository.findOne(username);

        List<Friendship> friendshipsToDelete = new ArrayList<>();
        for (Friendship friendship : friendshipRepository.findAll()) {
            if (friendship.getId().contains(user.getId())) {
                friendshipsToDelete.add(friendship);
            }
        }

        for (Friendship friendship : friendshipsToDelete) {
            String friendId = friendship.getId().getE1().equals(user.getId()) ? friendship.getId().getE2() : friendship.getId().getE1();
            User friend = userRepository.findOne(friendId);
            friend.removeFriend(user);
            user.removeFriend(friend);
            friendshipRepository.delete(friendship.getId());
        }

        userRepository.delete(username);

        return 0;

    }




    /**
     * Method that checks if the users exist, and if the friendship doesn't already exist
     *
     * @param username1 String
     * @param username2 String
     * @throws UserMissingException         if one or both of the users don t exist
     * @throws EntityAlreadyExistsException if the friendship already exists
     */
    private void friendshipsUsersChecking(String username1, String username2) throws UserMissingException, EntityAlreadyExistsException {
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
        Friendship friendship = new Friendship(new Tuple<>(username1, username2));
        friendshipValidator.validate(friendship);
        friendshipsUsersChecking(username1, username2);
        User user1 = userRepository.findOne(username1);
        User user2 = userRepository.findOne(username2);
        user1.addFriend(user2);
        user2.addFriend(user1);
        friendshipRepository.save(friendship);

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
        Friendship friendship = friendshipRepository.findOne(frId);
        if (friendship == null) {
            friendship = friendshipRepository.findOne(reverseFrId);
        }
        friendshipsUsersChecking(username1, username2);
        User user1 = userRepository.findOne(username1);
        User user2 = userRepository.findOne(username2);
        user1.removeFriend(user2);
        user2.removeFriend(user1);
        friendshipRepository.delete(friendship.getId());

    }

    /**
     * Method that refreshes the list of friends for each user
     */
    public void refreshFriends() {
        for (Friendship friendship : friendshipRepository.findAll()) {
            User user1 = userRepository.findOne(friendship.getId().getE1());
            User user2 = userRepository.findOne(friendship.getId().getE2());

            if (!user1.getFriends().contains(user2)) {
                user1.addFriend(user2);
            }

            if (!user2.getFriends().contains(user1)) {
                user2.addFriend(user1);
            }
        }


    }

    private Map<String, Integer> visited = new HashMap<>();

    /**
     * DFS to mark all users connected to the given user.
     *
     * @param user        String
     * @param communityId int, the ID of the community being assigned to connected users
     */
    private void DFS(User user, int communityId) {
        visited.put(user.getId(), communityId);

        for (User friend : user.getFriends()) {
            if (!visited.containsKey(friend.getId())) {
                DFS(friend, communityId);
            }
        }
    }

    /**
     * Calculates the number of communities (connected components) in the network.
     * A community is defined as a group of users who are directly or indirectly
     * connected through friendships.
     *
     * @return the number of communities found in the user network, int
     */
    public int numberOfCommunities() {
        int numberOfConnectedComponents = 0;
        visited.clear();
        for (User user : userRepository.findAll()) {
            if (!visited.containsKey(user.getId())) {
                numberOfConnectedComponents++;
                DFS(user, numberOfConnectedComponents);
            }
        }
        return numberOfConnectedComponents;
    }


    /**
     * Finds and returns the  community with the most users
     *
     * @return List<User>
     */
    public List<User> getTheMostFriendlyCommunity() {
        int maxFriends = -1;
        int numberOfCommunities = numberOfCommunities();
        int[] freq = new int[numberOfCommunities + 1];
        int communityIndex = 0;
        List<User> mostFriendlyCommunity = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            int communityId = visited.get(user.getId());
            freq[communityId]++;
        }

        for (int i = 0; i < numberOfCommunities; i++) {
            if (freq[i] > maxFriends) {
                maxFriends = freq[i];
                communityIndex = i;
            }
        }

        for (User u : userRepository.findAll()) {
            if (visited.get(u.getId()) == communityIndex) {
                mostFriendlyCommunity.add(u);
            }
        }
        return mostFriendlyCommunity;
    }


}
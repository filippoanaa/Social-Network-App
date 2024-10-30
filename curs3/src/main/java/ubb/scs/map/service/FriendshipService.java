package ubb.scs.map.service;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.domain.validators.Validator;
import ubb.scs.map.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendshipService extends Service {

    private final Validator<Friendship> friendshipValidator;
    private final Map<String, Integer> visitedUsers = new HashMap<>();

    public FriendshipService(Repository<String, User> userRepository, Repository<Tuple<String, String>, Friendship> friendshipRepository, Validator<Friendship> friendshipValidator) {
        super(userRepository, friendshipRepository);
        this.friendshipValidator = friendshipValidator;
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
     * Method that reinitializes the list of friends for each user
     */
    public void refreshFriends() {
        friendshipRepository.findAll().forEach(friendship -> {
            User user1 = userRepository.findOne(friendship.getId().getE1()).orElse(null);
            User user2 = userRepository.findOne(friendship.getId().getE2()).orElse(null);
            if (user1 != null && user2 != null) {
                user1.addFriend(user2);
                user2.addFriend(user1);
            }
        });
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
        User user1 = userRepository.findOne(username1)
                .orElseThrow(() -> new UserMissingException(username1));
        User user2 = userRepository.findOne(username2)
                .orElseThrow(() -> new UserMissingException(username2));
        user1.addFriend(user2);
        user2.addFriend(user1);
        friendshipRepository.save(friendship);
        refreshFriends();
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
        refreshFriends();
    }


    /**
     * DFS to mark all users connected to the given user.
     *
     * @param user        String
     * @param communityId int, the ID of the community being assigned to connected users
     */
    private void DFS(User user, int communityId) {
        visitedUsers.put(user.getId(), communityId);
        for (User friend : user.getFriends()) {
            if (!visitedUsers.containsKey(friend.getId())) {
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
        visitedUsers.clear();
        refreshFriends();
        for (User user : userRepository.findAll()) {
            if (!visitedUsers.containsKey(user.getId())) {
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
        int maxCommunityMembers = -1;
        int numberOfCommunities = this.numberOfCommunities();
        int[] freq = new int[numberOfCommunities + 1];
        int communityIndex = 1;
        List<User> mostFriendlyCommunity = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            int communityId = visitedUsers.get(user.getId());
            freq[communityId]++;
        }

        for (int i = 1; i <= numberOfCommunities; i++) {
            if (freq[i] > maxCommunityMembers) {
                maxCommunityMembers = freq[i];
                communityIndex = i;
            }
        }

        for (User u : userRepository.findAll()) {
            if (visitedUsers.get(u.getId()) == communityIndex) {
                mostFriendlyCommunity.add(u);
            }
        }
        return mostFriendlyCommunity;
    }


}

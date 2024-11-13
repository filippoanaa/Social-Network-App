package ubb.scs.map.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendshipService {

    NetworkService NetworkService;
    private final Map<String, List<String>> adjacencyList = new HashMap<>();

    public FriendshipService(NetworkService NetworkService) {
        this.NetworkService = NetworkService;
    }



    private void DFS(String username, HashMap<String, Boolean> visitedUsers, List<String> community) {
        visitedUsers.put(username, true);
        community.add(username);
        if (adjacencyList.containsKey(username)) {
            adjacencyList.get(username).stream()
                    .filter(x -> !visitedUsers.containsKey(x))
                    .forEach(x -> DFS(x, visitedUsers, community));
        }
    }

    private void refreshFriendsList() {
        adjacencyList.clear();
        NetworkService.getAllUsers().forEach(user -> {
            List<String> friends = new ArrayList<>();
            NetworkService.getAllFriendships().forEach(friendship -> {
                if (friendship.getId().getE1().equals(user.getId()))
                    friends.add(friendship.getId().getE2());
                if (friendship.getId().getE2().equals(user.getId()))
                    friends.add(friendship.getId().getE1());
            });
            adjacencyList.putIfAbsent(user.getId(), friends);
        });

    }

    public int numberOfCommunities() {
        refreshFriendsList();
        HashMap<String, Boolean> visitedUsers = new HashMap<>();
        int numberOfCommunities = 0;

        for (String username : adjacencyList.keySet()) {
            if (!visitedUsers.getOrDefault(username, false)) {
                List<String> community = new ArrayList<>();
                DFS(username, visitedUsers, community);
                numberOfCommunities++;
            }
        }
        return numberOfCommunities;
    }


    public List<String> mostFriendlyCommunity() {
        refreshFriendsList();
        HashMap<String, Boolean> visitedUsers = new HashMap<>();
        List<String> largestCommunity = new ArrayList<>();

        for (String username : adjacencyList.keySet()) {
            if (!visitedUsers.getOrDefault(username, false)) {
                List<String> community = new ArrayList<>();
                DFS(username, visitedUsers, community);
                if (community.size() > largestCommunity.size()) {
                    largestCommunity = new ArrayList<>(community);
                }
            }
        }
        return largestCommunity;
    }


}

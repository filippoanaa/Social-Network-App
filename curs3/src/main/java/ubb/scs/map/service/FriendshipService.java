package ubb.scs.map.service;

import java.util.*;

public class FriendshipService {

    NetworkService NetworkService;
    private final Map<UUID, List<UUID>> adjacencyList = new HashMap<>();

    public FriendshipService(NetworkService NetworkService) {
        this.NetworkService = NetworkService;
    }



    private void DFS(UUID id, HashMap<UUID, Boolean> visitedUsers, List<UUID> community) {
        visitedUsers.put(id, true);
        community.add(id);
        if (adjacencyList.containsKey(id)) {
            adjacencyList.get(id).stream()
                    .filter(x -> !visitedUsers.containsKey(x))
                    .forEach(x -> DFS(x, visitedUsers, community));
        }
    }

    private void refreshFriendsList() {
        adjacencyList.clear();
        NetworkService.getAllUsers().forEach(user -> {
            List<UUID> friends = new ArrayList<>();
            NetworkService.getAllFriendships().forEach(friendship -> {
                if (friendship.getId().getE2().equals(user.getId()))
                    friends.add(friendship.getId().getE2());
                if (friendship.getId().getE2().equals(user.getId()))
                    friends.add(friendship.getId().getE1());
            });
            adjacencyList.putIfAbsent(user.getId(), friends);
        });

    }

    public int numberOfCommunities() {
        refreshFriendsList();
        HashMap<UUID, Boolean> visitedUsers = new HashMap<>();
        int numberOfCommunities = 0;

        for (UUID id : adjacencyList.keySet()) {
            if (!visitedUsers.getOrDefault(id, false)) {
                List<UUID> community = new ArrayList<>();
                DFS(id, visitedUsers, community);
                numberOfCommunities++;
            }
        }
        return numberOfCommunities;
    }


    public List<UUID> mostFriendlyCommunity() {
        refreshFriendsList();
        HashMap<UUID, Boolean> visitedUsers = new HashMap<>();
        List<UUID> largestCommunity = new ArrayList<>();

        for (UUID id : adjacencyList.keySet()) {
            if (!visitedUsers.getOrDefault(id, false)) {
                List<UUID> community = new ArrayList<>();
                DFS(id, visitedUsers, community);
                if (community.size() > largestCommunity.size()) {
                    largestCommunity = new ArrayList<>(community);
                }
            }
        }
        return largestCommunity;
    }


}

package ubb.scs.map.repository.database;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.repository.FriendshipPagingRepository;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.utils.Page;
import ubb.scs.map.utils.Pageable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class FriendshipRepositoryDatabase implements FriendshipPagingRepository {
    private final String url;
    private final String username;
    private final String password;

    public FriendshipRepositoryDatabase(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Friendship extractEntityFromResultSet(ResultSet resultSet) throws SQLException {
        UUID firstUserId = resultSet.getObject("first_user", UUID.class);
        UUID secondUserId = resultSet.getObject("second_user", UUID.class);
        LocalDateTime friendshipDate = resultSet.getTimestamp("friends_from").toLocalDateTime();
        FriendshipStatus status = FriendshipStatus.valueOf(resultSet.getString("status"));
        UUID senderId = resultSet.getObject("sender", UUID.class);
        boolean isNotificationSent = resultSet.getBoolean("notification_sent");

        Friendship friendship = new Friendship(firstUserId, secondUserId, friendshipDate, status);
        friendship.setSender(senderId);
        friendship.setNotificationSent(isNotificationSent);
        return friendship;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        if (exists(entity.getId())) {
            throw new EntityAlreadyExistsException(entity.getId().getE1() + " is already friends with " + entity.getId().getE2());
        }

        String sql = "INSERT INTO friendships (first_user, second_user, friends_from, status, sender, notification_sent) VALUES (?, ?, ?, ?, ?,?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, entity.getId().getE1());
            preparedStatement.setObject(2, entity.getId().getE2());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            preparedStatement.setString(4, entity.getFriendshipStatus().name());
            preparedStatement.setObject(5, entity.getIdSender());
            preparedStatement.setBoolean(6, entity.isNotificationSent());
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return findOne(entity.getId());
    }

    public Iterable<Friendship> findAll() {
        Map<Tuple<UUID, UUID>, Friendship> friendships = new HashMap<>();
        String sql = "SELECT * FROM friendships";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Friendship friendship = extractEntityFromResultSet(resultSet);
                friendships.put(friendship.getId(), friendship);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return friendships.values();
    }


    @Override
    public Optional<Friendship> findOne(Tuple<UUID, UUID> friendshipId) {
        if (friendshipId == null) throw new IllegalArgumentException("Friendship's id is null");
        UUID firstUser = friendshipId.getE1();
        UUID secondUser = friendshipId.getE2();
        String sql = "SELECT * FROM friendships WHERE (first_user = ? AND second_user = ?) OR (first_user = ? AND second_user = ?)";

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, firstUser);
            preparedStatement.setObject(2, secondUser);
            preparedStatement.setObject(3, secondUser);
            preparedStatement.setObject(4, firstUser);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(extractEntityFromResultSet(resultSet));
            } else {
                return Optional.empty();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Friendship> delete(Tuple<UUID, UUID> friendshipId) {
        Friendship friendship = findOne(friendshipId).orElseThrow(() -> new EntityMissingException("Friendship does not exist!"));
        UUID firstUser = friendshipId.getE1();
        UUID secondUser = friendshipId.getE2();
        String sql = "DELETE FROM friendships WHERE (first_user = ? AND second_user = ?) OR (first_user = ? AND second_user = ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, firstUser);
            preparedStatement.setObject(2, secondUser);
            preparedStatement.setObject(3, secondUser);
            preparedStatement.setObject(4, firstUser);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.of(friendship);
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        findOne(entity.getId()).orElseThrow(() -> new EntityMissingException(entity.getId() + " does not exist!"));
        String sql = "UPDATE friendships SET status = ?, friends_from = ?, notification_sent = ? WHERE first_user = ? AND second_user = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, entity.getFriendshipStatus().name());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(entity.getFriendsFrom()));
            preparedStatement.setBoolean(3, entity.isNotificationSent());
            preparedStatement.setObject(4, entity.getId().getE1());
            preparedStatement.setObject(5, entity.getId().getE2());
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public boolean exists(Tuple<UUID, UUID> friendshipId) {
        return findOne(friendshipId).isPresent();
    }

    private int countFriendships(Connection connection, UUID id){
        String sql = "SELECT COUNT(*) FROM friendships WHERE (first_user = ? OR second_user = ? ) and status = ? ";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setObject(1, id);
            statement.setObject(2, id);
            statement.setString(3, FriendshipStatus.ACCEPTED.name());

            ResultSet resultSet = statement.executeQuery();

            int totalNumberOfFriendships = 0;

            if(resultSet.next())
                totalNumberOfFriendships = resultSet.getInt("count");

            return totalNumberOfFriendships;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private List<Friendship> findAllFriendsOnPage(Connection connection, Pageable pageable, UUID id)  {
        List<Friendship> friendshipsOnPage = new ArrayList<>();
        String sql = "SELECT * FROM friendships WHERE (first_user = ? OR second_user = ?) AND status = ? LIMIT ? OFFSET ?";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.setObject(2, id);
            statement.setString(3,FriendshipStatus.ACCEPTED.name());
            statement.setInt(4, pageable.getPageSize());
            statement.setInt(5, pageable.getPageNumber() * pageable.getPageSize());

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Friendship friendship = extractEntityFromResultSet(resultSet);
                friendshipsOnPage.add(friendship);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendshipsOnPage;

    }

    @Override
    public Page<Friendship> findAllOnFriendsOnPage(Pageable pageable, UUID id) {
        try(Connection connection = DriverManager.getConnection(this.url, this.username, this.password)) {
            int totalNumberOfFriends= countFriendships(connection, id);
            List<Friendship> friendshipsOnPage ;
            if(totalNumberOfFriends > 0)
                friendshipsOnPage = findAllFriendsOnPage(connection, pageable, id);
            else
                friendshipsOnPage = new ArrayList<>();
            return new Page<>(friendshipsOnPage, totalNumberOfFriends);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

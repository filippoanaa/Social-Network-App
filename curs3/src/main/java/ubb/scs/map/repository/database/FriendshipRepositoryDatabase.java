package ubb.scs.map.repository.database;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.repository.Repository;
import java.sql.*;

import java.time.LocalDateTime;
import java.util.*;

public class FriendshipRepositoryDatabase implements Repository<Tuple<String, String>, Friendship> {
    private final String url;
    private final String username;
    private final String password;

    public FriendshipRepositoryDatabase(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }


    private Friendship extractEntityFromResultSet(ResultSet resultSet) throws SQLException{
        String first_username = resultSet.getString("first_user");
        String second_username = resultSet.getString("second_user");
        LocalDateTime friendshipDate = resultSet.getTimestamp("friends_from").toLocalDateTime();
        return new Friendship(first_username, second_username, friendshipDate);
    }

    @Override
    public Optional<Friendship> findOne(Tuple<String, String> friendshipId) {
        if (friendshipId == null) throw new IllegalArgumentException("Friendship's id is null");
        String first_user = friendshipId.getE1();
        String second_user = friendshipId.getE2();
        String sql = "SELECT * FROM friendships WHERE (first_user = ? AND second_user = ?) or (first_user = ? AND second_user = ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, first_user);
            preparedStatement.setString(2, second_user);
            preparedStatement.setString(3, second_user);
            preparedStatement.setString(4, first_user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new EntityMissingException("Friendship does not exist!");
            }

            return Optional.of(extractEntityFromResultSet(resultSet));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.empty();
    }


    public Iterable<Friendship> findAll() {
        Map<Tuple<String, String>, Friendship> friendships = new HashMap<>();
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
    public Optional<Friendship> save(Friendship entity) {
        if (exists(entity.getId()))
            throw new EntityAlreadyExistsException(entity.getId().getE1() + " is already friend with  " + entity.getId().getE2());

        String sql = "INSERT INTO friendships (first_user, second_user, friends_from) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, entity.getId().getE1());
            preparedStatement.setString(2, entity.getId().getE2());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return findOne(entity.getId());
    }

    @Override
    public Optional<Friendship> delete(Tuple<String, String> friendshipId) {
        Friendship friendship = findOne(friendshipId).orElseThrow(() -> new EntityMissingException("Friendship does not exist!"));
        String first_user = friendshipId.getE1();
        String second_user = friendshipId.getE2();
        String sql = "DELETE FROM friendships WHERE (first_user = ? AND second_user = ?) or  (first_user = ? AND second_user = ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, first_user);
            preparedStatement.setString(2, second_user);
            preparedStatement.setString(3, second_user);
            preparedStatement.setString(4, first_user);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.of(friendship);
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        findOne(entity.getId()).orElseThrow(() -> new EntityMissingException(entity.getId() + " does not exist!"));
        String sql = "update friendhips set date =? where first_user = ? and second_user = ?";
        try(Connection connection = DriverManager.getConnection(this.url,this.username,this.password);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, Timestamp.valueOf(entity.getFriendsFrom()).toString());
            preparedStatement.setString(2, entity.getId().getE1());
            preparedStatement.setString(3, entity.getId().getE2());
            preparedStatement.executeUpdate();
        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public boolean exists(Tuple<String, String> friendshipId) {
        try {
            findOne(friendshipId);
        } catch (EntityMissingException e) {
            return false;
        }
        return true;
    }

}

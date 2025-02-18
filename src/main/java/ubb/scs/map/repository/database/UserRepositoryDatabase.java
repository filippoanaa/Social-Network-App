package ubb.scs.map.repository.database;

import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.repository.Repository;

import java.sql.*;
import java.util.*;

public class UserRepositoryDatabase implements Repository<UUID, User> {
    private final String url;
    private final String username;
    private final String password;

    public UserRepositoryDatabase(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private User extractEntityFromResultSet(ResultSet resultSet) throws SQLException {
        UUID id = resultSet.getObject("id", UUID.class);
        String username = resultSet.getString("username");
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        String password = resultSet.getString("password");
        byte[] profilePicture = resultSet.getBytes("profile_picture");
        User user = new User(username, firstName, lastName, password, profilePicture);
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findOne(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new UserMissingException("The user with id:" + id + " does not exist!");
            }
            return Optional.of(extractEntityFromResultSet(resultSet));

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        String sql = "SELECT * FROM users";
        Map<UUID, User> users = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = extractEntityFromResultSet(resultSet);
                users.put(user.getId(), user);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return users.values();
    }

    @Override
    public Optional<User> save(User entity) {
        if (exists(entity.getId())) {
            throw new EntityAlreadyExistsException(entity.getId() + " already exists");
        }
        String sql = "INSERT INTO users (id, username, first_name, last_name, password, profile_picture) VALUES (?, ?, ?, ?, ?, ?)"; // New
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, entity.getId());
            preparedStatement.setString(2, entity.getUsername());
            preparedStatement.setString(3, entity.getFirstName());
            preparedStatement.setString(4, entity.getLastName());
            preparedStatement.setString(5, entity.getPassword());
            preparedStatement.setBytes(6, entity.getProfilePicture()); // New
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<User> delete(UUID id) {
        User user = findOne(id).orElseThrow(() -> new EntityMissingException("User with id:" + id + " does not exist!"));
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.of(user);
    }

    @Override
    public Optional<User> update(User entity) {
        findOne(entity.getId()).orElseThrow(() -> new EntityMissingException(entity.getId() + " does not exist!"));
        String sql = "UPDATE users SET username = ?, first_name = ?, last_name = ?, password = ?, profile_picture = ? WHERE id = ?"; // New
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getFirstName());
            preparedStatement.setString(3, entity.getLastName());
            preparedStatement.setString(4, entity.getPassword());
            preparedStatement.setBytes(5, entity.getProfilePicture());
            preparedStatement.setObject(6, entity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public boolean exists(UUID id) {
        try {
            findOne(id);
        } catch (EntityMissingException e) {
            return false;
        }
        return true;
    }
}
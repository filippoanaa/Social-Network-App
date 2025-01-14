package ubb.scs.map.repository.database;

import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.repository.Repository;
import ubb.scs.map.utils.Page;
import ubb.scs.map.utils.Pageable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageRepositoryDatabase implements Repository<UUID, Message> {
    private final String url;
    private final String username;
    private final String password;
    UserRepositoryDatabase userRepositoryDatabase;

    public MessageRepositoryDatabase(String url, String username, String password, UserRepositoryDatabase userRepositoryDatabase) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.userRepositoryDatabase = userRepositoryDatabase;
    }

    private Message extractEntity(ResultSet resultSet) throws SQLException {
        UUID id = resultSet.getObject("id", UUID.class);
        UUID fromId = resultSet.getObject("from_id", UUID.class);
        String text = resultSet.getString("text");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
        UUID replyId = resultSet.getObject("reply_message_id", UUID.class);

        User from = userRepositoryDatabase.findOne(fromId).orElseThrow(UserMissingException::new);

        List<User> to = new ArrayList<>();
        String sql = "SELECT to_id FROM users_messages WHERE message_id = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement toStatement = connection.prepareStatement(sql)) {
            toStatement.setObject(1, id);
            ResultSet toResultSet = toStatement.executeQuery();
            while (toResultSet.next()) {
                UUID toId = toResultSet.getObject("to_id", UUID.class);
                User toUser = userRepositoryDatabase.findOne(toId).orElseThrow(UserMissingException::new);
                to.add(toUser);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        Message reply = null;
        if(replyId != null) {
            reply = this.findOne(replyId).orElse(null);
        }

        Message message = new Message(from, to, text, date);
        message.setReply(reply);
        message.setId(id);
        return message;

    }

    @Override
    public Optional<Message> findOne(UUID uuid) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                throw new EntityMissingException("Message with id " + uuid + " does not exist");
            return Optional.of(extractEntity(resultSet));

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        String sql = "SELECT * FROM messages ";

        Map<UUID, Message> messages = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Message message = extractEntity(resultSet);
                messages.put(message.getId(), message);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messages.values();
    }

    public List<Message> findMessagesBetween(UUID id1, UUID id2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT *" +
                "FROM messages m " +
                "JOIN users_messages um ON m.id = um.message_id " +
                "WHERE (um.to_id = ? AND m.from_id = ?) OR (um.to_id = ? AND m.from_id = ?) ";

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, id1);
            preparedStatement.setObject(2, id2);
            preparedStatement.setObject(3, id2);
            preparedStatement.setObject(4, id1);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Message message = extractEntity(resultSet);
                messages.add(message);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }


    @Override
    public Optional<Message> save(Message entity) {
        String sqlMessage = "INSERT INTO messages(id, from_id, text, date,  reply_message_id) VALUES (?, ?, ?, ?, ?) ";
        String sqlUsersMessages = "INSERT INTO users_messages(to_id, message_id) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement messageStatement = connection.prepareStatement(sqlMessage);
             PreparedStatement userMessagesStatement = connection.prepareStatement(sqlUsersMessages)) {

            messageStatement.setObject(1, entity.getId());
            messageStatement.setObject(2, entity.getFrom().getId());
            messageStatement.setObject(3, entity.getText());
            messageStatement.setTimestamp(4, Timestamp.valueOf(entity.getDate()));

            if (entity.getReply() != null) {
                messageStatement.setObject(5, entity.getReply().getId());
            } else {
                messageStatement.setNull(5, Types.NULL);
            }
            messageStatement.execute();

            for (User recipient : entity.getTo()) {
                userMessagesStatement.setObject(1, recipient.getId());
                userMessagesStatement.setObject(2, entity.getId());
                userMessagesStatement.addBatch();
            }
            userMessagesStatement.executeBatch();
            return Optional.of(entity);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Message> delete(UUID uuid) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setObject(1, uuid);
            preparedStatement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();

    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }

    @Override
    public boolean exists(UUID uuid) {
        return false;
    }




}

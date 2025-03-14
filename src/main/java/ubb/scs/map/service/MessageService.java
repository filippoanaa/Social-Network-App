package ubb.scs.map.service;

import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.repository.database.MessageRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;
import ubb.scs.map.utils.Observable;
import java.util.*;

public class MessageService extends Observable {
    private final MessageRepositoryDatabase messageRepositoryDatabase;
    private final UserRepositoryDatabase userRepositoryDatabase;

    public MessageService(MessageRepositoryDatabase messageRepositoryDatabase, UserRepositoryDatabase userRepositoryDatabase) {
        this.messageRepositoryDatabase = messageRepositoryDatabase;
        this.userRepositoryDatabase = userRepositoryDatabase;
    }

    public void addMessage(Message message) throws UserMissingException {
        if(!userRepositoryDatabase.exists(message.getFrom().getId()))
            throw new UserMissingException("Sender does not exits!");

        for(User recipient : message.getTo()){
            if (!userRepositoryDatabase.exists(recipient.getId())) {
                throw new UserMissingException("Recipient does not exist: " + recipient.getId());
            }
        }

        messageRepositoryDatabase.save(message);
        notifyObservers();
    }


    public List<Message> getMessagesBetween(UUID id1, UUID id2){
        List<Message> messages = messageRepositoryDatabase.findMessagesBetween(id1, id2);
        messages.sort(Comparator.comparing(Message::getDate));
        return messages;


    }

    public void deleteConversation(UUID id, UUID id1) {
        List<Message> messages = messageRepositoryDatabase.findMessagesBetween(id, id1);
        for(Message message : messages){
            messageRepositoryDatabase.delete(message.getId());
        }
        notifyObservers();
    }
}

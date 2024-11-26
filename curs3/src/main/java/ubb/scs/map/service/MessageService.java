package ubb.scs.map.service;

import ubb.scs.map.domain.Message;
import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.UserMissingException;
import ubb.scs.map.repository.database.MessageRepositoryDatabase;
import ubb.scs.map.repository.database.UserRepositoryDatabase;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageService {
    private final MessageRepositoryDatabase messageRepositoryDatabase;
    private final UserRepositoryDatabase userRepositoryDatabase;

    public MessageService(MessageRepositoryDatabase messageRepositoryDatabase, UserRepositoryDatabase userRepositoryDatabase) {
        this.messageRepositoryDatabase = messageRepositoryDatabase;
        this.userRepositoryDatabase = userRepositoryDatabase;
    }

    public boolean addMessage(Message message) {
        if(!userRepositoryDatabase.exists(message.getFrom().getId()))
            throw new UserMissingException("Sender does not exits!");

        for(User recipient : message.getTo()){
            if (!userRepositoryDatabase.exists(recipient.getId())) {
                throw new IllegalArgumentException("Recipient does not exist: " + recipient.getId());
            }
        }

        return messageRepositoryDatabase.save(message).isPresent();
    }

    public void removeMessage(UUID id) {
        if (!messageRepositoryDatabase.exists(id)) {
            throw new IllegalArgumentException("Message with ID " + id + " does not exist");
        }

        messageRepositoryDatabase.delete(id);
    }

    public Iterable<Message> getMessages() {

        Iterable<Message> messages = messageRepositoryDatabase.findAll();
        int cnt = 0;
        for(Message m : messages)
            cnt++;
        System.out.println("Nr mesaje:" + cnt);
        return messages;

    }

//    public List<Message> getMessagesBetween(UUID id1, UUID id2) {
//        List<Message> messages = new ArrayList<>();
//
//        Iterable<Message> allMessages = messageRepositoryDatabase.findAll();
//        for(Message message : allMessages){
//            if(isMessageBetweenUsers(message, id1, id2))
//                messages.add(message);
//        }
//        messages.sort(Comparator.comparing(Message::getDate));
//        return messages;
//
//    }
//
//    private boolean isMessageBetweenUsers(Message message, UUID senderId, UUID receiverId) {
//        boolean isSenderReceiver = (message.getFrom().getId().equals(senderId) &&
//                message.getTo().stream().anyMatch(user -> user.getId().equals(receiverId))) ||
//                (message.getFrom().getId().equals(receiverId) &&
//                        message.getTo().stream().anyMatch(user -> user.getId().equals(senderId)));
//        return isSenderReceiver;
//    }

    public List<Message> getMessagesBetween(UUID id1, UUID id2){
        List<Message> messages = messageRepositoryDatabase.findMessagesBetween(id1, id2);
        System.out.println("Lung mesaje: " + messages.size());
        messages.sort(Comparator.comparing(Message::getDate));
        return messages;


    }

}

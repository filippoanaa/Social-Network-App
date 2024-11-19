package ubb.scs.map.repository.file;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.FriendshipStatus;
import ubb.scs.map.domain.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FriendshipRepository extends AbstractFileRepository<Tuple<UUID, UUID>, Friendship> {

    public FriendshipRepository(String fileName) {
        super(fileName);
    }

    @Override
    public Friendship createEntity(String line) {
        String[] splited = line.split(";");
        Friendship friendship = new Friendship(UUID.fromString(splited[0]), UUID.fromString(splited[1]), LocalDateTime.parse(splited[2]), FriendshipStatus.valueOf(splited[3]));
        friendship.setSender(UUID.fromString(splited[4]));
        return friendship;
    }

    @Override
    public String saveEntity(Friendship entity) {
        Tuple<UUID, UUID> usersTuple = entity.getId();
        LocalDateTime date = entity.getFriendsFrom();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = date.format(formatter);
        String status = entity.getFriendshipStatus().toString();
        String sender = entity.getIdSender().toString();
        return  usersTuple.getE1() + ";" + usersTuple.getE2() + ";" + formattedTime + ";" + status + ";" + sender;

    }


}

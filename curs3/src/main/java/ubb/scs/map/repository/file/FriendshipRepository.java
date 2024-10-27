package ubb.scs.map.repository.file;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FriendshipRepository extends AbstractFileRepository<Tuple<String, String>, Friendship> {

    public FriendshipRepository(String fileName) {
        super(fileName);
    }

    @Override
    public Friendship createEntity(String line) {
        String[] splited = line.split(";");
        Tuple<String, String> tuple = new Tuple<>(splited[0], splited[1]);
        return new Friendship(tuple);
    }

    @Override
    public String saveEntity(Friendship entity) {
        Tuple<String, String> usersTuple = entity.getId();
        LocalDateTime date = entity.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = date.format(formatter);
        return  usersTuple.getE1() + ";" + usersTuple.getE2() + ";" + formattedTime ;

    }


}

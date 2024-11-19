package ubb.scs.map.repository.file;

import ubb.scs.map.domain.User;

import java.util.UUID;

public class UserRepository extends AbstractFileRepository<UUID, User> {

    public UserRepository(String fileName) {
        super(fileName);
    }

    @Override
    public User createEntity(String line) {
        String[] splited = line.split(";");
        User user = new User(splited[1], splited[2], splited[3], splited[4]);
        user.setId(UUID.fromString(splited[0]));
        return user;
    }

    @Override
    public String saveEntity(User entity) {
        return entity.getId() + ";" + entity.getUsername() + ";" + entity.getFirstName() + ";" + entity.getLastName() + ";" + entity.getPassword();
    }


}

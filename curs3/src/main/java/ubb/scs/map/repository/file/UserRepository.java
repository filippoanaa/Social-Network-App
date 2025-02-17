package ubb.scs.map.repository.file;

import ubb.scs.map.domain.User;

import java.util.UUID;

public class UserRepository extends AbstractFileRepository<UUID, User> {

    public UserRepository(String fileName) {
        super(fileName);
    }

    @Override
    public User createEntity(String line) {
        String[] split = line.split(";");
        User user = new User(split[1], split[2], split[3], split[4], split[5].getBytes());
        user.setId(UUID.fromString(split[0]));
        return user;
    }

    @Override
    public String saveEntity(User entity) {
        return entity.getId() + ";" + entity.getUsername() + ";" + entity.getFirstName() + ";" + entity.getLastName() + ";" + entity.getPassword();
    }


}

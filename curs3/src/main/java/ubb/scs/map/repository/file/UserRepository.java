package ubb.scs.map.repository.file;

import ubb.scs.map.domain.User;

public class UserRepository extends AbstractFileRepository<String, User> {

    public UserRepository(String fileName) {
        super(fileName);
    }

    @Override
    public User createEntity(String line) {
        String[] splited = line.split(";");
        return new User(splited[0], splited[1], splited[2], splited[3]);
    }

    @Override
    public String saveEntity(User entity) {
        return entity.getId() + ";" + entity.getFirstName() + ";" + entity.getLastName() + ";" + entity.getPassword();
    }




}

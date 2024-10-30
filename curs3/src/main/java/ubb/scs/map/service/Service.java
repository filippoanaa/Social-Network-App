package ubb.scs.map.service;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.domain.User;
import ubb.scs.map.repository.Repository;

public abstract class Service {
    protected final Repository<String, User> userRepository;
    protected final Repository<Tuple<String, String>, Friendship> friendshipRepository;

    protected Service(Repository<String, User> userRepository, Repository<Tuple<String, String>, Friendship> friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }


}

package ubb.scs.map.repository;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;
import ubb.scs.map.utils.Page;
import ubb.scs.map.utils.Pageable;

import java.util.UUID;

public interface FriendshipPagingRepository extends Repository<Tuple<UUID, UUID>, Friendship> {
    Page<Friendship> findAllOnFriendsOnPage(Pageable pageable, UUID id);
}

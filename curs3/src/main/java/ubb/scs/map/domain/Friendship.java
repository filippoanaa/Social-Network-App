package ubb.scs.map.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Friendship extends Entity<Tuple<UUID, UUID>> {
    private UUID idUser1;
    private UUID idUser2;
    private LocalDateTime friendsFrom;
    private FriendshipStatus friendshipStatus;
    private UUID idSender;


    public Friendship(UUID idUser1, UUID idUser2, LocalDateTime friendsFrom, FriendshipStatus friendshipStatus) {
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
        this.friendsFrom = friendsFrom;
        this.friendshipStatus = friendshipStatus;
        this.setId(generateId(idUser1, idUser2));
    }



    public Friendship(UUID idUser1, UUID idUser2) {
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
        this.friendsFrom = LocalDateTime.now();
        this.friendshipStatus = FriendshipStatus.PENDING;
        this.setId(generateId(idUser1, idUser2));

    }


    private Tuple<UUID, UUID> generateId(UUID idUser1, UUID idUser2) {
        return idUser1.compareTo(idUser2) < 0
                ? new Tuple<>(idUser1, idUser2)
                : new Tuple<>(idUser2, idUser1);
    }


    public UUID getUser1() { return idUser1; }

    public void setUser1(UUID user1) { this.idUser1 = user1; }

    public UUID getUser2() { return idUser2; }

    public void setUser2(UUID user2) { this.idUser2 = user2;}


    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    public FriendshipStatus getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    public boolean isSender(UUID sender){ return sender.equals(this.idSender); }

    public void setSender(UUID sender){ this.idSender = sender; }

    public UUID getIdSender() { return idSender; }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(idUser1, that.idUser1) && Objects.equals(idUser2, that.idUser2) && Objects.equals(friendsFrom, that.friendsFrom) && friendshipStatus == that.friendshipStatus && Objects.equals(idSender, that.idSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser1, idUser2, friendsFrom, friendshipStatus, idSender);
    }
}

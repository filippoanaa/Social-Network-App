package ubb.scs.map.domain;

import java.time.LocalDateTime;

public class Friendship extends Entity<Tuple<String ,String>> {
    private LocalDateTime friendsFrom;
    public Friendship(String user1, String user2, LocalDateTime friendsFrom) {
        super(new Tuple<>(user1, user2));
        this.friendsFrom = friendsFrom;
    }

    public Friendship(String user1, String user2) {
        super(new Tuple<>(user1, user2));
        this.friendsFrom = LocalDateTime.now();
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }
    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship)) return false;
        Friendship friendship = (Friendship) o;
        Tuple<String, String> thisTuple = this.getId();
        Tuple<String, String> otherTuple = friendship.getId();

        return (thisTuple.getE1().equals(otherTuple.getE1()) && thisTuple.getE2().equals(otherTuple.getE2())) ||
                (thisTuple.getE1().equals(otherTuple.getE2()) && thisTuple.getE2().equals(otherTuple.getE1()));
    }


}

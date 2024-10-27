package ubb.scs.map.domain;

import java.time.LocalDateTime;

public class Friendship extends Entity<Tuple<String ,String>> {
    private final LocalDateTime date = LocalDateTime.now();

    public Friendship(Tuple<String ,String> userTuple) {
        super(userTuple);
    }

    public LocalDateTime getDate() {
        return date;
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

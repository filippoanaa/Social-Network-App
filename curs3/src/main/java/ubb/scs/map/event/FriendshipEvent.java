package ubb.scs.map.event;
import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.User;

public class FriendshipEvent implements Event {
    private ChangeEventType type;
    private Friendship data, oldData;

    public FriendshipEvent(ChangeEventType type, Friendship data) {
        this.type = type;
        this.data = data;
    }
    public FriendshipEvent(ChangeEventType type, Friendship data, Friendship oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Friendship getData() {
        return data;
    }

    public Friendship getOldData() {
        return oldData;
    }
}

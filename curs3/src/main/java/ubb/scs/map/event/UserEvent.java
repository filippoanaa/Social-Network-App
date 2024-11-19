package ubb.scs.map.event;

import ubb.scs.map.domain.User;
import ubb.scs.map.event.ChangeEventType;

public class UserEvent implements Event {
    private ChangeEventType type;
    private User data, oldData;

    public UserEvent(ChangeEventType type, User data) {
        this.type = type;
        this.data = data;
    }
    public UserEvent(ChangeEventType type, User data, User oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public User getData() {
        return data;
    }

    public User getOldData() {
        return oldData;
    }
}
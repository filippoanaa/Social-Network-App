package ubb.scs.map.domain;

import java.io.Serializable;
import java.util.UUID;

public abstract class Entity<ID>  implements Serializable {

    private static final long serialVersionUID = 7331115341259248461L;
    private ID id;
    public ID getId() {
        return id;
    }
    public void setId(ID id) {
        this.id = id;
    }

}
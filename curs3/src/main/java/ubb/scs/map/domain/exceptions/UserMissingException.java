package ubb.scs.map.domain.exceptions;

public class UserMissingException extends EntityMissingException{
    public UserMissingException() {
        super();
    }

    public UserMissingException(String message) {
        super(message);
    }

}
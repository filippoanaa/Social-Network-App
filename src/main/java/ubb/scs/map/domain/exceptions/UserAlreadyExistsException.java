package ubb.scs.map.domain.exceptions;

public class UserAlreadyExistsException extends EntityAlreadyExistsException{
    public UserAlreadyExistsException(String message) {
        super(message);
    }

}

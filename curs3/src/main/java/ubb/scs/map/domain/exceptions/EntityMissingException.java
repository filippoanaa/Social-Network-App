package ubb.scs.map.domain.exceptions;

public class EntityMissingException extends RuntimeException{
    public EntityMissingException() {
    }

    public EntityMissingException(String message) {
        super(message);
    }

}
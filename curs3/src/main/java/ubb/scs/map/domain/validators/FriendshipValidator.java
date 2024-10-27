package ubb.scs.map.domain.validators;

import ubb.scs.map.domain.Friendship;
import ubb.scs.map.domain.Tuple;

public class FriendshipValidator implements Validator<Friendship>{

    /**
     * Validates a friendship
     * @param entity Friendship - the friendship to be validated
     * @throws ValidationException with the errors if the friendship is not valid
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(entity.getId().getE1().length() < 3 || entity.getId().getE2().length() < 3)
            throw new ValidationException("The username must have at least 3 characters");
        if(entity.getId().getE1().equals(entity.getId().getE2())) {
            throw new ValidationException("Friendship usernames cannot be the same!");
        }
    }
}

package ubb.scs.map.domain.validators;

import ubb.scs.map.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship>{

    /**
     * Validates a friendship
     * @param entity Friendship - the friendship to be validated
     * @throws ValidationException with the errors if the friendship is not valid
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(entity.getUser1() == entity.getUser2())
            throw new ValidationException("You cannot add yourself as a friend...:(");
    }
}

package ubb.scs.map.domain.validators;


import ubb.scs.map.domain.User;

import java.util.regex.Pattern;


public class UserValidator implements Validator<User> {

    /**
     * Verifies if a string is a valid id
     * @param username - the string that has to be validated
     * @return errorMessages - String that contains the validation errors
     */
    private String validateId(String username) {
        String errorMessages = "";
        if (username.length() <= 2)
            errorMessages += "Username is too short.";
        return errorMessages;
    }

    /**
     * Verifies if a string is a valid name
     * @param name - the string that has to be validated
     * @return errorMessages - String that contains the validation errors
     */
    public String validateName(String name) {
        String errorMessages = "";
        boolean pattern = Pattern.matches("^[A-Za-z\\s]{3,}$", name);
        if (!pattern)
            errorMessages += "Name must contain only letters and spaces. Minimum length 3.";
        return errorMessages;
    }

    /**
     * Validates an user
     * @param entity - User
     * @throws ValidationException only if a user field is not valid; contains the validation errors
     */
    @Override
    public void validate(User entity) throws ValidationException {
        String errorMessages = "";
        String usernameErrors = this.validateId(entity.getId());
        if (!usernameErrors.isEmpty())
            errorMessages += usernameErrors + "\n";

        String firstNameErrors = this.validateName(entity.getFirstName());
        if (!firstNameErrors.isEmpty())
            errorMessages += firstNameErrors + "\n";

        String lastNameErrors = this.validateName(entity.getLastName());
        if (!lastNameErrors.isEmpty())
            errorMessages += lastNameErrors + "\n";

        if (!errorMessages.isEmpty())
            throw new ValidationException(errorMessages);
    }


}

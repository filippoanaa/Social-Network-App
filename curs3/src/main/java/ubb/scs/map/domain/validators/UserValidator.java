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
            errorMessages += "name must contain only letters and spaces. Minimum length 3.";
        return errorMessages;
    }

    public String validatePassword(String password) {
        String errorMessages = "";
        boolean patternLength =password.length() >= 5;
        boolean specialCharacter = Pattern.matches(".*[!@#$%^&*()~_+\\-{}\\[\\];':\"<>?/.,].*", password);
        boolean numericCharacter = Pattern.matches(".*[0-9].*", password);
        if(!patternLength)
            errorMessages += "The password is too weak! It must have at least 5 characters\n";
        if(!specialCharacter)
            errorMessages += "The password must have at least one special character\n";
        if(!numericCharacter)
            errorMessages += "The password must have at least one numeric character\n";
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
            errorMessages += "First " + firstNameErrors + "\n";

        String lastNameErrors = this.validateName(entity.getLastName());
        if (!lastNameErrors.isEmpty())
            errorMessages += "Last " + lastNameErrors + "\n";

        String passwordErrors = this.validatePassword(entity.getPassword());
        if (!passwordErrors.isEmpty())
            errorMessages += passwordErrors + "\n";

        if (!errorMessages.isEmpty())
            throw new ValidationException(errorMessages);
    }


}

package com.example.messanger.Validators;

import java.util.regex.Pattern;

public class ValidSurname extends Validation{

    @Override
    public boolean checkConstraint(String fieldValue) {
        return Pattern.compile("[A-Z]{0,1}[a-z]{3,15}").matcher(Character.toUpperCase(fieldValue.charAt(0)) + fieldValue.substring(1, fieldValue.length()).toLowerCase()).matches();
    }
}

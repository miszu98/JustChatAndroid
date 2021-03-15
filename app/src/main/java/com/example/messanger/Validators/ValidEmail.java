package com.example.messanger.Validators;

import java.util.regex.Pattern;

public class ValidEmail extends Validation {
    @Override
    public boolean checkConstraint(String fieldValue) {
        return Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matcher(fieldValue).matches();
    }


}

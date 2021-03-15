package com.example.messanger.Validators;

import java.util.regex.Pattern;

public class ValidAge extends Validation{

    @Override
    public boolean checkConstraint(String fieldValue) {
        return Pattern.compile("[1-9]{1}[0-9]{1}").matcher(fieldValue).matches();
    }

}

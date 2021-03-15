package com.example.messanger.Validators;

import java.util.regex.Pattern;

public class ValidPassword extends Validation {

    @Override
    public boolean checkConstraint(String fieldValue) {
        return Pattern.compile("^[a-zA-Z]{8,12}[0-9]{1,4}[.,!@#$&*]{1}").matcher(fieldValue).matches();
    }



}

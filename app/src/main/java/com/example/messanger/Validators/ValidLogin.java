package com.example.messanger.Validators;


import java.util.regex.Pattern;

public class ValidLogin extends Validation{

    @Override
    public boolean checkConstraint(String fieldValue) {
        return Pattern.compile("[A-Z]{0,5}[a-z]{5,15}[0-9]{0,4}").matcher(fieldValue).matches();
    }

}

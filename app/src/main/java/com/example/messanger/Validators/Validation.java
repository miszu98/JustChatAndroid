package com.example.messanger.Validators;

public abstract class Validation {
    abstract boolean checkConstraint(String fieldValue);

    public boolean emptyField(String fieldValue) {
        return fieldValue.length() == 0;
    }

    public boolean oneSpace(String fieldValue) {
        for (char x : fieldValue.toCharArray()) {
            if (x == ' ') { return true; }
        }
        return false;
    }
}

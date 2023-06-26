package com.zerototen.savegame.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<Enum, String> {

    private Enum annotation;

    @Override
    public void initialize(Enum constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Object[] enumValues = this.annotation.enumClass().getEnumConstants();
        if (enumValues != null) {
            if (value != null) {
                for (Object enumValue : enumValues) {
                    if (enumValue.toString().equals("ALL")) {
                        continue;
                    }
                    if (value.equals(enumValue.toString())
                        || (this.annotation.ignoreCase() && value.equalsIgnoreCase(enumValue.toString()))) {
                        return true;
                    }
                }
            } else {
                return this.annotation.nullable();
            }
        }
        return false;
    }

}
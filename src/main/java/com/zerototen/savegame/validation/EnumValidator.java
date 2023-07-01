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
                    if (value.equals(enumValue.toString())
                        || (this.annotation.ignoreCase() && value.equalsIgnoreCase( // 대소문자 구분 안함
                        enumValue.toString()))) {
                        if (enumValue.toString().equals("ALL")) {
                            return this.annotation.allowAll(); // ALL 허용여부
                        }
                        return true;
                    }
                }
            } else {
                return this.annotation.nullable(); // null 허용여부
            }
        }
        return false;
    }

}
package com.zerototen.savegame.validation;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumListValidator implements ConstraintValidator<EnumList, List<String>> {

    private EnumList annotation;
    private Class<? extends java.lang.Enum<?>> enumClass;

    @Override
    public void initialize(EnumList constraintAnnotation) {
        this.annotation = constraintAnnotation;
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true;
        }

        for (String value : values) {
            if (!isEnumValue(value)) {
                return false;
            }
        }

        return true;
    }

    private boolean isEnumValue(String value) {
        Object[] enumValues = enumClass.getEnumConstants();
        if (enumValues != null) {
            for (Object enumValue : enumValues) {
                if (value.equals(enumValue.toString())
                    || (this.annotation.ignoreCase() && value.equalsIgnoreCase(
                    enumValue.toString()))) {
                    if (enumValue.toString().equals("ALL")) {
                        return this.annotation.allowAll(); // ALL 허용여부 (입력이 전체가 아니라 카테고리가 ALL)
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
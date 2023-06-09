package com.zerototen.savegame.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCrypt;

@UtilityClass
public class PasswordUtil {

    public static boolean checkPassword(String input, String encryptedPassword) {
        return BCrypt.checkpw(input, encryptedPassword);
    }

}
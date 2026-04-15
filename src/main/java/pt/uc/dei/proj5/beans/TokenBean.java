package pt.uc.dei.proj5.beans;

import jakarta.ejb.Stateless;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;

@Stateless
public class TokenBean implements Serializable {

    public static String generateToken() {
        SecureRandom sr = new SecureRandom();
        byte[] token = new byte[16];
        sr.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

}

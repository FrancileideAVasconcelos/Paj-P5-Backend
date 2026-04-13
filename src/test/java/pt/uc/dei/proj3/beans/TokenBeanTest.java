package pt.uc.dei.proj3.beans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uc.dei.proj5.beans.TokenBean;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBeanTest {

    private TokenBean tokenBean;

    @BeforeEach
    void setUp() {
        tokenBean = new TokenBean();
    }

    @Test
    void testGenerateToken_NotNullAndNotEmpty() {
        // Act
        String token = TokenBean.generateToken();

        // Assert
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
    }

    @Test
    void testGenerateToken_Uniqueness() {
        // Act
        String token1 = TokenBean.generateToken();
        String token2 = TokenBean.generateToken();

        // Assert
        assertNotEquals(token1, token2, "Two generated tokens should not be identical");
    }

    @Test
    void testGenerateToken_Format() {
        // Act
        String token = TokenBean.generateToken();

        // Assert
        // Since it's Base64 URL encoded without padding, it should only contain
        // alphanumeric characters, hyphens, or underscores.
        assertTrue(token.matches("^[A-Za-z0-9_-]+$"), "Token contains invalid characters");
    }

    @Test
    void testGetterAndSetter() {
        // Act
        String myToken = "test-token-123";
        tokenBean.setToken(myToken);

        // Assert
        assertEquals(myToken, tokenBean.getToken(), "The getter should return the value set by the setter");
    }
}
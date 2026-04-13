package pt.uc.dei.proj3.beans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.uc.dei.proj5.beans.TokenBean;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.dao.TokenDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.UserEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserBeanTest {

    @Mock
    private UserDao userDao;

    @Mock
    private TokenDao tokenDao;

    @InjectMocks
    private UserBean userBean;

    private UserEntity mockEntity;
    private UserDto mockDto;

    @BeforeEach
    void setUp() {
        mockEntity = new UserEntity();
        mockEntity.setUsername("tester");
        mockEntity.setPrimeiroNome("John");

        mockDto = new UserDto();
        mockDto.setUsername("tester");
    }

    @Test
    void testLoginToken_Success() {
        // Arrange
        String user = "tester";
        String pass = "pass123";
        String fakeToken = "random-uuid-string";

        when(userDao.getLogin(user, pass)).thenReturn(mockEntity);

        // Mocking the static method in TokenBean
        try (MockedStatic<TokenBean> mockedTokenBean = mockStatic(TokenBean.class)) {
            mockedTokenBean.when(TokenBean::generateToken).thenReturn(fakeToken);

            // Act
            String result = userBean.loginToken(user, pass);

            // Assert
            assertEquals(fakeToken, result);
            verify(tokenDao).guardarTokenDB(fakeToken, mockEntity);
        }
    }

    @Test
    void testLoginToken_Failure() {
        when(userDao.getLogin(anyString(), anyString())).thenReturn(null);

        String result = userBean.loginToken("wrong", "wrong");

        assertNull(result);
        verifyNoInteractions(tokenDao);
    }

    @Test
    void testRegister_UsernameExists() {
        // Arrange
        when(userDao.checkUsername("tester")).thenReturn(mockEntity);

        // Act
        boolean result = userBean.register(mockDto);

        // Assert
        assertFalse(result);
        verify(userDao, never()).novoUserDB(any());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userDao.checkUsername("tester")).thenReturn(null);

        // Act
        boolean result = userBean.register(mockDto);

        // Assert
        assertTrue(result);
        verify(userDao).novoUserDB(mockDto);
    }

    @Test
    void testGetUserByToken_NotFound() {
        when(tokenDao.getUserByToken("invalid-token")).thenReturn(null);

        UserDto result = userBean.getUserByToken("invalid-token");

        assertNull(result);
    }

    @Test
    void testUpdateUser_Success() throws Exception{

        userBean.updateUser(mockEntity, mockDto);

        verify(userDao).updateUserDB(mockEntity, mockDto);
    }
}
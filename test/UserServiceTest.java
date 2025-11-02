// src/test/java/com/aws/chat_app/service/UserServiceTest.java
package com.aws.chat_app;

import com.aws.chat_app.model.User;
import com.aws.chat_app.repository.UserRepository;
import com.aws.chat_app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testFindUserByUsername() {
        User mockUser = new User("alice", "alice@example.com", "password");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.findByUsername("alice");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void testSaveUser() {
        User user = new User("bob", "bob@example.com", "pass123");
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.save(user);

        assertEquals("bob", saved.getUsername());
    }
}

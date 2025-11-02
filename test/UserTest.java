// src/test/java/com/aws/chat_app/model/UserTest.java
package com.aws.chat_app;

import com.aws.chat_app.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserFields() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("secret");

        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("secret", user.getPasswordHash());
    }

}

// src/test/java/com/aws/chat_app/model/MessageTest.java
package com.aws.chat_app;

import com.aws.chat_app.model.cassandra.Message;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testGettersAndSetters() {
        Instant now = Instant.now();
        Message message = new Message();
        message.setSenderId("chat1");
        message.setSenderName("Alice");
        message.setText("Hello!");
        message.setMessageTime(now);

        assertEquals("chat1", message.getSenderId());
        assertEquals("Alice", message.getSenderName());
        assertEquals("Hello!", message.getText());
        assertEquals(now, message.getMessageTime());
    }

    @Test
    void testConstructor() {
        Instant now = Instant.now();
        Message message = new Message("1",Instant.now(),"chat1","Bob","Hi!");

        assertEquals("chat1", message.getSenderId());
        assertEquals("Bob", message.getSenderName());
        assertEquals("Hi!", message.getText());
        assertEquals(now, message.getMessageTime());
    }
}

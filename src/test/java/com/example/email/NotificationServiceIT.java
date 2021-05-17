package com.example.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NotificationServiceIT {

    @RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		.withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "spring"))
		.withPerMethodLifecycle(false);

    @Autowired
    private NotificationService notificationService;

    @Test
    void testSendEmailToUser() throws Exception {

        notificationService.notifyUser("user@example.com", "Hello, world!");

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);

        MimeMessage message = receivedMessages[0];

        assertEquals("Hello, world!", GreenMailUtil.getBody(message));
        assertEquals("user@example.com", message.getAllRecipients()[0].toString());

    }
    
}

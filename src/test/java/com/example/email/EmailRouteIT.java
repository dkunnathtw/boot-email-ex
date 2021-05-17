package com.example.email;

import static org.junit.Assert.assertEquals;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailRouteIT {

    @RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		.withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "spring"))
		.withPerMethodLifecycle(false);

    @Autowired
    protected CamelContext camelContext;

    @Produce(uri = EmailRouteBuilder.ENDPOINT)
    protected ProducerTemplate template;

    @Test
    void testSendEmailToUser() throws Exception {
    
        Endpoint endpoint = camelContext.getEndpoint("direct:testMailerEndpoint");
        Exchange exchange = endpoint.createExchange();

        Message in = exchange.getIn();
        in.setHeader("to", "user@example.com");
        in.setBody("Hello, world!");

        template.send(exchange);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);

        MimeMessage message = receivedMessages[0];

        assertEquals("Hello, world!", GreenMailUtil.getBody(message));
        assertEquals("user@example.com", message.getAllRecipients()[0].toString());
        
    }
}

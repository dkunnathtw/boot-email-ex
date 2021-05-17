# Boot-Email-Ex

Boot-Email-Ex is a sample application to demonstrate sending email from spring boot, using both JavaMailSender and Camel Mail.  Also shown is integration testing with an in-process GreenMail instance to avoid the need for using full SMTP infrastructure.

## Installation

Use the included gradle wrapper to install boot-email-ex.

```bash
gradle build
```

## Usage

Run gradle test suite to run two integration tests.

```bash
gradle test
```

Configuration and content of the two tests is explained below and should be instructive on how to setup and test sending email from a Spring Boot application.

### Common

```JavaMailSender``` bean is registered using ```spring.mail.*``` properties in application.properties.

```properties
spring.mail.username=spring
spring.mail.password=spring
spring.mail.host=127.0.0.1
spring.mail.port=3025
spring.mail.protocol=smtp
```

This configuration in ```application.properties``` configures Spring Boot to send mail to an SMTP server using the host, port, username, and password provided.  In a dev/test profile, GreenMail will assume the SMTP responsibility as shown in Integration Tests. 

This should be sufficient to keep _test email_ off a real SMTP server.  This also avoids the need to set up test email accounts, clean up inboxes, and risk the potential of an accidental email making it beyond the test pipeline.

In the JUnit5 context, an in-process GreenMail SMTP is controlled by registering an extension:

```java
@RegisterExtension
static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
	.withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "spring"))
	.withPerMethodLifecycle(false);
```

The extension controls the SMTP server lifecycle based on the configuration parameters shown.

The SMTP server can be interrogated during integration tests using assertions similar to these:

```java
MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
assertEquals(1, receivedMessages.length);

MimeMessage message = receivedMessages[0];

assertEquals("Hello, world!", GreenMailUtil.getBody(message));
assertEquals("user@example.com", message.getAllRecipients()[0].toString());
```

Both of the following integration tests demonstrated use this configuration and vary only in the way that an email gets sent.  Both of these cases are applicable in different scenarios.

#### Dependencies

Several common dependencies are used, as shown in the snippet from ```build.gradle```:

```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.springframework.boot:spring-boot-starter-actuator'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

    /** JUnit 5 Test Dependencies **/
	testImplementation 'org.junit.jupiter:junit-jupiter-api:5.3.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.3.1'

    /** Spring Boot Mail **/
    implementation 'org.springframework.boot:spring-boot-starter-mail'

	/** Greenmail test in-process SMTP server for integration tests **/
	testImplementation 'com.icegreen:greenmail-junit5:1.6.1'

	/** Camel Dependencies **/
	implementation 'org.apache.camel.springboot:camel-spring-boot-starter:3.9.0'
	implementation 'org.apache.camel.springboot:camel-mail-starter:3.9.0'
	testImplementation 'org.apache.camel:camel-test-spring-junit5:3.9.0'
}
```

### Notification Service IT

NotificationServiceIT wires in a sample Spring Boot ```@Service``` to handle notifications.  This example uses email as the notification vehicle, and could be accompanied by other notification implementations.  The Service could be called from a ReST interface or within other business logic as a handler for email as with any other ```@Service``` stereotype.

```java
@Service
public class NotificationService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void notifyUser(String email, String content) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("person@example.com");
        mail.setSubject("Mail from Spring Boot Notifications!");
        mail.setTo(email);
        mail.setText(content);

        javaMailSender.send(mail);
    }

}
```

It takes a _to_ email address and a ```String``` containing email content.



The test

```java
@Test
void testSendEmailToUser() throws Exception {

    notificationService.notifyUser("user@example.com", "Hello, world!");

    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
    assertEquals(1, receivedMessages.length);

    MimeMessage message = receivedMessages[0];

    assertEquals("Hello, world!", GreenMailUtil.getBody(message));
    assertEquals("user@example.com", message.getAllRecipients()[0].toString());

}
```

uses the notification service to send an email to an in-process GreenMail server.  

### Email Route IT

```c.e.e.EmailRouteIT``` specifies an integration test for an email sender handled by an [Apache Camel](http://camel.apache.org) route.  This type of deployment can be useful for sending an email in response to a message, file, or simple HTTP handler by configuring an appropriate route typical for Camel.

The route definition

```java
@Component
public class EmailRouteBuilder extends RouteBuilder {

    public static final String ENDPOINT = "direct:testMailerEndpoint";

    @Override
    public void configure() throws Exception {

        //This is a direct endpoint for purposes of this example, but can be from any of the inputs that Camel EIP supports.
        from(ENDPOINT)
            .doTry()
                .setHeader("subject", constant("Hello, world!"))
                .setHeader("to", constant("user@example.com"))
                .setHeader("java.smtp.from", method(UUID.class, "randomUUID"))
                .to("smtp://127.0.0.1:3025?username=spring&password=spring")
            .doCatch(Exception.class)
                .log(String.format("Caught an exception on %s: %s", ENDPOINT);
            .end();
    }

    
    
}
```

used for this example simply registers a ```direct:``` route, but can be any inputs that are supported in a Camel context.

When using ``smtp://``, the subject, _to_ email address, and other configuration can be set as message headers for the exchange.  Content comes from the message body, as shown in the test.

```java
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
```

Outside of the variations in triggering the call for Camel vs. a Spring Boot service, it should be noted that the assertions and configuration for the stand-in SMTP server are exactly the same.

### Reference Documentation
For further reference, please consider the following sections:

* [Java Mail Sender](https://docs.spring.io/spring-boot/docs/2.4.5/reference/htmlsingle/#boot-features-email)
* [Using Apache Camel with Spring Boot](https://camel.apache.org/camel-spring-boot/latest/spring-boot.html)

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)


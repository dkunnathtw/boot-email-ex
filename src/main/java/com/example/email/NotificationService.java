package com.example.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

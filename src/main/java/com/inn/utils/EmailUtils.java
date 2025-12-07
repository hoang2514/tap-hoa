package com.inn.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOldPasswordEmail(String email, String password){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nguyenlequochung3011@gmail.com");
        message.setTo(email);
        message.setSubject("Old password");
        message.setText("Old password: " + password);

        mailSender.send(message);
    }
}

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

    public void sendOTPEmail(String email, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nguyenlequochung3011@gmail.com");
        message.setTo(email);
        message.setSubject("Mã OTP đăng ký tài khoản");
        message.setText("Mã OTP của bạn là: " + otp + "\n\nMã này có hiệu lực trong 30 giây.");

        mailSender.send(message);
    }
}

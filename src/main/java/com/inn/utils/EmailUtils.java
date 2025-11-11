package com.inn.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailUtils {
    
    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text, List<String> list) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(""); // thêm địa chỉ email vào đây, email phải giống với ở spring.mail.username trong application.properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if (list != null && list.size() > 0) {
            message.setCc(getCcArray(list));
        }
        emailSender.send(message);
    }

    private String[] getCcArray(List<String> ccList) {
        String[] cc = new String[ccList.size()];
        for (int i = 0; i < ccList.size(); i++) {
            cc[i] = ccList.get(i);
        }
        return cc;
    }
}

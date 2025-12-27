package com.inn.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(Map<String, String> requestMap);
    ResponseEntity<?> verifyOTP(Map<String, String> requestMap);
    ResponseEntity<String> resendOTP(Map<String, String> requestMap);
    ResponseEntity<String> changePassword(Map<String, String> requestMap);
    ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
    ResponseEntity<String> login(Map<String, String> requestMap);
}

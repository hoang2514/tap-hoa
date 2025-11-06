package com.inn.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(Map<String, String> requestMap);
    ResponseEntity<String> changePassword(Map<String, String> requestMap);
    ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
}

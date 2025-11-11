package com.inn.service;

import org.springframework.http.ResponseEntity;

import com.inn.wrapper.UserWrapper;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<String> signUp(Map<String, String> requestMap);

    ResponseEntity<String> login(Map<String, String> requestMap);

    ResponseEntity<List<UserWrapper>> getAllUser();

}

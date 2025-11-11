package com.inn.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.inn.service.UserService;
import com.inn.wrapper.UserWrapper;

public class UserServiceImpl implements UserService {

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'signUp'");
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            // if (jwtFilter.isAdmin()) {
            //     return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
            // } else {
            //     return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}

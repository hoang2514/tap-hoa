package com.inn.restImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.inn.rest.UserRest;
// import com.inn.service.UserService;
import com.inn.wrapper.UserWrapper;

public class UserRestImpl implements UserRest{
    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            // return UserService.getAllUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

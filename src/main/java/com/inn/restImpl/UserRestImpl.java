package com.inn.restImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.inn.constants.TaphoaConstants;
import com.inn.rest.UserRest;
import com.inn.service.UserService;
import com.inn.utils.TaphoaUtils;
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
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            // return UserService.update(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        } return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

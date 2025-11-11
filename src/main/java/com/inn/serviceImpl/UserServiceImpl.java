package com.inn.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.inn.POJO.User;
import com.inn.constants.TaphoaConstants;
import com.inn.dao.UserDao;
import com.inn.service.UserService;
import com.inn.utils.TaphoaUtils;
import com.inn.wrapper.UserWrapper;

public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

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

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            // if (jwtFilter.isAdmin()) {
            //     Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
            //     if (!optional.isEmpty()) {
            //         userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
            //         return TaphoaUtils.getResponseEntity("User status updated successfully.", HttpStatus.OK);
            //     } else {
            //         return TaphoaUtils.getResponseEntity("User ID does not exist.", HttpStatus.OK);
            //     }
            // } else {
            //     return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}

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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserRestImpl implements UserRest {

    @Autowired
    UserService userService;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try {
            return userService.signUp(requestMap);
        } catch (Exception exception) {
            log.error("Exception in signUp", exception);
        }
        return null;
    }

    @Override
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap) {
        try {
            return userService.changePassword(requestMap);
        }
        catch (Exception exception) {
            log.error("Exception in changePassword", exception);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            return userService.forgotPassword(requestMap);
        }
        catch (Exception exception) {
            return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            return userService.login(requestMap);
        } catch (Exception ex) {
            log.error("Exception in login", ex);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            return userService.update(requestMap);
        } catch (Exception exception) {
            log.error("Exception in update", exception);
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            return userService.getAllUser();
        } catch (Exception exception) {
            log.error("Exception in getAllUser", exception);
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

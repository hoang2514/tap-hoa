package com.inn.serviceImpl;

import com.inn.POJO.User;
import com.inn.constants.TaphoaConstants;
import com.inn.dao.UserDao;
import com.inn.service.UserService;
import com.inn.utils.TaphoaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try{
            if(validateSignUpMap(requestMap)) {
                // Find user information in database using DAO
                User user = userDao.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return TaphoaUtils.getResponseEntity("User signed up successfully", HttpStatus.OK);
                }
                else {
                    return TaphoaUtils.getResponseEntity("User already exists", HttpStatus.BAD_REQUEST);
                }
            }
            else {
                return TaphoaUtils.getResponseEntity("Error signing up user", HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            return TaphoaUtils.getResponseEntity("Exception while signing up", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("email") && requestMap.containsKey("password") &&
        requestMap.containsKey("name") && requestMap.containsKey("contactNumber");
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setRole("user");
        return user;
    }
}

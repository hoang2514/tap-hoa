package com.inn.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.inn.constants.TaphoaConstants;
import com.inn.JWT.CustomerUsersDetailsService;
import com.inn.JWT.JwtFilter;
import com.inn.JWT.JwtUtil;
import com.inn.POJO.User;
import com.inn.dao.UserDao;
import com.inn.service.UserService;
import com.inn.utils.EmailUtils;
import com.inn.utils.TaphoaUtils;
import com.inn.wrapper.UserWrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    JwtFilter jwtFilter;
  
    @Autowired
    UserDao userDao;

    @Autowired
    EmailUtils emailUtils;
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                    return TaphoaUtils.getResponseEntity("User status updated successfully.", HttpStatus.OK);
                } else {
                    return TaphoaUtils.getResponseEntity("User ID does not exist.", HttpStatus.OK);
                }
            } else {
                return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account approved", 
                "USER: "+user+" \n is approved by \nADMIN: "+ jwtFilter.getCurrentUser(), allAdmin);
        } else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account disabled", 
                "USER: "+user+" \n is disabled by \nADMIN: "+ jwtFilter.getCurrentUser(), allAdmin);
        }
    }

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

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user)) {
                if (user.getPassword().equals(requestMap.get("oldPassword"))) {
                    user.setPassword(requestMap.get("newPassword"));
                    userDao.save(user);
                    return TaphoaUtils.getResponseEntity("Password changed successfully", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("Old password does not match", HttpStatus.BAD_REQUEST);
            }
            return TaphoaUtils.getResponseEntity("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Exception e) {
            return TaphoaUtils.getResponseEntity("Exception while changing password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && user.getEmail().equals(requestMap.get("email"))) {
                emailUtils.sendOldPasswordEmail(user.getEmail(),user.getPassword());
            }
            return TaphoaUtils.getResponseEntity("Check your email for password.", HttpStatus.OK);
        }
        catch (Exception e) {
            return TaphoaUtils.getResponseEntity("Exception while forgot password", HttpStatus.INTERNAL_SERVER_ERROR);
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

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));

            if (auth.isAuthenticated()) {
                if (customerUsersDetailsService.getUserDetail().getStatus() != null) {
                    if (customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                        return new ResponseEntity<>("{\"token\":\"" + jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(), customerUsersDetailsService.getUserDetail().getRole()) + "\"}", HttpStatus.OK);
                    }
                } else {
                    return new ResponseEntity<>("{\"message\":\"Wait for admin approval.\"}", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred while login", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Bad Credentials.\"}", HttpStatus.BAD_REQUEST);
    }
}

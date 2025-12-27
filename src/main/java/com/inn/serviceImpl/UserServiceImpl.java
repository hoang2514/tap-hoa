package com.inn.serviceImpl;

import com.inn.JWT.CustomerUsersDetailsService;
import com.inn.JWT.JwtFilter;
import com.inn.JWT.JwtUtil;
import com.inn.POJO.User;
import com.inn.dao.UserDao;
import com.inn.service.UserService;
import com.inn.utils.EmailUtils;
import com.inn.utils.OtpUtils;
import com.inn.utils.TaphoaUtils;
import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

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

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try{
            if(validateSignUpMap(requestMap)) {
                // Find user information in database using DAO
                User user = userDao.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    // Generate OTP
                    String otp = OtpUtils.generateOTP();
                    String email = requestMap.get("email");
                    
                    // Store user data temporarily in Redis with OTP
                    Map<String, String> signupData = new HashMap<>();
                    signupData.put("email", requestMap.get("email"));
                    signupData.put("password", requestMap.get("password"));
                    signupData.put("name", requestMap.get("name"));
                    signupData.put("contactNumber", requestMap.get("contactNumber"));
                    signupData.put("otp", otp);
                    signupData.put("otpTimestamp", String.valueOf(System.currentTimeMillis()));
                    
                    Gson gson = new Gson();
                    String signupDataJson = gson.toJson(signupData);
                    
                    // Store in Redis with 5 minutes expiration (allows multiple OTP resends)
                    // Each OTP is valid for 30 seconds, but signup data stays for 5 minutes
                    String redisKey = "signup:" + email;
                    redisTemplate.opsForValue().set(redisKey, signupDataJson, 5, TimeUnit.MINUTES);
                    
                    // Send OTP email
                    emailUtils.sendOTPEmail(email, otp);
                    
                    return TaphoaUtils.getResponseEntity("OTP đã được gửi đến email của bạn. Vui lòng kiểm tra email và nhập mã OTP.", HttpStatus.OK);
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
            log.error("Exception while signing up", e);
            return TaphoaUtils.getResponseEntity("Exception while signing up", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> verifyOTP(Map<String, String> requestMap) {
        try {
            String email = requestMap.get("email");
            String otp = requestMap.get("otp");
            
            if (email == null || otp == null) {
                return TaphoaUtils.getResponseEntity("Email và OTP là bắt buộc", HttpStatus.BAD_REQUEST);
            }
            
            // Get signup data from Redis
            String redisKey = "signup:" + email;
            String signupDataJson = redisTemplate.opsForValue().get(redisKey);
            
            if (signupDataJson == null) {
                return TaphoaUtils.getResponseEntity("OTP đã hết hạn hoặc không tồn tại. Vui lòng gửi lại mã OTP.", HttpStatus.BAD_REQUEST);
            }
            
            // Parse signup data
            Gson gson = new Gson();
            Map<String, String> signupData = gson.fromJson(signupDataJson, new TypeToken<Map<String, String>>(){}.getType());
            
            // Verify OTP
            if (!otp.equals(signupData.get("otp"))) {
                return TaphoaUtils.getResponseEntity("Mã OTP không đúng", HttpStatus.BAD_REQUEST);
            }
            
            // Check if OTP is still valid (within 30 seconds)
            String otpTimestampStr = signupData.get("otpTimestamp");
            if (otpTimestampStr != null) {
                try {
                    long otpTimestamp = Long.parseLong(otpTimestampStr);
                    long currentTime = System.currentTimeMillis();
                    long elapsedSeconds = (currentTime - otpTimestamp) / 1000;
                    
                    if (elapsedSeconds > 30) {
                        return TaphoaUtils.getResponseEntity("Mã OTP đã hết hạn. Vui lòng gửi lại mã OTP.", HttpStatus.BAD_REQUEST);
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid OTP timestamp format", e);
                }
            }
            
            // OTP is correct, create user account
            User user = new User();
            user.setEmail(signupData.get("email"));
            user.setPassword(signupData.get("password"));
            user.setName(signupData.get("name"));
            user.setContactNumber(signupData.get("contactNumber"));
            user.setRole("user");
            user.setStatus("true");
            
            userDao.save(user);
            
            // Delete signup data from Redis
            redisTemplate.delete(redisKey);
            
            return TaphoaUtils.getResponseEntity("Đăng ký thành công", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception while verifying OTP", e);
            return TaphoaUtils.getResponseEntity("Exception while verifying OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> resendOTP(Map<String, String> requestMap) {
        try {
            String email = requestMap.get("email");
            
            if (email == null) {
                return TaphoaUtils.getResponseEntity("Email là bắt buộc", HttpStatus.BAD_REQUEST);
            }
            
            // Check if user already exists
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null) {
                return TaphoaUtils.getResponseEntity("User already exists", HttpStatus.BAD_REQUEST);
            }
            
            // Get existing signup data from Redis
            String redisKey = "signup:" + email;
            String signupDataJson = redisTemplate.opsForValue().get(redisKey);
            
            if (signupDataJson == null) {
                return TaphoaUtils.getResponseEntity("Không tìm thấy thông tin đăng ký. Vui lòng đăng ký lại.", HttpStatus.BAD_REQUEST);
            }
            
            // Parse existing signup data
            Gson gson = new Gson();
            Map<String, String> signupData = gson.fromJson(signupDataJson, new TypeToken<Map<String, String>>(){}.getType());
            
            // Generate new OTP
            String newOtp = OtpUtils.generateOTP();
            signupData.put("otp", newOtp);
            signupData.put("otpTimestamp", String.valueOf(System.currentTimeMillis()));
            
            // Store updated data in Redis with 5 minutes expiration (reset timer)
            String updatedSignupDataJson = gson.toJson(signupData);
            redisTemplate.opsForValue().set(redisKey, updatedSignupDataJson, 5, TimeUnit.MINUTES);
            
            // Send new OTP email
            emailUtils.sendOTPEmail(email, newOtp);
            
            return TaphoaUtils.getResponseEntity("Mã OTP mới đã được gửi đến email của bạn. Vui lòng kiểm tra email.", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception while resending OTP", e);
            return TaphoaUtils.getResponseEntity("Exception while resending OTP", HttpStatus.INTERNAL_SERVER_ERROR);
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

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    String otp = OtpUtils.generateOTP();
                    String email = requestMap.get("email");

                    Map<String, String> signupData = new HashMap<>(requestMap);
                    signupData.put("otp", otp);
                    signupData.put("otpTimestamp", String.valueOf(System.currentTimeMillis()));

                    String redisKey = "signup:" + email;
                    redisTemplate.opsForValue().set(redisKey, new Gson().toJson(signupData), 5, TimeUnit.MINUTES);

                    emailUtils.sendOTPEmail(email, otp);
                    return TaphoaUtils.getResponseEntity("OTP đã được gửi đến email.", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("User already exists", HttpStatus.BAD_REQUEST);
            }
            return TaphoaUtils.getResponseEntity("Invalid data", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("SignUp error", e);
            return TaphoaUtils.getResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> verifyOTP(Map<String, String> requestMap) {
        try {
            String email = requestMap.get("email");
            String otp = requestMap.get("otp");
            String redisKey = "signup:" + email;
            String signupDataJson = redisTemplate.opsForValue().get(redisKey);

            if (signupDataJson == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "OTP hết hạn hoặc không tồn tại."));
            }

            Map<String, String> signupData = new Gson().fromJson(signupDataJson, new TypeToken<Map<String, String>>(){}.getType());

            if (!otp.equals(signupData.get("otp"))) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP không đúng"));
            }

            long otpTimestamp = Long.parseLong(signupData.get("otpTimestamp"));
            if ((System.currentTimeMillis() - otpTimestamp) / 1000 > 30) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP đã hết hạn."));
            }

            User user = new User();
            user.setEmail(signupData.get("email"));
            user.setName(signupData.get("name"));
            user.setContactNumber(signupData.get("contactNumber"));
            user.setRole("user");
            user.setStatus("true");
            // Hash password trước khi lưu
            user.setPassword(passwordEncoder.encode(signupData.get("password")));

            userDao.save(user);
            redisTemplate.delete(redisKey);

            return ResponseEntity.ok(Map.of("message", "Đăng ký thành công"));
        } catch (Exception e) {
            log.error("VerifyOTP error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error"));
        }
    }


    @Override
    public ResponseEntity<String> resendOTP(Map<String, String> requestMap) {
        try {
            String email = requestMap.get("email");
            String redisKey = "signup:" + email;
            String signupDataJson = redisTemplate.opsForValue().get(redisKey);

            if (signupDataJson == null) {
                return TaphoaUtils.getResponseEntity("Không tìm thấy thông tin đăng ký.", HttpStatus.BAD_REQUEST);
            }

            Map<String, String> signupData = new Gson().fromJson(signupDataJson, new TypeToken<Map<String, String>>(){}.getType());
            String newOtp = OtpUtils.generateOTP();
            signupData.put("otp", newOtp);
            signupData.put("otpTimestamp", String.valueOf(System.currentTimeMillis()));

            redisTemplate.opsForValue().set(redisKey, new Gson().toJson(signupData), 5, TimeUnit.MINUTES);
            emailUtils.sendOTPEmail(email, newOtp);

            return TaphoaUtils.getResponseEntity("Mã OTP mới đã được gửi.", HttpStatus.OK);
        } catch (Exception e) {
            log.error("ResendOTP error", e);
            return TaphoaUtils.getResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (user != null) {
                // Kiểm tra password cũ bằng matches()
                if (passwordEncoder.matches(requestMap.get("oldPassword"), user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
                    userDao.save(user);
                    return TaphoaUtils.getResponseEntity("Password changed successfully", HttpStatus.OK);
                }
                return TaphoaUtils.getResponseEntity("Old password does not match", HttpStatus.BAD_REQUEST);
            }
            return TaphoaUtils.getResponseEntity("User not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return TaphoaUtils.getResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if (user != null) {
                // Tạo mật khẩu ngẫu nhiên 8 ký tự
                String tempPassword = UUID.randomUUID().toString().substring(0, 8);
                user.setPassword(passwordEncoder.encode(tempPassword));
                userDao.save(user);

                emailUtils.sendOTPEmail(user.getEmail(), "Mật khẩu mới của bạn là: " + tempPassword);
            }
            return TaphoaUtils.getResponseEntity("Nếu email tồn tại, mật khẩu mới đã được gửi.", HttpStatus.OK);
        } catch (Exception e) {
            return TaphoaUtils.getResponseEntity("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("email") && requestMap.containsKey("password") &&
        requestMap.containsKey("name") && requestMap.containsKey("contactNumber");
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if (auth.isAuthenticated()) {
                User user = customerUsersDetailsService.getUserDetail();
                if ("true".equalsIgnoreCase(user.getStatus())) {
                    return new ResponseEntity<>("{\"token\":\"" + jwtUtil.generateToken(user.getEmail(), user.getRole()) + "\"}", HttpStatus.OK);
                }
                return new ResponseEntity<>("{\"message\":\"Wait for admin approval.\"}", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            log.error("Login failed", ex);
        }
        return new ResponseEntity<>("{\"message\":\"Bad Credentials.\"}", HttpStatus.BAD_REQUEST);
    }
}

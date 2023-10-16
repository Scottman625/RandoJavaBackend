package com.rando.springboot.randoJavaBackend.rest;

import com.rando.springboot.randoJavaBackend.entity.ApiResponse;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.security.JwtTokenProvider;
import com.rando.springboot.randoJavaBackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;  // 這個是用來處理JWT的組件，您可能需要自己實現。

    @PostMapping("/register/")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@RequestParam String phone, @RequestParam String password) {
        User existingUser = userService.findByPhoneAndPassword(phone, password);
        if (existingUser != null) {
            return new ResponseEntity<>(new ApiResponse<>(null, "This phone number is already been used."), HttpStatus.UNAUTHORIZED); // 或者其他您希望的錯誤響應
        }
        User newUser = userService.createUser(phone, password);
        if (newUser != null) {
            String username = "New User" + newUser.getId();
            String token = jwtTokenProvider.createToken(username);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);
            return new ResponseEntity<>(new ApiResponse<>(tokenMap, "success login"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse<>(null, "login failed"), HttpStatus.UNAUTHORIZED);
        }
//        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login/")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestParam("username") String phone, @RequestParam("password") String password) {

        User user = userService.validateUser(phone, password);
        if (user != null) {
            String username = user.getUsername();
            String token = jwtTokenProvider.createToken(username);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);
            return new ResponseEntity<>(new ApiResponse<>(tokenMap, "success login"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse<>(null, "login failed"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/me/")
    public ResponseEntity<User> getMe(HttpServletRequest request) {
        log.info("Entering getMe method");

        // 從請求頭中取得token
        String token = jwtTokenProvider.resolveToken(request);
        log.info("Resolved token: " + token);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            log.warn("Token validation failed");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // 從token中取得使用者ID或使用者名稱
        String username = jwtTokenProvider.getUsername(token);
        log.info("Resolved username: " + username);

        // 使用ID或使用者名稱從數據庫中獲取使用者詳情
        User user = userService.findByUsername(username);

        if (user == null) {
            log.warn("User not found for username: " + username);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        log.info("Found user: " + user.toString());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}



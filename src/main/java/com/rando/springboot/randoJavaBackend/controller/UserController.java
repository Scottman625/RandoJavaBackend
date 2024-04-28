package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dto.UserDTO;
import com.rando.springboot.randoJavaBackend.entity.ApiResponse;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.security.JwtTokenProvider;
import com.rando.springboot.randoJavaBackend.service.JwtService;
import com.rando.springboot.randoJavaBackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;  // 這個是用來處理JWT的組件，您可能需要自己實現。

    @Autowired
    private JwtService jwtService;  // 這個是用來處理JWT的組件，您可能需要自己實現。

    @PostMapping("/register/")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@RequestParam String phone,@RequestParam String username, @RequestParam String password) {
        User existingUser = userService.findByPhone(phone);
        if (existingUser != null) {
            log.info("This phone number is already been used.");
            return new ResponseEntity<>(new ApiResponse<>(null, "This phone number is already been used."), HttpStatus.UNAUTHORIZED); // 或者其他您希望的錯誤響應
        }
        User newUser = userService.createUser(phone, username, password);
        if (newUser != null) {
            String userphone = newUser.getPhone();
            String token = jwtTokenProvider.createToken(userphone);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);
            return new ResponseEntity<>(new ApiResponse<>(tokenMap, "success login"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse<>(null, "register failed"), HttpStatus.UNAUTHORIZED);
        }
//        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login/")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestParam("username") String phone, @RequestParam("password") String password) {

        User user = userService.validateUser(phone, password);
        if (user != null) {
//            String username = user.getUsername();
            String token = jwtTokenProvider.createToken(phone);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);
            return new ResponseEntity<>(new ApiResponse<>(tokenMap, "success login"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse<>(null, "login failed"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/get_user_id/")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserId(HttpServletRequest request) {

        try {
            log.info("getUserId");
            User user = jwtService.tokenGetUser(request);

            Map<String, String> userIdMap = new HashMap<>();
            String userId = Long.toString(user.getId());
            log.info("userId: " + userId);
            userIdMap.put("userId", userId);
            return  new ResponseEntity<>(new ApiResponse<>(userIdMap, "success getUserId"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/me/")
    public ResponseEntity<UserDTO> getMe(HttpServletRequest request) {

        try {
            User user = jwtService.tokenGetUser(request);
            UserDTO userDTO = userService.convertToUserDTO(user);

            log.info("Found user: " + user.toString());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/get_user/")
    public ResponseEntity<UserDTO> getUser(HttpServletRequest request) {

        try {
            User user = jwtService.tokenGetUser(request);
            UserDTO userDTO = userService.convertToUserDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}



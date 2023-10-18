package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    public User tokenGetUser(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);

        // 從token中取得使用者ID或使用者名稱
        String username = jwtTokenProvider.getUsername(token);

        // 使用ID或使用者名稱從數據庫中獲取使用者詳情
        User user = userService.findByUsername(username);

        return user;
    }
}

package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dto.UserDTO;
import com.rando.springboot.randoJavaBackend.dto.UserImageDTO;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserImage;
import com.rando.springboot.randoJavaBackend.security.JwtTokenProvider;
import com.rando.springboot.randoJavaBackend.service.JwtService;
import com.rando.springboot.randoJavaBackend.service.UserImageService;
import com.rando.springboot.randoJavaBackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserImageController {
    @Autowired
    private UserImageService userImageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;  // 這個是用來處理JWT的組件，您可能需要自己實現。

    @PostMapping("/update_user_images")
    public ResponseEntity<?> uploadUserImage(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
        // get authenticated user, similar to Django's self.request.user
        User user = jwtService.tokenGetUser(request);

        userImageService.createUserImage(user, image);
        // convert to DTO and return...
        UserDTO userDTO = userService.convertToUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/update_user_images/{id}/")
    public ResponseEntity<?> updateUserImage(@PathVariable Long id, @RequestParam("image") MultipartFile newImage, HttpServletRequest request) {
        User user = jwtService.tokenGetUser(request);

        userImageService.updateUserImage(user, id, newImage);
        // 將 updatedImage 轉換為 DTO，然後回傳...

        UserDTO userDTO = userService.convertToUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/update_user_images/{id}/")
    public ResponseEntity<?> deleteUserImage(@PathVariable Long id, HttpServletRequest request) {
        User user = jwtService.tokenGetUser(request);

        userImageService.deleteUserImage(user, id);
        // convert to DTO and return...
        UserDTO userDTO = userService.convertToUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    // additional endpoints...
}


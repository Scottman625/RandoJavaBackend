package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dto.ChatMessageDTO;
import com.rando.springboot.randoJavaBackend.entity.ApiResponse;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.service.ChatroomMessageService;
import com.rando.springboot.randoJavaBackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatMessageController {
    @Autowired
    private ChatroomMessageService chatroomMessageService;

    @Autowired
    private JwtService jwtService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @PostMapping("/refresh_chatMessages")
    public ResponseEntity<String> refreshChatMessages(@RequestParam Long chatroomId, HttpServletRequest request) {
        User user = jwtService.tokenGetUser(request);
        chatroomMessageService.refreshChatMessages(user, chatroomId);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@RequestParam("chatroom_id") Long chatroomId, HttpServletRequest request) {
        try {
            User user = jwtService.tokenGetUser(request);
            List<ChatMessageDTO> messages = chatroomMessageService.getMessages(chatroomId, user);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }


    }
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> postMessage(
            @RequestParam Long chatroomId,
            @RequestParam(required = false) String content,
            @RequestParam(name = "image", required = false) MultipartFile image,
            HttpServletRequest request) {
        User user = jwtService.tokenGetUser(request);
        log.info("a");
        if(!content.isEmpty()){
            log.info("b");
            return ResponseEntity.ok(new ApiResponse<>(chatroomMessageService.postMessage(user, chatroomId,content,null),"Post content success"));
        } else if (!image.isEmpty()) {
            log.info("c");
            return ResponseEntity.ok(new ApiResponse<>(chatroomMessageService.postMessage(user, chatroomId,null,image),"Post image success"));
        }
        log.info("d");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null,"Failed to post message"));
    }

}


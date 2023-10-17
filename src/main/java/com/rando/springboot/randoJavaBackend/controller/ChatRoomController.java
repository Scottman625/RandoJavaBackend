package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.ResourceNotFoundException;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.security.JwtTokenProvider;
import com.rando.springboot.randoJavaBackend.service.ChatRoomService;
import com.rando.springboot.randoJavaBackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private UserRepository userRepository; // 假設您有一個User的Repository

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> getChatRoomsForUser(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "yes") String isChat
    ) {
        boolean shouldIncludeChats = !isChat.equals("no");
        List<ChatRoomDTO> chatRooms = chatRoomService.getChatRoomsForUser(user, shouldIncludeChats);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/{chatroomId}/")
    public ResponseEntity<ChatRoomDTO> retrieveChatRoom(
            @PathVariable Long chatroomId,
            HttpServletRequest request
    ) {
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
        ChatRoomDTO chatRoom = chatRoomService.retrieveChatRoom(user, chatroomId);
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestBody String otherSideUserPhone,HttpServletRequest request
    ) {
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
        return ResponseEntity.ok(chatRoomService.createChatRoom(user,otherSideUserPhone));
    }

    @GetMapping("/deleteUserChatRoom")
    public ResponseEntity<List<ChatRoomDTO>> deleteUserChatRoom(@RequestParam(name = "is_chat", required = false) String isChat,@RequestBody String otherSideUserPhone,HttpServletRequest request) {
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
        User currentUser = userService.findByUsername(username);
        User otherSideUser = userRepository.findByPhone(otherSideUserPhone).orElseThrow(() -> new ResourceNotFoundException("Other side user not found"));

        return ResponseEntity.ok(chatRoomService.deleteUserChatRoom(currentUser,otherSideUser,isChat));


    }
}




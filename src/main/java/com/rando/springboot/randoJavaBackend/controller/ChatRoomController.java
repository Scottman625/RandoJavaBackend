package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.ResourceNotFoundException;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.security.JwtTokenProvider;
import com.rando.springboot.randoJavaBackend.service.ChatRoomService;
import com.rando.springboot.randoJavaBackend.service.JwtService;
import com.rando.springboot.randoJavaBackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> getChatRoomsForUser(
            @RequestParam(required = false, defaultValue = "yes") String isChat,
            HttpServletRequest request
    ) {
        boolean shouldIncludeChats = !isChat.equals("no");
        try{
            User user = jwtService.tokenGetUser(request);
            List<ChatRoomDTO> chatRooms = chatRoomService.getChatRoomsForUser(user, shouldIncludeChats);
            return ResponseEntity.ok(chatRooms);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{chatroomId}/")
    public ResponseEntity<List<ChatRoomDTO>> retrieveChatRoom(
            @PathVariable Long chatroomId,
            HttpServletRequest request
    ) {
        try{
            User user = jwtService.tokenGetUser(request);
            List<ChatRoomDTO> chatRoom = chatRoomService.retrieveChatRoom(user, chatroomId);
            return ResponseEntity.ok(chatRoom);
        }catch (Exception e){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestParam String otherSideUserPhone,HttpServletRequest request
    ) {
        log.info("TEST");
        try{
            User user = jwtService.tokenGetUser(request);
            return ResponseEntity.ok(chatRoomService.createChatRoom(user,otherSideUserPhone));
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @DeleteMapping
    public ResponseEntity<List<ChatRoomDTO>> deleteUserChatRoom(@RequestParam(name = "is_chat", required = false) String isChat,@RequestBody String otherSideUserPhone,HttpServletRequest request) {
        try{
            User currentUser = jwtService.tokenGetUser(request);
            User otherSideUser = userRepository.findByPhone(otherSideUserPhone).orElseThrow(() -> new ResourceNotFoundException("Other side user not found"));

            return ResponseEntity.ok(chatRoomService.deleteUserChatRoom(currentUser,otherSideUser,isChat));
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}




package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.dao.ChatRoomRepository;
import com.rando.springboot.randoJavaBackend.dto.ChatMessageDTO;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WebSocketService {

    @Autowired
    private ObjectMapper objectMapper;
    // Store active sessions by user id or some identifier for targeted messaging
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    public void addSession(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }
    public void removeSession(String userId) {
        sessions.remove(userId);
    }
    public void registerSession(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }
    public void unregisterSession(String userId) {
        sessions.remove(userId);
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public void chatrooms(String roomName, ChatRoomDTO chatroom, Optional<List<ChatMessageDTO>> optionalMessages) {

        String destination = "/topic/" + "chatRoomMessages_" + roomName;
        Map<String, Object> payload = new HashMap<>();
        payload.put("chatrooms", chatroom);
        optionalMessages.ifPresent(messages -> payload.put("messages", messages));
        messagingTemplate.convertAndSend(destination, payload);
    }


}


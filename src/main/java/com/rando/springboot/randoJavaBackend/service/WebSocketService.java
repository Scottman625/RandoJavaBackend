package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.ChatRoomRepository;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
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

    @Autowired
    private ChatRoomRepository chatRoomRepository;

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

    public void sendChatRoomUpdate(ChatRoomDTO chatRoomDTO) {
        // Convert the DTO to JSON or the message format you're using
        String messagePayload = "";
        try {
            messagePayload = objectMapper.writeValueAsString(chatRoomDTO);
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace();
        }
        long id = chatRoomDTO.getChatroomId();
        // Assume chatRoomDTO has participant userIds as a List<String>
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(id);

            List<User> users = chatRoomRepository.findAllUsersByChatRoom(optionalChatRoom.get());
            List<Long> integerList = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            List<String> participantUserIds = integerList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            // your other code here...

//        List<String> participantUserIds = chatRoomRepository.findAllUsersByChatRoom(chatRoomRepository.findById(id));

        for (String userId : participantUserIds) {
            WebSocketSession session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messagePayload));
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle exception - maybe close the session or inform the user
                }
            }
        }
    }

}


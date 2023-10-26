package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.dao.ChatroomMessageRepository;
import com.rando.springboot.randoJavaBackend.dto.ChatMessageDTO;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @MessageMapping("/app/chatRoomMessages_{roomName}") // Added the /app prefix here
    @SendTo("/topic/chatRoomMessages_{roomName}")   // the client will listen to this topic to get the response
    public Map<String, Object> sendMessage(@DestinationVariable String roomName, Map<String, Object>chatroomMessages) {
        logger.info("Received message for room {}: {}", roomName, chatroomMessages);
        return chatroomMessages;
    }





    // You can add more handlers if necessary...
}



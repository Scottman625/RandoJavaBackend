package com.rando.springboot.randoJavaBackend.controller;

import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatRoomMessageController {

    @MessageMapping("/chat/send")
    @SendTo("/topic/chatRoomMessages/{roomName}")
    public ChatroomMessage sendMessage(@DestinationVariable String roomName, ChatroomMessage chatroomMessage) {
        return chatroomMessage;
    }
}

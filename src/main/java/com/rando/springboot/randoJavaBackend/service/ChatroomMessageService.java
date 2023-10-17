package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.ChatroomMessageRepository;
import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ChatroomMessageService {

    @Autowired
    private ChatroomMessageRepository chatroomMessageRepository;

    public boolean shouldShowSendTime(ChatroomMessage message) {
        List<ChatroomMessage> messages = chatroomMessageRepository.findBySenderAndCreateAtLessThanAndChatroomAndIdNot(
                message.getSender(), message.getCreateAt(), message.getChatroom(), message.getId());

        ChatroomMessage lastMessage = messages.isEmpty() ? null : messages.get(0);

        Duration duration = Duration.between(lastMessage.getCreateAt(), message.getCreateAt());
        long diffInMillis = duration.toMillis();

        if (lastMessage == null || diffInMillis > 600000) {
            return true;
        } else {
            return false;
        }
    }
}


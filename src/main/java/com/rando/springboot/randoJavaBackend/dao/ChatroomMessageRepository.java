package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ChatroomMessageRepository extends JpaRepository<ChatroomMessage, Long> {

    List<ChatroomMessage> findBySenderAndCreateAtLessThanAndChatroomAndIdNot(User sender, Date createAt, ChatRoom chatroom, Long id);

}


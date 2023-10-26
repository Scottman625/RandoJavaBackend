package com.rando.springboot.randoJavaBackend.dao;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatroomMessageRepository extends JpaRepository<ChatroomMessage, Long> {

    List<ChatroomMessage> findBySenderAndCreateAtLessThanAndChatroomAndIdNot(User sender, LocalDateTime createAt, ChatRoom chatroom, Long id);

    int countByChatroomAndSenderAndIsReadByOtherSide(ChatRoom chatRoom, User otherSideUser, boolean b);

    int countByChatroomAndIsReadByOtherSideAndSenderNot(ChatRoom chatRoom, boolean b, User user);

    int countByChatroomAndSenderAndIsReadByOtherSideFalse(ChatRoom chatroom, User sender);

    long countByMatch(UserMatch match);

    List<ChatroomMessage> findByChatroomOrderByCreateAtDesc(ChatRoom chatroom);
    int countByChatroomAndIsReadByOtherSideFalseAndSenderNot(ChatRoom chatroom, User sender);

//    ChatroomMessage findFirstByChatroomOrderByCreatedAtDesc(ChatRoom chatRoom);

    @Query("SELECT m FROM ChatroomMessage m WHERE m.chatroom = :chatroom ORDER BY m.createAt Desc")
    Optional<ChatroomMessage> findFirstByChatroomOrderByCreateAt(@Param("chatroom") ChatRoom chatroom);
//    @Query("SELECT m FROM ChatroomMessage m WHERE m.chatroom = :chatroom")
    Optional<ChatroomMessage> findFirstByChatroom(@Param("chatroom") ChatRoom chatroom);


    List<ChatroomMessage> findByChatroomOrderByCreateAtAsc(ChatRoom chatroom);

    @Modifying
    @Query("UPDATE ChatroomMessage m SET m.isReadByOtherSide = true WHERE m.chatroom = :chatroom AND m.sender != :user")
    void markAsReadByOtherSideWhereSenderIsNotUser(@Param("chatroom") ChatRoom chatroom, @Param("user") User user);

    int countByChatroom(ChatRoom eachChatroom);

    ChatroomMessage findFirstByChatroomOrderByCreateAtDesc(ChatRoom chatRoom);

    Optional<ChatroomMessage> findTopByChatroomOrderByCreateAtDesc(ChatRoom chatRoom);
}


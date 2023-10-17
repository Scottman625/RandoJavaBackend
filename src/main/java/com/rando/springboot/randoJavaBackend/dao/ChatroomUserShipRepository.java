package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.ChatroomUserShip;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface ChatroomUserShipRepository extends JpaRepository<ChatroomUserShip, Long> {
    @Query("SELECT cu.chatroom.id FROM ChatroomUserShip cu WHERE cu.user = :user")
    List<Long> findChatRoomIdsByUser(User user);

    @Query("SELECT cu.user FROM ChatroomUserShip cu WHERE cu.chatroom = :chatroom AND cu.user != :user")
    User findOtherSideUser(ChatRoom chatroom, User user);

    List<ChatroomUserShip> findByUser(User user);

    List<ChatroomUserShip> findByUserAndChatroomIdIn(User otherSideUser, Set<Long> chatroomsForUser1);

    List<Long> findChatroomIdsByUser(User user);
}

package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    ChatRoom findById(int id);

    List<ChatRoom> findByIdInOrderByUpdateAtDesc(List<Long> chatroomIds);

    ChatRoom findFirstByIdIn(Set<Long> user1Chatrooms);

//    List<ChatRoom> findChatroomsWithMessages(List<Long> chatroomIds);
    @Query("SELECT cm.chatroom FROM ChatroomMessage cm WHERE cm.chatroom.id in :chatroomIds order by cm.chatroom.updateAt DESC ") // Your JPQL query here
    List<ChatRoom> findChatroomsWithMessagesOrderByUpdateAtDesc(@Param("chatroomIds")List<Long> chatroomIds);

    @Query("SELECT cus.user FROM ChatroomUserShip cus WHERE cus.chatroom = :chatRoom AND cus.user <> :givenUser")
    User findOtherSideUser(@Param("givenUser") User givenUser, @Param("chatRoom") ChatRoom chatRoom);

    @Query("SELECT cus.user FROM ChatroomUserShip cus WHERE cus.chatroom = :chatRoom")
    List<User> findAllUsersByChatRoom(@Param("chatRoom") ChatRoom chatRoom);


    @Query("SELECT c FROM ChatRoom c WHERE (SELECT COUNT(m) FROM ChatroomMessage m WHERE m.chatroom = c) > 0 And c IN :otherSideChatRooms")
    List<ChatRoom> findChatroomsWithMessagesIn(List<ChatRoom> otherSideChatRooms);
}

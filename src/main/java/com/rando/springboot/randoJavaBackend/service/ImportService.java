package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.ChatRoomRepository;
import com.rando.springboot.randoJavaBackend.dao.ChatroomUserShipRepository;
import com.rando.springboot.randoJavaBackend.dao.MatchRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.ChatroomUserShip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rando.springboot.randoJavaBackend.entity.*;

import java.util.List;

@Service
public class ImportService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatroomUserShipRepository chatroomUserShipRepository;

    @Autowired
    private UserRepository userRepository;

    public void importUserMatch(){

        for(int i = 2; i <= 200; i+=3){
            for (int j = 200; j <= 400; j +=2 ){
                UserMatch userMatch = new UserMatch();
                userMatch.setUser1(userRepository.findById(i));
                userMatch.setUser2(userRepository.findById(j));
                matchRepository.save(userMatch);
            }
        }
    }

//    public void importChatRoom() {
//        List<UserMatch> matches = matchRepository.findAll();
//
//        for (UserMatch match : matches) {
//            ChatRoom chatroom = new ChatRoom();
//            chatRoomRepository.save(chatroom);
//
//            ChatroomUserShip chatroomUserShip1 = new ChatroomUserShip();
//            chatroomUserShip1.setChatroom(chatroom);
//            chatroomUserShip1.setUser(match.getUser1());
//            chatroomUserShipRepository.save(chatroomUserShip1);
//
//            ChatroomUserShip chatroomUserShip2 = new ChatroomUserShip();
//            chatroomUserShip2.setChatroom(chatroom);
//            chatroomUserShip2.setUser(match.getUser2());
//            chatroomUserShipRepository.save(chatroomUserShip2);
//        }
//    }

    public void importCareer() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            switch ((int)user.getId() % 3) {
                case 1:
                    user.setCareer("工程師");
                    break;
                case 2:
                    user.setCareer("護理師");
                    break;
                default:
                    user.setCareer("設計師");
            }
            userRepository.save(user);
        }
    }
}


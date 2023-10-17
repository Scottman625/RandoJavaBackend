package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.dao.*;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository; // 假設您有一個User的Repository

    @Autowired
    private ChatRoomRepository chatRoomRepository; // Your JPA repository

    @Autowired
    private ChatroomUserShipRepository chatroomUserShipRepository;

    @Autowired
    private ChatroomMessageRepository chatroomMessageRepository;

    @Autowired
    private S3Service s3Service; // The S3Service we defined previously

    @Autowired
    private WebSocketService webSocketService; // 假設有一個專門處理WebSocket的服務

    @Autowired
    private MatchRepository matchRepository;


    public List<ChatRoomDTO> getChatRoomsForUser(User user, boolean isChat) {
        List<Long> chatroomIds = chatroomUserShipRepository.findChatRoomIdsByUser(user);

        List<ChatRoom> chatRooms = chatRoomRepository.findByIdInOrderByUpdateAtDesc(chatroomIds);

        if (!isChat) {
            return convertToDTO(chatRooms,user); // Simple conversion if isChat is 'no'
        }

        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDTO dto = new ChatRoomDTO(chatRoom);

            User otherSideUser = chatroomUserShipRepository.findOtherSideUser(chatRoom, user);
            dto.setOtherSideUser(otherSideUser);

            int unreadCount = chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSide(chatRoom, otherSideUser, false);
            dto.setUnreadNums(unreadCount);

            dto.setOtherSideImageUrl(s3Service.generatePresignedUrl(otherSideUser.getImage()));
            dto.setOtherSideName(otherSideUser.getUsername());
            dto.setOtherSideAge(userService.getAge(otherSideUser));
            dto.setOtherSideCareer(otherSideUser.getCareer());

            dto.setCurrentUser(user);
            dto.setCurrentUserId(user.getId());
            dto.setCurrentUserImageUrl(s3Service.generatePresignedUrl(user.getImage()));

            Optional<ChatroomMessage> optionalLastMessage = chatroomMessageRepository.findFirstByChatroomOrderByCreateAt(chatRoom);
            if (optionalLastMessage.isPresent()) {
                ChatroomMessage lastMessage = optionalLastMessage.get();
                if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {
                    dto.setLastMessage(lastMessage.getContent().substring(0, Math.min(15, lastMessage.getContent().length())));
                } else if (lastMessage.getImage() != null && !lastMessage.getImage().isEmpty()) {
                    dto.setLastMessage("已傳送圖片");
                } else {
                    dto.setLastMessage("");
                }

                int chatRoomsNotReadMessages = chatroomMessageRepository.countByChatroomAndIsReadByOtherSideAndSenderNot(chatRoom, false, user);
                dto.setUnreadNums(chatRoomsNotReadMessages);
                dto.setLastMessageTime(getLastUpdateAt(chatRoom));
            }
            chatRoomDTOs.add(dto);
        }

        return chatRoomDTOs;
    }

    public ChatRoomDTO retrieveChatRoom(User user, Long chatroomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId).orElse(null);

        if (chatRoom == null) {
            throw new ResourceNotFoundException("Chat room not found"); // Or handle as per your error management
        }

        ChatRoomDTO dto = new ChatRoomDTO(chatRoom);
        User otherSideUser = chatroomUserShipRepository.findOtherSideUser(chatRoom, user);

        dto.setOtherSideUser(otherSideUser);
        dto.setCurrentUser(user);
        dto.setOtherSideAge(userService.getAge(otherSideUser));
        dto.setCurrentUserId(user.getId());

        Optional<ChatroomMessage> optionalLastMessage = chatroomMessageRepository.findFirstByChatroomOrderByCreateAt(chatRoom);
        if (optionalLastMessage.isPresent()) {
            dto.setLastMessageTime(getLastUpdateAt(chatRoom));
        }

        return dto;
    }

    public ChatRoomDTO createChatRoom(User user ,String otherSideUserPhone) {

        User otherSideUser = userRepository.findByPhone(otherSideUserPhone)
                .orElseThrow(() -> new RuntimeException("Other side user not found"));

        Set<Long> user1Chatrooms = chatroomUserShipRepository.findByUser(user).stream()
                .map(ChatroomUserShip::getChatroom)
                .map(ChatRoom::getId)
                .collect(Collectors.toSet());

        Set<Long> user2Chatrooms = chatroomUserShipRepository.findByUser(otherSideUser).stream()
                .map(ChatroomUserShip::getChatroom)
                .map(ChatRoom::getId)
                .collect(Collectors.toSet());

        user1Chatrooms.retainAll(user2Chatrooms); // This retains only the common elements, effectively computing the intersection.

        ChatRoom chatroom;
        if (user1Chatrooms.isEmpty()) {
            chatroom = new ChatRoom();
            chatRoomRepository.save(chatroom);

            ChatroomUserShip ship1 = new ChatroomUserShip(chatroom, user);
            ChatroomUserShip ship2 = new ChatroomUserShip(chatroom, otherSideUser);
            chatroomUserShipRepository.save(ship1);
            chatroomUserShipRepository.save(ship2);
        } else {
            chatroom = chatRoomRepository.findFirstByIdIn(user1Chatrooms);
        }
        // Assuming you have a method to generate presigned URLs similar to the Django version.
        String presignedUrl = s3Service.generatePresignedUrl(otherSideUser.getImage());


// 3. Convert to DTO and return
        ChatRoomDTO chatRoomDTO = convertToChatRoomDTO(chatroom,otherSideUser);
        // 2. Setting values on the chatroom object
        chatRoomDTO.setOtherSideUser(otherSideUser);
        chatRoomDTO.setCurrentUser(user);
        chatRoomDTO.setOtherSideImageUrl(presignedUrl);

        // Perform WebSocket Notification
        notifyUsersViaWebSocket(chatroom);

        return chatRoomDTO;
    }

    public List<ChatRoomDTO> deleteUserChatRoom(User user, User otherSideUser,String isChat) {


        Set<Long> chatroomsForUser1 = chatroomUserShipRepository.findByUser(user).stream()
                .map(ChatroomUserShip::getChatroom)
                .map(ChatRoom::getId)
                .collect(Collectors.toSet());

        List<ChatroomUserShip> commonChatroomShips = chatroomUserShipRepository.findByUserAndChatroomIdIn(otherSideUser, chatroomsForUser1);

        if (!commonChatroomShips.isEmpty()) {
            ChatRoom chatroomToDelete = commonChatroomShips.get(0).getChatroom();
            chatRoomRepository.delete(chatroomToDelete);
        }

        // Assume there's a match repository to handle deletion of matches.
        UserMatch matchToDelete = matchRepository.findByUsersCombination(user, otherSideUser).orElse(null);
        if (matchToDelete != null) {
            matchRepository.delete(matchToDelete);
        }

        List<ChatRoom> userChatRooms = getChatroomList(user);

//                if (!"no".equals(isChat)) {
//            userChatRooms = getChatroomList(user);
//        }

        // TODO: Convert these ChatRooms to DTOs with additional data and return them.
        String presignedUrl = s3Service.generatePresignedUrl(otherSideUser.getImage());


// 3. Convert to DTO and return
        List<ChatRoomDTO> chatRoomDTOs = convertToDTO(userChatRooms,user);


        // Further logic for updating unread messages, etc. goes here ...

        // ... this method gets lengthy because the provided code does a lot. You'd continue converting the logic here.
        return chatRoomDTOs;
    }

    private void notifyUsersViaWebSocket(ChatRoom chatroom) {
        ChatRoomDTO chatRoomDTO = convertToChatRoomDTO(chatroom, null); // Assuming conversion can handle null for the otherSideUser
        webSocketService.sendChatRoomUpdate(chatRoomDTO);
    }

    private ChatRoomDTO convertToChatRoomDTO(ChatRoom chatroom, User otherSideUser) {
        ChatRoomDTO dto = new ChatRoomDTO(chatroom);
        dto.setId(chatroom.getId());
//        dto.setRoomName(chatroom.getRoomName());
        // ... add other chatroom fields
        if (otherSideUser != null) {
            dto.setOtherSideName(otherSideUser.getUsername());
            dto.setOtherSideImageUrl(otherSideUser.getImage());
            // ... add other user-related fields
        }
        return dto;
    }
    private List<ChatRoomDTO> convertToDTO(List<ChatRoom> chatRooms,User user) {
        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDTO dto = new ChatRoomDTO(chatRoom);
            User otherSideChatRoomUser = chatRoomRepository.findOtherSideUser(user,chatRoom);
            int unreadCount = chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSideFalse(chatRoom, otherSideChatRoomUser);

            if (unreadCount > 0) {
                dto.setUnreadNums(unreadCount);
            }
            // Here, populate the DTO's attributes based on the ChatRoom entity.
            dto.setOtherSideImageUrl(s3Service.generatePresignedUrl(otherSideChatRoomUser.getImage()));
            dto.setOtherSideName(otherSideChatRoomUser.getUsername());
            dto.setOtherSideAge(userService.getAge(otherSideChatRoomUser));
            dto.setOtherSideCareer(otherSideChatRoomUser.getCareer());

            dto.setId(chatRoom.getId());
            dto.setCurrentUser(user);
            dto.setCurrentUserId(user.getId());
            dto.setCurrentUserImageUrl(user.getImage());
            // ... and so on for other attributes

            List<ChatroomMessage> messages = chatroomMessageRepository.findByChatroomOrderByCreateAtDesc(chatRoom);

            if (!messages.isEmpty()) {
                ChatroomMessage lastMessage = messages.get(0);  // Get the first item from the ordered list, which is the latest message

                if (lastMessage.getContent() != null && lastMessage.getContent().isEmpty()) {
                    dto.setLastMessage(lastMessage.getContent().substring(0, Math.min(15, lastMessage.getContent().length())));
                } else if (lastMessage.getImage() != null && !lastMessage.getImage().isEmpty()) {
                    dto.setLastMessage("已傳送圖片");
                } else {
                    dto.setLastMessage("");
                }

                unreadCount = chatroomMessageRepository.countByChatroomAndIsReadByOtherSideFalseAndSenderNot(chatRoom, user);
                dto.setUnreadNums(unreadCount);
                dto.setLastMessageTime(chatRoom.getUpdateAt());
            }
            chatRoomDTOs.add(dto);
        }
        return chatRoomDTOs;
    }
    public LocalDateTime getLastUpdateAt(ChatRoom chatroom) {
        ChatroomMessage lastMessage = chatroomMessageRepository.findFirstByChatroomOrderByCreateAt(chatroom)
                .orElse(null);
        if (lastMessage != null) {
            return lastMessage.getCreateAt();
        }
        return null;  // or you might want to handle this differently
    }

//    public List<Map<String, Object>> getUnreadChatroomMessageCount(User user, boolean isSender, User otherSideUser) {
//        List<Long> chatroomIds;
//        List<ChatRoom> chatrooms;
//
//        if (isSender) {
//            chatroomIds = chatroomUserShipRepository.findChatroomIdsByUser(otherSideUser);
//            chatrooms = chatRoomRepository.findBySenderIdAndUnread(user, chatroomIds);
//        } else {
//            chatroomIds = chatroomUserShipRepository.findChatroomIdsByUser(user);
//            chatrooms = chatRoomRepository.findByReceiverIdAndUnread(user, chatroomIds);
//        }
//
//        List<Map<String, Object>> chatroomMessageCountList = new ArrayList<>();
//        for (ChatRoom chatroom : chatrooms) {
//            Map<String, Object> chatroomMessageMap = new HashMap<>();
//            chatroomMessageMap.put("chatroom", chatroom.getId());
//            chatroomMessageMap.put("unread_nums", chatroom.getUnreadNums()); // Assuming you've added getUnreadNums method in ChatRoom entity.
//            chatroomMessageCountList.add(chatroomMessageMap);
//        }
//
//        return chatroomMessageCountList;
//    }

    public List<ChatRoom> getChatroomList(User user) {
        List<Long> chatroomIds = chatroomUserShipRepository.findChatroomIdsByUser(user);
        return chatRoomRepository.findChatroomsWithMessages(chatroomIds);
    }
}




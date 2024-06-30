package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.dao.*;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ChatRoomService {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

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
            return convertToDTO(chatRooms, user); // Simple conversion if isChat is 'no'
        }

//        for (ChatRoom chatRoom : chatRooms) {
//            ChatRoomDTO dto = new ChatRoomDTO(chatRoom);
//            User otherSideUser = chatroomUserShipRepository.findOtherSideUser(chatRoom, user);
//            dto.setOtherSideAbout(otherSideUser.getAboutMe());
//            log.info("chatRoomId is : " + chatRoom.getId());
//            dto.setChatroomId(chatRoom.getId());
//            int unreadCount = chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSide(chatRoom, otherSideUser, false);
//            dto.setUnreadNums(unreadCount);
//            dto.setOtherSideImageUrl(s3Service.getPresignedUrl(otherSideUser.getImage()));
//            dto.setOtherSideName(otherSideUser.getUsername());
//            dto.setOtherSideAge(userService.getAge(otherSideUser));
//            dto.setCurrentUserId(user.getId());
//            dto.setCurrentUserImageUrl(s3Service.getPresignedUrl(user.getImage()));
//            Optional<ChatroomMessage> lastMessageOptional = chatroomMessageRepository.findTopByChatroomOrderByCreateAtDesc(chatRoom);
//            if (lastMessageOptional.isPresent()) {
//                ChatroomMessage lastMessage = lastMessageOptional.get();
//                if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {
//                    dto.setLastMessage(lastMessage.getContent().substring(0, Math.min(15, lastMessage.getContent().length())));
//                } else if (lastMessage.getImage() != null && !lastMessage.getImage().isEmpty()) {
//                    dto.setLastMessage("已傳送圖片");
//                } else {
//                    dto.setLastMessage("");
//                }
//            } else {
//                // 如果没有找到最后一条消息，将LastMessage设置为空字符串
//                dto.setLastMessage("");
//            }
//
//            int chatRoomsNotReadMessages = chatroomMessageRepository.countByChatroomAndIsReadByOtherSideAndSenderNot(chatRoom, false, user);
//            dto.setUnreadNums(chatRoomsNotReadMessages);
//            dto.setLastMessageTime(getLastUpdateAt(chatRoom));
//
//            chatRoomDTOs.add(dto);
//
//        }

        return convertToDTO(chatRooms,user);
    }

    public List<ChatRoomDTO> retrieveChatRoom(User user, Long chatroomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId).orElse(null);

        if (chatRoom == null) {
            throw new ResourceNotFoundException("Chat room not found"); // Or handle as per your error management
        }
        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        ChatRoomDTO dto = new ChatRoomDTO(chatRoom);

        User otherSideUser = chatroomUserShipRepository.findOtherSideUser(chatRoom, user);
        dto.setOtherSideAbout(otherSideUser.getAboutMe());
        dto.setChatroomId(chatRoom.getId());

        int unreadCount = chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSide(chatRoom, otherSideUser, false);
        dto.setUnreadNums(unreadCount);

        dto.setOtherSideImageUrl(s3Service.getPresignedUrl(otherSideUser.getImage()));
        dto.setOtherSideName(otherSideUser.getUsername());
        dto.setOtherSideAge(userService.getAge(otherSideUser));
        dto.setOtherSideCareer(otherSideUser.getCareer());

        dto.setCurrentUserId(user.getId());
        dto.setCurrentUserImageUrl(s3Service.getPresignedUrl(user.getImage()));

        Optional<ChatroomMessage> lastMessageOptional = chatroomMessageRepository.findTopByChatroomOrderByCreateAtDesc(chatRoom);
        if (lastMessageOptional.isPresent()) {
            ChatroomMessage lastMessage = lastMessageOptional.get();

            if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {

                dto.setLastMessage(lastMessage.getContent().substring(0, Math.min(15, lastMessage.getContent().length())));
            } else if (lastMessage.getImage() != null && !lastMessage.getImage().isEmpty()) {
                dto.setLastMessage("已傳送圖片");
            } else {
                dto.setLastMessage("");
            }
        } else {
            // 如果没有找到最后一条消息，将LastMessage设置为空字符串
            dto.setLastMessage("");
        }
        int chatRoomsNotReadMessages = chatroomMessageRepository.countByChatroomAndIsReadByOtherSideAndSenderNot(chatRoom, false, user);
        dto.setUnreadNums(chatRoomsNotReadMessages);
        dto.setLastMessageTime(getLastUpdateAt(chatRoom));

        chatRoomDTOs.add(dto);
//        notifyUsersViaWebSocket(user, chatRoomDTOs);
        return chatRoomDTOs;

    }

    public ChatRoomDTO createChatRoom(User user, String otherSideUserPhone) {

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
        String presignedUrl = s3Service.getPresignedUrl(otherSideUser.getImage());
// 3. Convert to DTO and return
        ChatRoomDTO chatRoomDTO = convertToChatRoomDTO(chatroom, otherSideUser);
        // 2. Setting values on the chatroom object
        chatRoomDTO.setOtherSideAbout(otherSideUser.getAboutMe());
        chatRoomDTO.setChatroomId(chatroom.getId());
        chatRoomDTO.setOtherSideImageUrl(presignedUrl);
        chatRoomDTO.setOtherSideAge(userService.getAge(otherSideUser));
        List<ChatRoomDTO> chatRoomDTOS = new ArrayList<>();
        chatRoomDTOS.add(chatRoomDTO);
        // 假设你的ChatRoom实体或DTO有一个方法可以获取所有的participant userIds
        // Perform WebSocket Notification
        notifyUsersViaWebSocket(user, chatRoomDTOS);
        return chatRoomDTO;
    }

    public List<ChatRoomDTO> deleteUserChatRoom(User user, User otherSideUser, String isChat) {


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

        // 3. Convert to DTO and return
        List<ChatRoomDTO> chatRoomDTOs = convertToDTO(userChatRooms, user);

        notifyUsersViaWebSocket(user, chatRoomDTOs);

        List<ChatRoom> otherSideChatRooms = getChatroomList(otherSideUser);
        otherSideChatRooms = chatRoomRepository.findChatroomsWithMessagesIn(otherSideChatRooms);

        List<ChatRoomDTO> otherChatRoomDTOs = convertToDTO(otherSideChatRooms, otherSideUser);

        notifyUsersViaWebSocket(otherSideUser, otherChatRoomDTOs);
        // Further logic for updating unread messages, etc. goes here ...

        // ... this method gets lengthy because the provided code does a lot. You'd continue converting the logic here.
        return chatRoomDTOs;
    }

    private void notifyUsersViaWebSocket(User user, List<ChatRoomDTO> chatRoomDTOs) {
        webSocketService.chatrooms(String.valueOf(user.getId()), chatRoomDTOs,Optional.empty());
    }

    private ChatRoomDTO convertToChatRoomDTO(ChatRoom chatroom, User otherSideUser) {
        ChatRoomDTO dto = new ChatRoomDTO(chatroom);
        dto.setChatroomId(chatroom.getId());
//        dto.setRoomName(chatroom.getRoomName());
        // ... add other chatroom fields
        if (otherSideUser != null) {
            dto.setOtherSideName(otherSideUser.getUsername());
            dto.setOtherSideImageUrl(otherSideUser.getImage());
            // ... add other user-related fields
        }
        return dto;
    }

    private List<ChatRoomDTO> convertToDTO(List<ChatRoom> chatRooms, User user) {
        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDTO dto = new ChatRoomDTO(chatRoom);
            User otherSideChatRoomUser = chatRoomRepository.findOtherSideUser(user, chatRoom);
            int unreadCount = chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSideFalse(chatRoom, otherSideChatRoomUser);

            if (unreadCount > 0) {
                dto.setUnreadNums(unreadCount);
            }
            // Here, populate the DTO's attributes based on the ChatRoom entity.
            dto.setOtherSideImageUrl(s3Service.getPresignedUrl(otherSideChatRoomUser.getImage()));
            dto.setOtherSideName(otherSideChatRoomUser.getUsername());
            dto.setOtherSideAge(userService.getAge(otherSideChatRoomUser));
            dto.setOtherSideCareer(otherSideChatRoomUser.getCareer());
            dto.setCurrentUserId(user.getId());
            dto.setCurrentUserImageUrl(user.getImage());
            dto.setOtherSideChatRoomUser(otherSideChatRoomUser);
            dto.setChatroomId(chatRoom.getId());
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
        ChatroomMessage lastMessage = chatroomMessageRepository.findFirstByChatroomOrderByCreateAtDesc(chatroom);
        if (lastMessage != null) {
            return lastMessage.getCreateAt();
        }
        return null;  // or you might want to handle this differently
    }

    public List<ChatRoom> getChatroomList(User user) {
        List<Long> chatroomIds = chatroomUserShipRepository.findChatroomIdsByUser(user);
        return chatRoomRepository.findChatroomsWithMessagesOrderByUpdateAtDesc(chatroomIds);
    }

    public List<String> getParticipantUserIds(ChatRoomDTO chatRoomDTO) {
        long id = chatRoomDTO.getChatroomId();
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(id);
        List<User> users = chatRoomRepository.findAllUsersByChatRoom(optionalChatRoom.get());
        List<Long> integerList = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        List<String> participantUserIds = integerList.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        return participantUserIds;
    }
}




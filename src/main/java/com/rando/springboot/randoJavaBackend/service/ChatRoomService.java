package com.rando.springboot.randoJavaBackend.service;

import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.dao.*;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.*;
import org.jetbrains.annotations.NotNull;
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
        List<ChatRoom> chatRooms = chatRoomRepository.findByIdInOrderByUpdateAtDesc(chatroomIds);// Simple conversion if isChat is 'no'

        return getChatRoomDTOS(user,chatRooms);
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
//        dto.setOtherSideChatRoomUser(otherSideUser);
        dto.setOtherSideUserInfo(otherSideUser.getAboutMe());
        dto.setChatroomId(chatroomId);

        Optional<ChatroomMessage> lastMessageOptional = chatroomMessageRepository.findTopByChatroomOrderByCreateAtDesc(chatRoom);
        if (lastMessageOptional.isPresent()) {
            ChatroomMessage lastMessage = lastMessageOptional.get();
            getChatRoomDTO(dto, lastMessage);

        } else {
            // 如果没有找到最后一条消息，将LastMessage设置为空字符串
            dto.setLastMessage("");
        }
        int chatRoomsNotReadMessages = chatroomMessageRepository.countByChatroomAndIsReadByOtherSideAndSenderNot(chatRoom, false, user);
        dto.setUnreadNums(chatRoomsNotReadMessages);
        dto.setLastMessageTime(getLastUpdateAt(chatRoom));

        chatRoomDTOs.add(dto);
//        notifyUsersViaWebSocket(user, dto);
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
//        List<ChatRoomDTO> chatRoomDTOS = new ArrayList<>();
//        chatRoomDTOS.add(chatRoomDTO);
        // 假设你的ChatRoom实体或DTO有一个方法可以获取所有的participant userIds
        // Perform WebSocket Notification
        notifyUsersViaWebSocket(user, chatRoomDTO);
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
        List<ChatRoomDTO> chatRoomDTOs = getChatRoomDTOS(user,userChatRooms);

//        notifyUsersViaWebSocket(user, chatRoomDTO);

        List<ChatRoom> otherSideChatRooms = getChatroomList(otherSideUser);
        otherSideChatRooms = chatRoomRepository.findChatroomsWithMessagesIn(otherSideChatRooms);

        List<ChatRoomDTO> otherChatRoomDTOs = getChatRoomDTOS(otherSideUser,otherSideChatRooms);

//        notifyUsersViaWebSocket(otherSideUser, otherChatRoomDTOs);
        // Further logic for updating unread messages, etc. goes here ...

        // ... this method gets lengthy because the provided code does a lot. You'd continue converting the logic here.
        return chatRoomDTOs;
    }

    private void notifyUsersViaWebSocket(User user, ChatRoomDTO chatRoomDTO) {
        webSocketService.chatrooms(String.valueOf(user.getId()), chatRoomDTO,Optional.empty());
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

    private List<ChatRoomDTO> getChatRoomDTOS(User user, List<ChatRoom> chatRooms) {
        List<ChatRoomDTO> chatRoomDTOS = new ArrayList<>();
        for (ChatRoom each_chatroom : chatRooms) {
            ChatRoomDTO chatRoomDTO = addDTO(user, each_chatroom, chatroomUserShipRepository, chatroomMessageRepository, s3Service, chatRoomRepository, userService);
            chatRoomDTOS.add(chatRoomDTO);

        }
        return chatRoomDTOS;
    }

    static ChatRoomDTO addDTO(User user, ChatRoom each_chatroom, ChatroomUserShipRepository chatroomUserShipRepository, ChatroomMessageRepository chatroomMessageRepository, S3Service s3Service, ChatRoomRepository chatRoomRepository, UserService userService) {
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO(each_chatroom);
        User otherSideChatroomUser = chatroomUserShipRepository.findFirstByChatroomAndUserNot(each_chatroom, user).getUser();

        int unreadCount = chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSideFalse(each_chatroom, otherSideChatroomUser);
        if (unreadCount != 0) {
            chatRoomDTO.setUnreadNums(unreadCount);
        }

        chatRoomDTO.setOtherSideImageUrl(s3Service.getPresignedUrl(otherSideChatroomUser.getImage()));
        chatRoomDTO.setOtherSideName(otherSideChatroomUser.getUsername());

        log.debug("Checking for messages in chatroom with ID: {}", each_chatroom.getId());

        int messageCount = chatroomMessageRepository.countByChatroom(each_chatroom);
        log.debug("Message count for chatroom ID {}: {}", each_chatroom.getId(), messageCount);

        if (messageCount > 0) {
            ChatroomMessage lastMessage = chatroomMessageRepository.findFirstByChatroomOrderByCreateAtDesc(each_chatroom);
            log.debug("Last message for chatroom ID {}: {}", each_chatroom.getId(), lastMessage);

            chatRoomDTO.setLastMessageTime(lastMessage.getCreateAt());
            each_chatroom.setUpdateAt(lastMessage.getCreateAt());
            chatRoomRepository.save(each_chatroom);

            log.debug("Last Message Content: {}", lastMessage.getContent());
            log.debug("Last Message Image: {}", lastMessage.getImage());

            String lastMessageContent = lastMessage.getContent();
            String lastMessageImage = lastMessage.getImage();

            if (lastMessageContent != null && !lastMessageContent.isEmpty()) {
                chatRoomDTO.setLastMessage(lastMessageContent.length() > 15 ? lastMessageContent.substring(0, 15) : lastMessageContent);
            } else if (lastMessageImage != null && !lastMessageImage.isEmpty()) {
                chatRoomDTO.setLastMessage("已傳送圖片");
            } else {
                chatRoomDTO.setLastMessage("");
            }
        } else {
            log.debug("No messages found for chatroom ID: {}", each_chatroom.getId());
        }

        chatRoomDTO.setChatroomId(each_chatroom.getId());
        chatRoomDTO.setOtherSideAge(userService.getAge(otherSideChatroomUser));
        chatRoomDTO.setOtherSideCareer(otherSideChatroomUser.getCareer());
        chatRoomDTO.setCurrentUserId(user.getId());
        chatRoomDTO.setOtherSideAbout(otherSideChatroomUser.getAboutMe());
        chatRoomDTO.setChatroomId(each_chatroom.getId());

        return chatRoomDTO;
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

    private void getChatRoomDTO(ChatRoomDTO dto, ChatroomMessage lastMessage) {
        if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {
            dto.setLastMessage(lastMessage.getContent().substring(0, Math.min(15, lastMessage.getContent().length())));
        } else if (lastMessage.getImage() != null && !lastMessage.getImage().isEmpty()) {
            dto.setLastMessage("已傳送圖片");
        } else {
            dto.setLastMessage("");
        }
    }
}




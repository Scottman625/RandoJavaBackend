package com.rando.springboot.randoJavaBackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.dao.*;
import com.rando.springboot.randoJavaBackend.dto.ChatMessageDTO;
import com.rando.springboot.randoJavaBackend.dto.ChatRoomDTO;
import com.rando.springboot.randoJavaBackend.entity.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.in;
import static org.aspectj.runtime.internal.Conversions.intValue;

@Service
public class ChatroomMessageService {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatroomMessageRepository chatroomMessageRepository;

    @Autowired
    private ChatroomUserShipRepository chatroomUserShipRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserImageService userImageService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private WebSocketService webSocketService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public void refreshChatMessages(User user, Long chatroomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId).orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found"));
        List<ChatroomMessage> messages = chatroomMessageRepository.findByChatroomOrderByCreateAtAsc(chatRoom);
//        chatroomMessageRepository.markAsReadByOtherSideWhereSenderIsNotUser(chatRoom, user);
        Optional<List<ChatroomMessage>> chatMessages = chatroomMessageRepository.findByChatroomAndSenderNot(chatRoom, user);
        if (chatMessages.isPresent()){
            for(ChatroomMessage message:chatMessages.get()){
                message.setReadByOtherSide(true);
                chatroomMessageRepository.save(message);
            }
        }
        List<ChatRoom> chatRooms = chatRoomService.getChatroomList(user);

        List<ChatRoomDTO> chatRoomDTOS= getChatRoomDTOS(user,chatRooms);

        List<User> chatroomUsers = chatroomUserShipRepository.findUsersByChatroom(chatRoom);

        User otherSideUser = chatroomUsers.stream()
                .filter(chatUser -> chatUser.getId() != user.getId())
                .findFirst().orElse(null);

        if (otherSideUser == null) {
            // Handle error
            return;
        }

        List<ChatMessageDTO> chatMessageDTOS = getMessageDTOS(user, messages);
        // Assuming ChatRoomDTO and MessageDTO are your DTOs, and you have relevant mappers or converters to convert your entities to these DTOs
        webSocketService.chatrooms(String.valueOf(user.getId()),chatRoomDTOS, Optional.of(chatMessageDTOS));
        // Add more logic as required from your Django view
    }

    public List<ChatMessageDTO> getMessages(Long chatroomId, User user) {
        ChatRoom chatRoom;
        if (chatRoomRepository.findById(chatroomId).isPresent()) {
            chatRoom = chatRoomRepository.findById(chatroomId).get();
            List<ChatroomUserShip> ships = chatroomUserShipRepository.findByChatroom(chatRoom);
            List<Long> userIds = ships.stream().map(ship -> ship.getUser().getId()).toList();

            if (userIds.contains(user.getId())) {
                List<ChatroomMessage> messages = chatroomMessageRepository.findByChatroomOrderByCreateAtAsc(chatRoom);

                List<ChatMessageDTO> chatMessageDTOS = new ArrayList<>();
                for (ChatroomMessage message : messages) {
                    ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
                    if (message.getSender().equals(user)) {
                        chatMessageDTO.setMessageIsMine(true);

                    } else {
                        message.setReadByOtherSide(true);
                        chatroomMessageRepository.save(message);
                    }

                    chatMessageDTO.setContent(message.getContent());
                    chatMessageDTO.setCreateAt(message.getCreateAt());

                    chatMessageDTO.setShouldShowTime(shouldShowSendTime(message));


                    if (message.getImage() != null && !message.getImage().trim().isEmpty()) {
                        chatMessageDTO.setImageUrl(s3Service.getPresignedUrl(message.getImage()));
                    }


                    chatMessageDTOS.add(chatMessageDTO);
                }

                return chatMessageDTOS;
            }
            //... your code using chatRoom
        }

        return null;
    }

    public List<ChatMessageDTO> postMessage(User user, Long chatroomId, String content, MultipartFile image) {
        ChatRoom chatRoom;
        if (chatRoomRepository.findById(chatroomId).isPresent()) {
            chatRoom = chatRoomRepository.findById(chatroomId).get();
            List<ChatroomUserShip> ships = chatroomUserShipRepository.findByChatroom(chatRoom);
            List<Long> chatroomUserIds = ships.stream().map(ship -> ship.getUser().getId()).toList();
;
            if(chatroomUserIds.contains(user.getId())){
                User otherSideUser = userRepository.findFirstNotAndIn(user,chatroomUserIds);
                ChatroomMessage newMessage = new ChatroomMessage();
                newMessage.setCreateAt(LocalDateTime.now());
                newMessage.setChatroom(chatRoom);
                newMessage.setSender(user);
                Optional<UserMatch> match = matchRepository.findByUsersCombination(user,otherSideUser);

                if (match.isPresent()){
                    newMessage.setMatch(match.get());
                }
                if(content != null && !content.isEmpty()){
                    newMessage.setContent(content);
                }
                if (image != null && !image.isEmpty()) {
                    try{
                        String fileName = "prefix-"+ "-" +currentTimeMillis() + image.getOriginalFilename();
                        String filePath = userImageService.saveTempFile(image);

                        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
                        String key ="images/" + fileName; // 這裡加上了 "images/" 前綴
                        s3.putObject(bucketName, key, new File(filePath));
                        new File(filePath).delete();
                        newMessage.setImage(s3Service.getPresignedUrl("images/" + fileName));
                    }
                    catch (Exception e){
                        log.info(e.getMessage());
                    }
                }
                chatroomMessageRepository.save(newMessage);
                chatRoom.setUpdateAt(LocalDateTime.now());
//                List<ChatroomMessage> messages = chatroomMessageRepository.findByChatroomOrderByCreateAtAsc(chatRoom);



//                List<ChatMessageDTO> chatMessageDTOS = getMessageDTOS(user, messages);
                ChatMessageDTO chatMessageDTO = getMessageDTO(user,newMessage);
                List<ChatMessageDTO> chatMessageDTOS = new ArrayList<>();
                chatMessageDTOS.add(chatMessageDTO);
                List<ChatRoom> chatRooms = chatRoomService.getChatroomList(user);
                List<ChatRoomDTO> chatRoomDTOS = getChatRoomDTOS(user, chatRooms);
                webSocketService.chatrooms(String.valueOf(user.getId()),chatRoomDTOS, Optional.of(chatMessageDTOS));

                List<ChatRoom> otherSideChatRooms = chatRoomService.getChatroomList(otherSideUser);
                List<ChatRoomDTO> otherSideChatRoomDTOS = getChatRoomDTOS(otherSideUser, otherSideChatRooms);
                webSocketService.chatrooms(String.valueOf(otherSideUser.getId()), otherSideChatRoomDTOS,Optional.of(chatMessageDTOS));


                return chatMessageDTOS;
            }
            //... your code using chatRoom
        }
        return null;
    }

    private ChatMessageDTO getMessageDTO(User user, ChatroomMessage message) {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setShouldShowTime(shouldShowSendTime(message));
        if (message.getContent() != null && !message.getContent().isEmpty()){
            chatMessageDTO.setContent(message.getContent());
        } else if (message.getImage() != null && !message.getImage().isEmpty()) {
            chatMessageDTO.setImageUrl(message.getImage());
        }

        chatMessageDTO.setCreateAt(message.getCreateAt());
        if (message.getSender().getId() == user.getId()) {
            chatMessageDTO.setMessageIsMine(true);
        }
        return chatMessageDTO;
    }

    @NotNull
    private List<ChatRoomDTO> getChatRoomDTOS(User user, List<ChatRoom> chatRooms) {
        List<ChatRoomDTO> chatRoomDTOS = new ArrayList<>();
        for (ChatRoom each_chatroom : chatRooms) {
            ChatRoomDTO chatRoomDTO = new ChatRoomDTO(each_chatroom);
            User otherSideChatroomUser = chatroomUserShipRepository.findFirstByChatroomAndUserNot(each_chatroom, user).getUser();
            if (chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSideFalse(each_chatroom, otherSideChatroomUser) != 0) {
                chatRoomDTO.setUnreadNums(chatroomMessageRepository.countByChatroomAndSenderAndIsReadByOtherSideFalse(each_chatroom, otherSideChatroomUser));
            }
            chatRoomDTO.setOtherSideImageUrl(s3Service.getPresignedUrl(otherSideChatroomUser.getImage()));
            chatRoomDTO.setOtherSideName(otherSideChatroomUser.getUsername());

            if (chatroomMessageRepository.countByChatroom(each_chatroom) > 0) {
                ChatroomMessage lastMessage = chatroomMessageRepository.findFirstByChatroomOrderByCreateAtDesc(each_chatroom);
                chatRoomDTO.setLastMessageTime(lastMessage.getCreateAt());
                each_chatroom.setUpdateAt(lastMessage.getCreateAt());
                chatRoomRepository.save(each_chatroom);
                if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {
                    String result = "";
                    if (lastMessage.getContent().length() > 15) {
                        result = lastMessage.getContent().substring(0, 15);
                    } else {
                        result = lastMessage.getContent();
                    }
                    chatRoomDTO.setLastMessage(result);
                } else if (lastMessage.getImage() != null && !lastMessage.getImage().isEmpty()) {
                    chatRoomDTO.setLastMessage("已傳送圖片");
                } else {
                    chatRoomDTO.setLastMessage("");
                }
            }
            chatRoomDTO.setChatroomId(each_chatroom.getId());
            chatRoomDTO.setOtherSideAge(userService.getAge(otherSideChatroomUser));
            chatRoomDTO.setOtherSideCareer(otherSideChatroomUser.getCareer());
            chatRoomDTO.setCurrentUserId(user.getId());
            chatRoomDTO.setOtherSideAbout(otherSideChatroomUser.getAboutMe());
            chatRoomDTO.setChatroomId(each_chatroom.getId());
            chatRoomDTOS.add(chatRoomDTO);

        }
        return chatRoomDTOS;
    }
    @NotNull
    private List<ChatMessageDTO> getMessageDTOS(User user, List<ChatroomMessage> messages) {
        List<ChatMessageDTO> chatMessageDTOS = new ArrayList<>();
        for (ChatroomMessage message : messages) {
            ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
            chatMessageDTO.setShouldShowTime(shouldShowSendTime(message));
            if (message.getContent() != null && !message.getContent().isEmpty()){
                chatMessageDTO.setContent(message.getContent());
            } else if (message.getImage() != null && !message.getImage().isEmpty()) {
                chatMessageDTO.setImageUrl(message.getImage());
            }

            chatMessageDTO.setCreateAt(message.getCreateAt());
            if (message.getSender().getId() == user.getId()) {
                chatMessageDTO.setMessageIsMine(true);
            }
            chatMessageDTOS.add(chatMessageDTO);
        }
        return chatMessageDTOS;
    }
    public boolean shouldShowSendTime(ChatroomMessage message) {
        List<ChatroomMessage> messages = chatroomMessageRepository.findBySenderAndCreateAtLessThanAndChatroomAndIdNot(
                message.getSender(), message.getCreateAt(), message.getChatroom(), message.getId());

        ChatroomMessage lastMessage = messages.isEmpty() ? null : messages.get(0);

        if(lastMessage == null){
            return true;
        }else{
            Duration duration = Duration.between(lastMessage.getCreateAt(), message.getCreateAt());
            long diffInMillis = duration.toMillis();
            if (diffInMillis > 600000) {
                return true;
            }
            return false;
        }
    }
}


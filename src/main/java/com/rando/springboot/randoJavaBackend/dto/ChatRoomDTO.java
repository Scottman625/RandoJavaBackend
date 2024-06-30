package com.rando.springboot.randoJavaBackend.dto;

import com.rando.springboot.randoJavaBackend.entity.ChatRoom;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomDTO {

    private String otherSideImageUrl;
    private String otherSideName;
    private String lastMessage;
    private Long chatroomId;  // Assuming chatroom's ID type is Long
    private LocalDateTime lastMessageTime;  // Use LocalDateTime for date-time without timezone

    private LocalDateTime updateAt;
    private Integer otherSideAge;
    private String otherSideCareer;
    private String otherSideAbout;
    private Integer unreadNums;
    private Long currentUserId;
    private String OtherSideUserInfo;
    private List<String> participantUserIds;
    private User otherSideChatRoomUser;
    public ChatRoomDTO(ChatRoom chatRoom) {

    }

    // Standard getters and setters for each attribute...

    public String getOtherSideImageUrl() {
        return otherSideImageUrl;
    }
    public void setOtherSideImageUrl(String otherSideImageUrl) {
        this.otherSideImageUrl = otherSideImageUrl;
    }

    public String getOtherSideName() {
        return otherSideName;
    }
    public void setOtherSideName(String otherSideName) {
        this.otherSideName = otherSideName;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getChatroomId() {
        return chatroomId;
    }
    public void setChatroomId(Long chatroomId) {
        this.chatroomId = chatroomId;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }
    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getOtherSideAge() {
        return otherSideAge;
    }
    public void setOtherSideAge(Integer otherSideAge) {
        this.otherSideAge = otherSideAge;
    }

    public String getOtherSideCareer() {
        return otherSideCareer;
    }
    public void setOtherSideCareer(String otherSideCareer) {
        this.otherSideCareer = otherSideCareer;
    }

    public Integer getUnreadNums() {
        return unreadNums;
    }
    public void setUnreadNums(Integer unreadNums) {
        this.unreadNums = unreadNums;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }
    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setId(Long id) {
    }

    public void setCurrentUserImageUrl(String s) {
    }
    public List<String> getParticipantUserIds() {
        return participantUserIds;
    }
    public void setParticipantUserIds(List<String> participantUserIds) {
        this.participantUserIds = participantUserIds;
    }

    public String getOtherSideUserInfo() {
        return OtherSideUserInfo;
    }
    public void setOtherSideUserInfo(String otherSideUserInfo) {
        OtherSideUserInfo = otherSideUserInfo;
    }

    public User getOtherSideChatRoomUser() {
        return otherSideChatRoomUser;
    }
    public void setOtherSideChatRoomUser(User otherSideChatRoomUser) {
        this.otherSideChatRoomUser = otherSideChatRoomUser;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }


    public String getOtherSideAbout() {
        return otherSideAbout;
    }

    public void setOtherSideAbout(String otherSideAbout) {
        this.otherSideAbout = otherSideAbout;
    }
}



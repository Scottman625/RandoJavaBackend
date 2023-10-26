package com.rando.springboot.randoJavaBackend.dto;

import com.rando.springboot.randoJavaBackend.entity.ChatroomMessage;

import java.time.LocalDateTime;

public class ChatMessageDTO {
//    private ChatroomMessage chatroomMessage;
    private Boolean messageIsMine;
//    private String otherSideImageUrl;
//    private String otherSidePhone;
    private String content;
    private Boolean shouldShowTime;
    private String imageUrl;
    private LocalDateTime createAt;

    public ChatMessageDTO(){

    }

//    public ChatroomMessage getChatroomMessage() {
//        return chatroomMessage;
//    }
//
//    public void setChatroomMessage(ChatroomMessage chatroomMessage) {
//        this.chatroomMessage = chatroomMessage;
//    }

    public Boolean getMessageIsMine() {
        return messageIsMine;
    }

    public void setMessageIsMine(Boolean messageIsMine) {
        this.messageIsMine = messageIsMine;
    }

//    public String getOtherSideImageUrl() {
//        return otherSideImageUrl;
//    }
//
//    public void setOtherSideImageUrl(String otherSideImageUrl) {
//        this.otherSideImageUrl = otherSideImageUrl;
//    }
//
//    public String getOtherSidePhone() {
//        return otherSidePhone;
//    }
//
//    public void setOtherSidePhone(String otherSidePhone) {
//        this.otherSidePhone = otherSidePhone;
//    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getShouldShowTime() {
        return shouldShowTime;
    }

    public void setShouldShowTime(Boolean shouldShowTime) {
        this.shouldShowTime = shouldShowTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }
}

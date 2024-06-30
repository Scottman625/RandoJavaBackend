package com.rando.springboot.randoJavaBackend.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
@Entity
@Table(name = "chatroom_message")
public class ChatroomMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatroom;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private UserMatch match;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Getter
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "create_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createAt;

    // Assuming a String representation for the image URL after upload.
    @Column(name = "image")
    private String image;

    @Column(name = "is_read_by_other_side")
    private boolean isReadByOtherSide;

    // getters, setters, and other methods like shouldShowSendTime() (which would need additional logic in Java).


    public ChatroomMessage(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatRoom getChatroom() {
        return chatroom;
    }

    public void setChatroom(ChatRoom chatroom) {
        this.chatroom = chatroom;
    }

    public UserMatch getMatch() {
        return match;
    }

    public void setMatch(UserMatch match) {
        this.match = match;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isReadByOtherSide() {
        return isReadByOtherSide;
    }

    public void setReadByOtherSide(boolean readByOtherSide) {
        isReadByOtherSide = readByOtherSide;
    }
}


package com.rando.springboot.randoJavaBackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chatroom_usership")
public class ChatroomUserShip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatroom;

    @Column(name = "is_create")
    private boolean isCreate = false;

    // getters, setters, etc.
    public ChatroomUserShip(){}
    public ChatroomUserShip(ChatRoom chatroom,User user){
        this.chatroom = chatroom;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ChatRoom getChatroom() {
        return chatroom;
    }

    public void setChatroom(ChatRoom chatroom) {
        this.chatroom = chatroom;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean create) {
        isCreate = create;
    }
}


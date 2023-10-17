package com.rando.springboot.randoJavaBackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "update_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updateAt;

    // Assume there is another Entity called ChatroomMessage which has a method to get the last update timestamp.
    public Date lastUpdateAt() {
        // TODO: Implement this logic with appropriate repository/DAO call.
        return null;  // Temporary placeholder
    }

    // getters, setters, etc.
    public ChatRoom(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

}


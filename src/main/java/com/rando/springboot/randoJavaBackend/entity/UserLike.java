package com.rando.springboot.randoJavaBackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_like")
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "liked_user_id")
    private User likedUser;

    @Column(name = "is_like")
    private Boolean likeStatus;

    // ... Getters, setters, etc ...

    public Boolean getLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(Boolean likeStatus) {
        this.likeStatus = likeStatus;
    }

    @PrePersist
    @PreUpdate
    public void checkMatchCreation() {
        // This will need some adjustments because in Java/Spring,
        // you don't access the repository directly from the entity.
        // Generally, this logic would be in a service class.
    }

    public UserLike(){}

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getLikedUser() {
        return likedUser;
    }

    public void setLikedUser(User likedUser) {
        this.likedUser = likedUser;
    }

}


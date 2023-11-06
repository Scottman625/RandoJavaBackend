package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

//    UserImage findFirstByUserOrderByUpdateAtDesc(User user);

    List<UserImage> findByUser(User user);
    Optional<UserImage> findByUserAndId(User user, Long id);

    UserImage findFirstByUserOrderByUpdateAtAsc(User user);
}


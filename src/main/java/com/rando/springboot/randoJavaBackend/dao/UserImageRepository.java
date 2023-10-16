package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    UserImage findFirstByUserOrderByUpdateAtDesc(User user);

}


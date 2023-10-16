package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.ChatroomUserShip;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatroomUserShipRepository extends JpaRepository<ChatroomUserShip, Long> {
}

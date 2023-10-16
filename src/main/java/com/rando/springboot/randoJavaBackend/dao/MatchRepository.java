package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<UserMatch, Long> {

    // Check if a Match relationship exists between two users
    int countByUser1AndUser2(User user1, User user2);

    List<UserMatch> findAll();

}


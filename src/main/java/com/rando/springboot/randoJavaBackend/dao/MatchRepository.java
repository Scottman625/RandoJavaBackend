package com.rando.springboot.randoJavaBackend.dao;

import com.rando.springboot.randoJavaBackend.entity.UserMatch;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<UserMatch, Long> {

    // Check if a Match relationship exists between two users
    int countByUser1AndUser2(User user1, User user2);

    int countByUser2AndUser1(User user1, User user2);

    List<UserMatch> findAll();

    @Query("SELECT m FROM UserMatch m WHERE (m.user1 = :user AND m.user2 = :otherSideUser) OR (m.user1 = :otherSideUser AND m.user2 = :user)")
    Optional<UserMatch> findByUsersCombination(@Param("user") User user, @Param("otherSideUser") User otherSideUser);

    @Query("SELECT m FROM UserMatch m WHERE m.user1 = :user OR m.user2 = :user")
    List<UserMatch> findAllByUser(User user);

}

